FROM maven:3.5.0-jdk-8 as builder
LABEL maintainer="Lucio M. <lucioric2000@hotmail.com>"
MAINTAINER   Lucio M. <lucioric2000@hotmail.com>

FROM tomcat:9-jdk8-openjdk
LABEL maintainer="Lucio M. <lucioric2000@hotmail.com>"
MAINTAINER   Lucio M. <lucioric2000@hotmail.com>
WORKDIR /libreclinica

#Environment variables
ENV M2_HOME='/usr/share/maven'
ENV PATH="$M2_HOME/bin:$PATH"
#ARG ADMINEMAIL
ARG ENVIRONMENT
#obtains Maven for this image
COPY --from=builder $M2_HOME $M2_HOME
RUN rm -rf /root/.m2/*

#installs using Maven
COPY . .
COPY docker/datainfo_docker_${ENVIRONMENT}.properties /libreclinica/core/src/main/resources/org/akaza/openclinica/datainfo.properties
COPY docker/datainfo_docker_${ENVIRONMENT}.properties /libreclinica/web/src/main/resources/org/akaza/openclinica/datainfo.properties
COPY docker/datainfo_docker_${ENVIRONMENT}.properties /libreclinica/ws/src/main/filters/datainfo.properties
COPY docker/datainfo_docker_${ENVIRONMENT}.properties /libreclinica/datainfo.properties
COPY docker/datainfo_docker_${ENVIRONMENT}.properties /usr/local/tomcat/libreclinica.config/datainfo.properties
#RUN find /libreclinica -type f -name "*.war"
#RUN mvn -B clean install -DskipTests

# /SampleWebApp
COPY SampleWebApp.war /usr/local/tomcat/webapps/SampleWebApp.war
COPY docker/index_${ENVIRONMENT}.html  /usr/local/tomcat/webapps/ROOT/index.html
COPY docker/web.xml /usr/local/tomcat/webapps/ROOT/WEB-INF/web.xml
COPY tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml
COPY docker/manager_context.xml /usr/local/tomcat/webapps/manager/META-INF/context.xml

#COPY --from=builder /libreclinica/ws/target/LibreClinica-ws-1.3.1.war /usr/local/tomcat/webapps/LibreClinica-ws-1.3.1.war

RUN env
#RUN mvn -B clean install -DskipTests|tee /libreclinica/build.log
#COPY --from=builder /libreclinica/web/target/LibreClinica-web-1.3.1.war  /usr/local/tomcat/webapps/LibreClinica.war
