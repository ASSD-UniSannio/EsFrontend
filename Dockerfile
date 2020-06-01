FROM alpine:latest
MAINTAINER woland7

# Install Java
RUN apk --no-cache add openjdk11-jre

ENV JAVA_HOME /usr/lib/jvm/default-jvm

# Set Unlimited Strength Jurisdiction Policy Files
COPY UnlimitedJCEPolicyJDK8/ $JAVA_HOME/jre/lib/security/

RUN mkdir /app \
    && cd /app

WORKDIR /app

ADD target/1.0-jar-with-dependencies.jar exgrizzly.jar

ENV APP_HOME /app

ENV PATH $APP_HOME/bin:$PATH

ENV ACCHOST "192.168.99.100"

ENV CUSHOST "192.168.99.100"

EXPOSE 8079

RUN chmod 777 -R /app

CMD ["java", "-jar", "exgrizzly.jar"]