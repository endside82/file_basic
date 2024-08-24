FROM public.ecr.aws/docker/library/amazoncorretto:21-alpine-jdk
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG DEPENDENCY=build/dependency
WORKDIR app
COPY ${DEPENDENCY}/BOOT-INF/classes BOOT-INF/classes
COPY ${DEPENDENCY}/BOOT-INF/lib BOOT-INF/lib
COPY ${DEPENDENCY}/META-INF META-INF
COPY ${DEPENDENCY}/org org
ARG SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE
ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS
ENTRYPOINT java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher
