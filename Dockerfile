FROM openjdk:11-jdk-slim

# create a temp dir in which to work
RUN OLDDIR="$PWD"
COPY aws/rds-ca-2019-root.pem /tmp/rds-ca/rds-certificate.pem

# split the bundle into individual certs (prefixed with xx)
RUN csplit -sz /tmp/rds-ca/rds-certificate.pem '/-BEGIN CERTIFICATE-/' '{*}'

# import each cert individually
RUN for CERT in xx*; do keytool -import -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -alias rds$CERT -file "$CERT"; done

# back out of the temp dir and delete it
RUN cd "$OLDDIR"
RUN rm -r /tmp/rds-ca

# list the imported rds certs as a sanity check
RUN keytool -list -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt | grep -i rds

COPY target/smokefree-initiative-service*.jar smokefree-initiative-service.jar
CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar smokefree-initiative-service.jar
