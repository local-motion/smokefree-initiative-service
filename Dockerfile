FROM openjdk:11-jdk-slim
COPY target/smokefree-initiative-service*.jar smokefree-initiative-service.jar
CMD java ${JAVA_OPTS} -jar smokefree-initiative-service.jar