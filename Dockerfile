FROM openjdk:11-jdk-slim

# create a temp dir in which to work
RUN OLDDIR="$PWD"
COPY aws/rds-combined-ca-bundle.pem /tmp/rds-ca/rds-combined-ca-bundle.pem

# split the bundle into individual certs (prefixed with xx)
RUN csplit -sz /tmp/rds-ca/rds-combined-ca-bundle.pem '/-BEGIN CERTIFICATE-/' '{*}'

# import each cert individually
RUN for CERT in xx*; do keytool -import -keystore /etc/ssl/certs/java/cacerts -storepass changeit -noprompt -alias rds$CERT -file "$CERT"; done

# back out of the temp dir and delete it
RUN cd "$OLDDIR"
RUN rm -r /tmp/rds-ca

# list the imported rds certs as a sanity check
RUN keytool -list -keystore /etc/ssl/certs/java/cacerts -storepass changeit -noprompt | grep -i rds

COPY target/smokefree-initiative-service*.jar smokefree-initiative-service.jar
CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar smokefree-initiative-service.jar