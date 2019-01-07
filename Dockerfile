FROM primetoninc/jdk:1.8

RUN ulimit -n 65536
RUN mkdir -p /opt/stack

COPY target/spring-boot-config-sample-1.0.0-SNAPSHOT.jar /opt/stack/
COPY ./conf.properties /opt/stack/

EXPOSE 8080

WORKDIR /opt/stack

ENV message=bonjour

ENTRYPOINT ["java", \
    "-jar", \
    "spring-boot-config-sample-1.0.0-SNAPSHOT.jar", \
    "--spring.config.name=application,conf"]

HEALTHCHECK --interval=30s --timeout=10s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
