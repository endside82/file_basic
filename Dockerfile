FROM bellsoft/liberica-openjre-alpine:21 AS builder
WORKDIR /builder
ARG JAR_FILE=build/libs/file-*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=tools -jar application.jar \
        extract --layers --application-filename application.jar --destination extracted

FROM bellsoft/liberica-openjre-alpine:21
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /application

COPY --from=builder --chown=spring:spring /builder/extracted/dependencies/ ./
COPY --from=builder --chown=spring:spring /builder/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /builder/extracted/application/ ./

ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE
ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
EXPOSE 38082

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar application.jar"]
