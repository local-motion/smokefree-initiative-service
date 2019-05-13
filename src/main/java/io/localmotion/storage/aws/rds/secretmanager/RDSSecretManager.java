package io.localmotion.storage.aws.rds.secretmanager;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Singleton
public class RDSSecretManager {
    @Value("${secret.name}")
    private String secretName;

    @Value("${aws.rds.jdbcdriverclass}")
    private String driverClass;

    @Value("${aws.rds.enable-ssl:true}")
    private boolean isSslEnabled;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    AWSSecretsManager secretManagerClient;

    private Map<String, Object> secretMap;

    private Map<String, Object> getRDSDetails() {
        if (secretMap == null) {
            GetSecretValueResult getSecretValueResult;
            GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                    .withSecretId(secretName);

            try {
                log.info("Connecting AWS Secret Manager {}", getSecretValueRequest.getSecretId());
                getSecretValueResult = secretManagerClient.getSecretValue(getSecretValueRequest);
                log.trace("Application fetched secrets successfully: {}", getSecretValueResult);
            } catch (DecryptionFailureException e) {
                log.error("Secrets Manager can't decrypt the protected secret text using the provided KMS key", e);
                throw new SecretManagerException(e.getMessage(), e);
            } catch (InternalServiceErrorException e) {
                log.error("An error occurred on the secret manager server side");
                throw new SecretManagerException(e.getMessage(), e);
            } catch (InvalidParameterException e) {
                log.error("You provided an invalid value for a parameter", e);
                throw new SecretManagerException(e.getMessage(), e);
            } catch (InvalidRequestException e) {
                log.error("You provided a parameter value that is not valid for the current state of the resource", e);
                throw new SecretManagerException(e.getMessage(), e);
            } catch (ResourceNotFoundException e) {
                log.error("Can't find the resource that you asked for");
                throw new SecretManagerException(e.getMessage(), e);
            }

            // Decrypts secret using the associated KMS CMK.
            // Depending on whether the secret is a string or binary, one of these fields will be populated.
            String secret = getSecretValueResult.getSecretString() != null
                    ? getSecretValueResult.getSecretString()
                    : new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
            this.secretMap = readSecretMap(secret);
        }

        return this.secretMap;
    }

    private Map<String, Object> readSecretMap(String secret) {
        try {
            //noinspection unchecked
            Map<String, Object> secretMap = objectMapper.readValue(secret, HashMap.class);
            log.trace("Parsed secrets from: {}", secretMap);
            return secretMap;
        } catch (IOException e) {
            log.error("Error while parsing JSON {}", secret);
            throw new SecretManagerException(e.getMessage(), e);
        }
    }


    public String getJDBCurl() {
        if (!didSecretFetch()) {
            this.getRDSDetails();
        }
        StringBuilder jdbcUrl = new StringBuilder("jdbc");
        jdbcUrl.append(SmokefreeConstants.COLON);
        jdbcUrl.append(secretMap.get(SmokefreeConstants.DB_ENGINE)).append(SmokefreeConstants.COLON).append(SmokefreeConstants.DOUBLE_SLASH);
        jdbcUrl.append(secretMap.get(SmokefreeConstants.DB_HOST)).append(SmokefreeConstants.COLON);
        jdbcUrl.append(secretMap.get(SmokefreeConstants.DB_PORT)).append(SmokefreeConstants.SINGLE_SLASH);
        jdbcUrl.append(secretMap.get(SmokefreeConstants.DBNAME));
        if (isSslEnabled) {
            jdbcUrl.append("?verifyServerCertificate=true&useSSL=true");
        }
        String completeJdbcUrl = jdbcUrl.toString();
        log.info("Formatted JDBC URL is {}", completeJdbcUrl);
        return completeJdbcUrl;
    }

    public String getUsername() {

        return didSecretFetch() ? secretMap.get(SmokefreeConstants.DB_USERNAME).toString() : getRDSDetails().get(SmokefreeConstants.DB_USERNAME).toString();
    }

    public String getPassword() {
        return didSecretFetch() ? secretMap.get(SmokefreeConstants.DB_PASSWORD).toString() : getRDSDetails().get(SmokefreeConstants.DB_PASSWORD).toString();
    }

    public String getJDBCDriverClass() {
        return driverClass;
    }

    private boolean didSecretFetch() {
        return this.secretMap != null;
    }


}
