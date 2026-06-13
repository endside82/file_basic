# S3 Module 버킷 및 Static FileType 추가 리포트

- 작성일: 2026-03-16
- 브랜치: dev

---

## 1. 개요

S3에 static 파일(html, js, css 등)을 업로드하기 위한 `MODULE` 카테고리와 `STATIC` FileType을 추가했다.
S3 경로에 날짜 prefix(`yyyyMMdd`)를 적용하여 일자별로 파일을 구분할 수 있도록 했다.

---

## 2. 변경 파일 목록

| 파일 | 변경 내용 |
|------|----------|
| `CategoryType.java` | `MODULE("module")` enum 값 및 switch case 추가 |
| `FileType.java` | `STATIC(4, "static")` enum 값 및 switch case 추가 |
| `S3PathGenerator.java` | `amazonS3ModuleBucketName` 필드 추가, `getBucketName()` / `getS3PathInfo()` / `generateS3Path()` 에 MODULE case 추가 |
| `S3PathValidator.java` | MODULE 카테고리 path validation 패턴(`\w+/\d{8}/.+`) 추가 |
| `application-default.yml` | `amazon.s3.module.name: your-module-bucket` 추가 |
| `application-compose.yml` | `amazon.s3.module.name: your-module-bucket` 추가 |

---

## 3. 상세 변경 내용

### 3.1 CategoryType.java

```java
MODULE("module")
```

- enum 값 추가
- `getStorageTypeByCategory()` switch에 `"module" -> MODULE` 매핑 추가

### 3.2 FileType.java

```java
STATIC(4, "static")
```

- enum 값 추가
- `getFileTypeByTypePath()` switch에 `"static" -> STATIC` 매핑 추가
- html, js, css 등 static 파일 업로드 시 사용

### 3.3 S3PathGenerator.java

- `@Value("${amazon.s3.module.name}")` 으로 버킷명 주입
- `getBucketName()`: MODULE -> `amazonS3ModuleBucketName` 반환
- `getS3PathInfo()`: MODULE은 `appPathSimpleDate()`를 사용하여 날짜 prefix 적용
- `generateS3Path()`: `typePath + "/" + addPath` 형식으로 경로 생성

### 3.4 S3PathValidator.java

- MODULE path 패턴: `\w+/\d{8}/.+`
- 예시 매칭: `static/20260316/uuid.js`

### 3.5 application-default.yml / application-compose.yml

```yaml
amazon:
  s3:
    module:
      name: your-module-bucket
```

---

## 4. S3 경로 구조

```
your-module-bucket/static/20260316/{uuid}.{ext}
```

| 구성 요소 | 값 | 설명 |
|----------|---|------|
| 버킷 | `your-module-bucket` | MODULE 전용 S3 버킷 |
| typePath | `static` | FileType.STATIC의 typeString |
| addPath | `20260316/` | 업로드 일자 (yyyyMMdd) |
| 파일명 | `{uuid}.{ext}` | UUID 기반 자동 생성 |

---

## 5. API 사용 방법

### 일반 사용자 업로드

```
POST /file/mng/v1/upload/module/static
Content-Type: multipart/form-data

file: (MultipartFile)
```

### Admin 업로드

```
POST /file/mng/v1/{adminKey}/upload/module/static
Content-Type: multipart/form-data

file: (MultipartFile)
```

### Admin 다운로드

```
GET /file/mng/v1/{adminKey}/download/module/static?path=static/20260316/{uuid}.js
```

### Admin 벌크 업로드

```
POST /file/mng/v1/{adminKey}/upload/bulk/module/static
Content-Type: multipart/form-data

file: (xlsx)
```

### Presigned URL 발급

```
GET /file/mng/v1/signed/upload/{category}/{type}?formatName=js
```

---

## 6. Content-Type 지원

`AmazonS3Util.CONTENT_TYPE_MAP`에 이미 등록된 static 파일 타입:

| 확장자 | Content-Type |
|--------|-------------|
| html | `text/html` |
| css | `text/css` |
| js | `application/javascript` |
| json | `application/json` |
| xml | `application/xml` |
| txt | `text/plain` |

---

## 7. 검증 결과

- 컴파일: BUILD SUCCESSFUL
- 테스트: BUILD SUCCESSFUL (4 tasks executed)
