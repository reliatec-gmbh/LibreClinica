FROM docker.io/library/maven:3-eclipse-temurin-8 AS builder

WORKDIR /app

COPY . .

RUN \
    # cache downloaded dependencies
    --mount=type=cache,target=/root/.m2 \
    # build cache
    --mount=type=cache,target=/app/core/target \
    --mount=type=cache,target=/app/docs/target \
    --mount=type=cache,target=/app/odm/target \
    --mount=type=cache,target=/app/web/target \
    --mount=type=cache,target=/app/ws/target \

    set -eux; \
    mvn package; \
    mv web/target/LibreClinica-web.war /;

############################################################
FROM tomcat:9-jdk11

RUN set -eux; \
    # set up redirection to application when accessing tomcat root
    mkdir /usr/local/tomcat/webapps/ROOT; \
    echo '<html><head><meta http-equiv="refresh" content="0; URL=LibreClinica/" /></head></html>' \
        > /usr/local/tomcat/webapps/ROOT/index.html;

# set up volumes for data and logs
VOLUME \
    /usr/local/tomcat/libreclinica.data \
    /usr/local/tomcat/logs

# add config files
COPY \
    /docker/config/ \
    /usr/local/tomcat/libreclinica.config/

# add libre-clinica war file
COPY --from=builder \
    /LibreClinica-web.war \
    /usr/local/tomcat/webapps/LibreClinica.war
