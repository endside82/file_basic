package com.endside.file.manage.service;

import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.config.error.exception.RestException;
import com.endside.file.manage.constant.CategoryType;
import com.endside.file.manage.constant.FileType;
import com.endside.file.manage.service.repo.S3PathGenerator;
import com.endside.file.util.AmazonS3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkUploadService {

    private final AmazonS3Util amazonS3Util;
    private final S3PathGenerator s3PathGenerator;

    private static final int MAX_REDIRECTS = 5;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * 엑셀 파일에서 URL 컬럼을 읽어 파일을 다운로드 후 S3에 업로드하고,
     * 옆 컬럼에 원본 파일명 / S3 path / 결과를 채워서 엑셀을 반환한다.
     *
     * 입력 엑셀 형식:
     *   | A (url)                          |
     *   | https://example.com/image1.png   |
     *   | https://example.com/doc.pdf      |
     *
     * 출력 엑셀 형식:
     *   | A (url)                          | B (error)            | C (originalFileName)              | D (s3Path)              |
     *   | https://example.com/image1.png   |                      | image1.png                        | image/20260312/uuid.png |
     *   | https://example.com/doc.pdf      |                      | doc.pdf                           | none/20260312/uuid.pdf  |
     *   | ftp://invalid/url                | http/https URL만 가능 |                                   |                         |
     */
    public byte[] bulkUploadFromExcel(MultipartFile excelFile, String category, String type) {
        CategoryType categoryType = CategoryType.getStorageTypeByCategory(category);
        if (categoryType == CategoryType.UNKNOWN) {
            throw new InvalidParameterException(ErrorCode.INVALID_CATEGORY);
        }

        FileType fileType = FileType.getFileTypeByTypePath(type);
        if (fileType == FileType.UNKNOWN) {
            throw new InvalidParameterException(ErrorCode.INVALID_CATEGORY);
        }

        S3AccessInfo accessInfo = s3PathGenerator.pathGenerator(categoryType, fileType, "admin");
        String bucketName = accessInfo.getBucket();
        String basePath = accessInfo.getPath();

        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getLastRowNum() < 0 || sheet.getPhysicalNumberOfRows() == 0) {
                throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
            }

            boolean hasHeader = isHeaderRow(sheet.getRow(0));
            int dataStartRow;

            if (hasHeader) {
                Row header = sheet.getRow(0);
                setCellValue(header, 1, "error");
                setCellValue(header, 2, "originalFileName");
                setCellValue(header, 3, "s3Path");
                dataStartRow = 1;
            } else {
                sheet.shiftRows(0, sheet.getLastRowNum(), 1);
                Row header = sheet.createRow(0);
                setCellValue(header, 0, "url");
                setCellValue(header, 1, "error");
                setCellValue(header, 2, "originalFileName");
                setCellValue(header, 3, "s3Path");
                dataStartRow = 1;
            }

            if (dataStartRow > sheet.getLastRowNum()) {
                throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
            }

            int processedCount = 0;

            for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Cell urlCell = row.getCell(0);
                if (urlCell == null) continue;

                String url = getCellStringValue(urlCell).trim();
                if (url.isEmpty()) continue;

                // 결과 컬럼 초기화 (이전 실행 결과 잔존 방지)
                setCellValue(row, 1, "");
                setCellValue(row, 2, "");
                setCellValue(row, 3, "");

                if (!isValidHttpUrl(url)) {
                    setCellValue(row, 1, "http/https URL만 가능");
                    processedCount++;
                    continue;
                }

                String originalFileName = extractFileNameFromUrl(url);

                try {
                    String s3Path = downloadAndUpload(url, originalFileName, bucketName, basePath);
                    setCellValue(row, 2, originalFileName);
                    setCellValue(row, 3, s3Path);
                } catch (Exception e) {
                    log.error("Failed to process URL: {}, error: {}", url, e.getMessage());
                    setCellValue(row, 1, e.getMessage());
                    setCellValue(row, 2, originalFileName);
                }
                processedCount++;
            }

            if (processedCount == 0) {
                throw new InvalidParameterException(ErrorCode.FILE_NOT_ATTACHED);
            }

            sheet.setColumnWidth(0, 256 * 30);
            sheet.setColumnWidth(1, 256 * 50);
            sheet.setColumnWidth(2, 256 * 10);
            sheet.setColumnWidth(3, 256 * 25);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (RestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Excel file: {}", e.getMessage());
            throw new RestException(ErrorCode.FILE_IO_EXCEPTION);
        }
    }

    // ── SSRF 방지 ──────────────────────────────────────────────────────

    /**
     * URL의 호스트를 DNS 해석 후, 내부망/메타데이터 IP 여부를 검사한다.
     * 차단 대상: loopback, link-local(169.254.x.x), site-local(10.x, 172.16-31.x, 192.168.x), any-local(0.0.0.0)
     */
    private void validateUrlSafety(String url) {
        URI uri = URI.create(url);
        String host = uri.getHost();
        if (host == null || host.isEmpty()) {
            throw new RuntimeException("URL에 호스트가 없습니다");
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new RuntimeException("호스트를 찾을 수 없습니다: " + host);
        }

        for (InetAddress addr : addresses) {
            if (addr.isLoopbackAddress()
                    || addr.isLinkLocalAddress()
                    || addr.isSiteLocalAddress()
                    || addr.isAnyLocalAddress()) {
                throw new RuntimeException("내부 네트워크 주소는 허용되지 않습니다: " + host);
            }
        }
    }

    // ── 다운로드 & 업로드 ──────────────────────────────────────────────

    private String downloadAndUpload(String url, String originalFileName, String bucketName, String basePath) {
        try {
            byte[] fileBytes = downloadWithSafeRedirects(url);

            if (fileBytes == null || fileBytes.length == 0) {
                throw new RuntimeException("Empty response body");
            }

            String extension = extractExtension(originalFileName, url);
            String saveFileName = basePath + UUID.randomUUID() + "." + extension;
            String contentType = AmazonS3Util.getContentTypeByFormatName(extension);

            InputStream inputStream = new ByteArrayInputStream(fileBytes);
            return amazonS3Util.uploadInputStream(bucketName, saveFileName, contentType, fileBytes.length, inputStream, null);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Download interrupted", e);
        } catch (IOException e) {
            throw new RuntimeException("Download failed: " + e.getMessage(), e);
        }
    }

    /**
     * 리다이렉트를 수동으로 따라가며 매 hop마다 SSRF 검증을 수행한다.
     */
    private byte[] downloadWithSafeRedirects(String url) throws IOException, InterruptedException {
        String currentUrl = url;

        for (int i = 0; i <= MAX_REDIRECTS; i++) {
            validateUrlSafety(currentUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(currentUrl))
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int statusCode = response.statusCode();

            if (statusCode >= 300 && statusCode < 400) {
                String location = response.headers().firstValue("location")
                        .orElseThrow(() -> new RuntimeException("Redirect without Location header"));
                // 상대 경로 리다이렉트 처리
                currentUrl = URI.create(currentUrl).resolve(location).toString();
                if (!isValidHttpUrl(currentUrl)) {
                    throw new RuntimeException("Redirect to non-HTTP URL: " + currentUrl);
                }
                continue;
            }

            if (statusCode != 200) {
                throw new RuntimeException("HTTP " + statusCode);
            }

            return response.body();
        }

        throw new RuntimeException("Too many redirects (max " + MAX_REDIRECTS + ")");
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────

    private boolean isHeaderRow(Row row) {
        if (row == null) return false;
        Cell firstCell = row.getCell(0);
        if (firstCell == null || firstCell.getCellType() != CellType.STRING) return false;
        String value = firstCell.getStringCellValue().trim().toLowerCase();
        return value.contains("url") || value.contains("링크") || value.contains("주소");
    }

    private boolean isValidHttpUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private String extractExtension(String originalFileName, String url) {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        }
        try {
            String path = URI.create(url).getPath();
            if (path != null && path.contains(".")) {
                String ext = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
                if (!ext.isEmpty() && ext.length() <= 10) {
                    return ext;
                }
            }
        } catch (Exception ignored) {
        }
        return "bin";
    }

    private String extractFileNameFromUrl(String url) {
        try {
            String path = URI.create(url).getPath();
            if (path != null && path.contains("/")) {
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
                if (!fileName.isEmpty()) {
                    return fileName;
                }
            }
        } catch (Exception ignored) {
        }
        return "unknown_file";
    }

    private void setCellValue(Row row, int colIdx, String value) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) {
            cell = row.createCell(colIdx);
        }
        cell.setCellValue(value != null ? value : "");
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }
}
