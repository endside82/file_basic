package com.endside.file.manage.service;

import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.exception.InvalidParameterException;
import com.endside.file.config.error.exception.RestException;
import com.endside.file.manage.constant.CategoryType;
import com.endside.file.manage.param.BatchConfirmParam;
import com.endside.file.manage.service.repo.S3PathGenerator;
import com.endside.file.util.AmazonS3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkPresignedService {

    private final AmazonS3Util amazonS3Util;
    private final S3PathGenerator s3PathGenerator;

    @Value("${cdn.base-url}")
    private String cdnBaseUrl;

    public byte[] confirmAndExport(String category, List<BatchConfirmParam> items) {
        CategoryType categoryType = CategoryType.getStorageTypeByCategory(category);
        if (categoryType == CategoryType.UNKNOWN) {
            throw new InvalidParameterException(ErrorCode.INVALID_CATEGORY);
        }

        String bucketName = s3PathGenerator.getBucketName(categoryType);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Upload Result");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("origin");
            header.createCell(1).setCellValue("cdnUrl");
            header.createCell(2).setCellValue("fileKey");
            header.createCell(3).setCellValue("verified");
            header.createCell(4).setCellValue("error");

            for (int i = 0; i < items.size(); i++) {
                BatchConfirmParam item = items.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getOrigin() != null ? item.getOrigin() : "");

                String keyUrl = item.getKeyUrl();
                if (keyUrl == null || keyUrl.isBlank()) {
                    row.createCell(1).setCellValue("");
                    row.createCell(2).setCellValue("");
                    row.createCell(3).setCellValue("X");
                    row.createCell(4).setCellValue("클라이언트 업로드 실패");
                } else {
                    String fullUrl = cdnBaseUrl + "/" + category + "/" + keyUrl;
                    boolean exists = amazonS3Util.existsObject(bucketName, keyUrl);
                    row.createCell(1).setCellValue(fullUrl);
                    row.createCell(2).setCellValue(keyUrl);
                    row.createCell(3).setCellValue(exists ? "O" : "X");
                    row.createCell(4).setCellValue(exists ? "" : "S3에 파일 없음");
                }
            }

            sheet.setColumnWidth(0, 256 * 30);
            sheet.setColumnWidth(1, 256 * 70);
            sheet.setColumnWidth(2, 256 * 50);
            sheet.setColumnWidth(3, 256 * 10);
            sheet.setColumnWidth(4, 256 * 25);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (RestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate confirm Excel: {}", e.getMessage());
            throw new RestException(ErrorCode.FILE_IO_EXCEPTION);
        }
    }
}
