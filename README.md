# File Basic Project

## Overview

- 기본 파일 프록시 API 프로젝트입니다.
- S3 presigned URL 발급, 파일 접근, 관리자 업로드, Excel 기반 bulk upload의 기본 구조를 제공합니다.
- `E:\00.source\firsthabit\chalk_file`의 기능을 기본 프로젝트 패키지와 Spring Boot 4.0.6 기준으로 정리했습니다.

## 기술 스택

- Java 21
- Spring Boot 4.0.6
- Gradle Wrapper 8.14
- Spring Web, Web Services, Security
- Redis
- AWS SDK S3
- Auth0 JWT
- Apache POI
- Lombok 1.18.x
- JUnit 5

## 프로젝트 정보

- Root project: `file`
- Group: `com.endside.file`
- 기본 포트: `38082`
- 주요 프로파일: `default`, `dev`, `compose`, `stg-compose`
- 로컬 Docker 구성: Redis

## 실행 준비

### Java

- Corretto 21 또는 Java 21 호환 JDK를 설치합니다.
- Windows: https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/what-is-corretto-21.html
- macOS: https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/macos-install.html

### Docker

- Docker Desktop을 설치합니다.
- Windows: https://docs.docker.com/desktop/install/windows-install/
- macOS: https://docs.docker.com/desktop/install/mac-install/

### IntelliJ

- Annotation Processing을 활성화합니다.
- `File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors`
- `Enable annotation processing` 체크

## 로컬 실행

```bash
./gradlew bootRun
```

Windows:

```bat
gradlew.bat bootRun
```

Docker Compose를 직접 실행할 경우:

```bash
docker compose up -d
docker compose down
```

## 빌드 및 검증

```bash
./gradlew clean build
./gradlew build -x test --warning-mode all
./gradlew testClasses
```

Windows:

```bat
gradlew.bat clean build
gradlew.bat build -x test --warning-mode all
gradlew.bat testClasses
```

## 설정

기본 설정 파일은 `src/main/resources` 아래에 있습니다.

- `application-default.yml`: 로컬 기본 실행 설정
- `application-dev.yml`: 개발 환경 설정
- `application-compose.yml`: Docker Compose 기반 로컬 실행 설정
- `application-stg-compose.yml`: staging compose 실행 설정

S3 bucket, CDN, JWT, Redis 값은 환경 변수 placeholder를 기준으로 관리합니다. 새 프로젝트를 시작할 때 실제 운영 비밀값을 커밋하지 않습니다.

## Admin Bulk Upload

관리자 bulk upload API는 Excel 파일에 담긴 URL 목록을 읽어 원본 파일을 내려받고 S3에 업로드한 뒤, 처리 결과를 Excel로 반환합니다.

- Endpoint: `POST /file/mng/v1/{adminKey}/upload/bulk/{category}/{type}`
- Content-Type: `multipart/form-data`
- Request part: `file` (`.xlsx`)
- Response Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- 인증: 기존 `adminKey` path variable 검증 사용

### Request Example

```bash
curl -X POST \
  "http://localhost:38082/file/mng/v1/{adminKey}/upload/bulk/resource/image" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@bulk-upload.xlsx"
```

### Input Excel Format

| A (url) |
| --- |
| https://example.com/image1.png |
| https://example.com/doc.pdf |

### Output Excel Format

성공:

| A (url) | B (error) | C (originalFileName) | D (s3Path) |
| --- | --- | --- | --- |
| https://example.com/image1.png |  | image1.png | image/20260312/uuid.png |
| https://example.com/doc.pdf |  | doc.pdf | none/20260312/uuid.pdf |

실패 포함:

| A (url) | B (error) | C (originalFileName) | D (s3Path) |
| --- | --- | --- | --- |
| ftp://invalid/url | http/https URL만 가능합니다 |  |  |
| https://fail.com/missing.png | HTTP 404 | missing.png |  |
| https://example.com/image1.png |  | image1.png | image/20260312/uuid.png |

### Processing Rules

1. 업로드 파일은 `XSSFWorkbook`으로 읽습니다. `.xls`는 지원하지 않습니다.
2. 첫 번째 시트의 첫 행 A열 문자열에 `url`, `링크`, `주소` 중 하나가 포함되면 헤더로 간주합니다.
3. 헤더가 있으면 1행에 `error`, `originalFileName`, `s3Path` 컬럼을 채우고 2행부터 처리합니다.
4. 헤더가 없으면 기존 행을 아래로 한 줄 밀고 1행에 `url`, `error`, `originalFileName`, `s3Path` 헤더를 생성한 뒤 2행부터 처리합니다.
5. 각 행의 A열 값이 비어 있으면 해당 행은 건너뜁니다.
6. URL이 `http://` 또는 `https://`로 시작하지 않으면 B열에 오류를 기록하고 다음 행으로 진행합니다.
7. 다운로드는 Java `HttpClient`를 사용합니다.
8. connect timeout은 30초, 요청 timeout은 60초, redirect 정책은 `HttpClient.Redirect.NORMAL`입니다.
9. HTTP 응답 코드가 `200`이 아니면 B열에 `HTTP {status}` 형식으로 기록합니다.
10. URL path에서 파일명을 추출하고 URL decode한 값을 C열에 기록합니다. 추출 실패 시 `unknown_file`을 사용합니다.
11. S3 업로드 경로는 `{type}/{yyyyMMdd}/{uuid}.{ext}` 형식으로 생성합니다.
12. 성공 시 B열은 비워 두고, C열은 원본 파일명, D열은 S3 path를 기록합니다.
13. 개별 행 처리 중 오류가 발생해도 전체 요청은 중단하지 않고 다음 행을 계속 처리합니다.
14. 처리 가능한 데이터 행이 하나도 없으면 `FILE_NOT_ATTACHED` 예외를 반환합니다.

### Error Response

- 잘못된 `adminKey`: `INVALID_REQUEST_FILE_PATH`
- 파일 미첨부 또는 처리 가능한 데이터 없음: `FILE_NOT_ATTACHED`
- 업로드 파일명이 없거나 `.xlsx`가 아님: `INVALID_FILE_NAME`
- 파일 읽기 중 `IOException`: `FILE_IO_EXCEPTION`

### Current Caveats

- URL 검증은 현재 `http`/`https` scheme prefix만 확인합니다.
- redirect는 자동으로 따라가며, redirect 목적지에 대한 별도 SSRF 방어는 구현되어 있지 않습니다.
- 실패 행 처리 중 B열과 C열은 갱신하지만, 기존 결과 파일을 재업로드한 경우 일부 실패 행의 D열 값이 남을 수 있습니다.
- 파일 이름이 `.xlsx`여도 실제 내용이 정상 OOXML이 아니면 500 계열 예외로 표면화될 수 있습니다.
- `category`와 `type` 검증은 기존 파일 업로드 API와 동일한 로직을 사용합니다.
- bulk upload API 전용 자동화 테스트는 아직 없습니다.
