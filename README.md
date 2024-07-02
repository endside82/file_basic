# API BASIC Project

## Overview
- FILE Proxy API

## 사용된 기술 스택
- JAVA21
- Spring boot 3.3.X
- Gradle 8.5
- JPA with QueryDSL and flyway
- mysql 8.x
- JUnit5

## 실행 방법

### 사전 준비

#### JAVA 설치

- Windows : https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/what-is-corretto-21.html
- MacOS : https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/macos-install.html

#### Gradle 설치
- 실행한 버전 8.5
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
6. 로컬에서 실행할 경우 도커를 통해 mysql 실행
    - IntelliJ 를 통한 실행 : https://www.jetbrains.com/help/idea/docker-compose.html
    - docker 명령어 실행
   ```
     $ docker-compose -f docker-compose.yml up -d 
   ```
    - docker 명령어 종료
   ```
     $ docker-compose -f docker-compose.yml down
   ```
   - 로컬에 바인딩 되는 볼륨의 경로를 변경하고 싶으면 docker-compose.yml 파일 중 volume을 수정한다.
   ```
     {로컬경로}:/var/lib/mysql
   ```
   - 현재 spring-boot-docker-compose로 인하여 어플리케이션 실행시 자동으로 실행된다.
7. Application 을 실행 한다.