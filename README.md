# API BASIC Project

## Overview
- FILE Proxy API

## 사용된 기술 스택
- Java 21
- Spring Boot 4.0.6
- Gradle 8.14
- Redis
- AWS SDK S3
- Auth0 JWT`n- Lombok (1.18.x)
- JUnit5

## 실행 방법

### 사전 준비

#### JAVA 설치

- Windows : https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/what-is-corretto-21.html
- MacOS : https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/macos-install.html

#### Gradle 설치
- 실행한 버전 8.X
- install guide :  https://gradle.org/install/
- releases : https://gradle.org/releases/

#### Docker 설치
- Windows : https://docs.docker.com/desktop/install/windows-install/
- MacOS : https://docs.docker.com/desktop/install/mac-install/

#### IntelliJ 설치
- install guide : https://www.jetbrains.com/help/idea/installation-guide.html
- download : https://www.jetbrains.com/idea/download

### 실행

1. 압축을 해제후 IntelliJ를 통해 프로젝트를 엽니다.
2. `File - Project Structure - prject`의 SDK가 올바르게 설정되어 있는지 확인합니다.
3. `File - Settings - Build, Execution, Deployment - Build Tools - Gradle`의 USER_HOME의 PATH가 gradle 설치 경로를 올바르게 설정되어 있는지
   확인합니다.
4. 이 프로젝트는 lombock을 사용하고 있습니다.
   `File - Settings - Build, Execution, Deployment - Compiler - Annotation Processors` 항목으로 이동
   후 `Enable annotation processing`이 체크되어 있는지 확인합니다.
5. gradle window로 이동 [Reload all gradle project] 버튼을 실행하던가 build 명령어를 실행합니다.
    - gradle build 실행 : `gradle build` or `./gradlew build`
6. 로컬에서 실행할 경우 도커를 통해 redis 실행
    - IntelliJ 를 통한 실행 : https://www.jetbrains.com/help/idea/docker-compose.html
    - docker 명령어 실행
   ```
     $ docker-compose -f docker-compose.yml up -d 
   ```
    - docker 명령어 종료
   ```
     $ docker-compose -f docker-compose.yml down
   ```
7. Application 을 실행 한다.

## API Docs

### Admin Bulk Upload

- Endpoint: `POST /file/mng/v1/{adminKey}/upload/bulk/{category}/{type}`
- Content-Type: `multipart/form-data`
- Request Part: `file` (`.xlsx`만 허용)
- Response Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- 인증: 기존 adminKey path variable 검증과 동일

#### Request Example

```bash
curl -X POST \
  "http://localhost:38082/file/mng/v1/{adminKey}/upload/bulk/resource/image" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@bulk-upload.xlsx"
```

#### Input Excel Format

| A (url) |
| --- |
| https://example.com/image1.png |
| https://example.com/doc.pdf |

#### Output Excel Format

성공:

| A (url) | B (error) | C (originalFileName) | D (s3Path) |
| --- | --- | --- | --- |
| https://example.com/image1.png |  | image1.png | image/20260312/uuid.png |
| https://example.com/doc.pdf |  | doc.pdf | none/20260312/uuid.pdf |

실패 포함:

| A (url) | B (error) | C (originalFileName) | D (s3Path) |
| --- | --- | --- | --- |
| ftp://invalid/url | http/https URL만 가능 |  |  |
| https://fail.com/missing.png | HTTP 404 | missing.png |  |
| https://example.com/image1.png |  | image1.png | image/20260312/uuid.png |

#### Processing Rules

1. 업로드된 파일은 `XSSFWorkbook`으로 읽는다. `.xls`는 지원하지 않는다.
2. 첫 번째 시트의 첫 행 A열 문자열에 `url`, `링크`, `주소` 중 하나가 포함되면 헤더로 간주한다.
3. 헤더가 있으면 1행에 `error`, `originalFileName`, `s3Path` 컬럼을 채우고 2행부터 처리한다.
4. 헤더가 없으면 기존 행을 아래로 한 줄 밀고 1행에 `url`, `error`, `originalFileName`, `s3Path` 헤더를 생성한 뒤 2행부터 처리한다.
5. 각 행의 A열 값이 비어 있으면 해당 행은 건너뛴다.
6. URL 문자열이 `http://` 또는 `https://`로 시작하지 않으면 B열에 `http/https URL만 가능`을 기록하고 다음 행으로 진행한다.
7. 다운로드는 Java `HttpClient`를 사용한다.
8. `connectTimeout`은 30초, 요청별 timeout은 60초, redirect 정책은 `HttpClient.Redirect.NORMAL`이다.
9. HTTP 응답 코드가 `200`이 아니면 B열에 `HTTP {status}` 형식으로 기록한다.
10. URL path에서 파일명을 추출하고 `URLDecoder`로 decode한 값을 C열에 기록한다. 추출 실패 시 `unknown_file`을 사용한다.
11. S3 업로드 경로는 `{type}/{yyyyMMdd}/{uuid}.{ext}` 형식으로 생성된다. bucket/path prefix는 기존 `category`, `type` 규칙을 그대로 따른다.
12. 성공 시 B열은 빈 값, C열은 원본 파일명, D열은 S3 path를 기록한다.
13. 개별 행 처리 중 오류가 발생해도 전체 요청은 중단하지 않고 다음 행을 계속 처리한다.
14. 처리 가능한 데이터 행이 하나도 없으면 `FILE_NOT_ATTACHED` 예외를 반환한다.

#### Error Response

- 잘못된 `adminKey`: `INVALID_REQUEST_FILE_PATH`
- 파일 미첨부 또는 처리 가능한 데이터 행 없음: `FILE_NOT_ATTACHED`
- 업로드 파일명이 없거나 `.xlsx`가 아님: `INVALID_FILE_NAME`
- 파일 읽기 중 `IOException`: `FILE_IO_EXCEPTION`

#### Current Caveats

- 현재 URL 검증은 `http`/`https` scheme prefix만 확인한다. 내부망 주소 차단, DNS/IP 검증, redirect 후 목적지 검증은 구현되어 있지 않다.
- redirect는 자동으로 따라간다. 따라서 SSRF 방어가 완전하지 않다.
- 실패 행 처리 시 B열과 C열은 갱신되지만, 기존 결과 파일을 재업로드한 경우 일부 실패 행의 D열 값이 남을 수 있다.
- `file` 이름이 `.xlsx`여도 실제 내용이 손상된 OOXML이거나 다른 포맷이면 500 계열 예외로 표면화될 수 있다.
- `category` 또는 `type` 값 검증은 기존 파일 업로드 API와 동일한 로직을 사용한다. `type`이 미지원 값이면 별도 400 없이 `unknown/...` prefix로 저장될 수 있다.
- 현재 이 API에 대한 전용 자동화 테스트는 없다. 프로젝트 테스트는 컨텍스트 로드 수준만 존재한다.
