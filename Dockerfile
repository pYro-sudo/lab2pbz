FROM registry.access.redhat.com/ubi9/openjdk-21:1.23

ENV LANGUAGE='en_US:en'

USER root
RUN mkdir -p /deployments && chown 185:0 /deployments
COPY --chown=185:0 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185:0 target/quarkus-app/*.jar /deployments/
COPY --chown=185:0 target/quarkus-app/app/ /deployments/app/
COPY --chown=185:0 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
ENV JAVA_LIB_DIR="/deployments/lib"

ENV APP_AUTH_USERNAME=admin
ENV APP_AUTH_PASSWORD=password

CMD ["java", "-jar", "/deployments/quarkus-run.jar"]