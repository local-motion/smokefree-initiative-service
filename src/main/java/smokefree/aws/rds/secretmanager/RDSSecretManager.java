package smokefree.aws.rds.secretmanager;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
@Named("SecretManager")
public class RDSSecretManager {



	@Value("${secret.name}")
	private String secretName;

	@Value("${aws.rds.jdbcdriverclass}")
	private String driverClass;

	@Inject
	ObjectMapper objectMapper;

	@Inject
	AWSSecretsManager secretManagerClient;

	private Map<String, String> secretMap;

	private Map<String, String> getRDSDetails() {
		if(secretMap == null) {
			String secret, decodedBinarySecret;
		    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
		                    .withSecretId(secretName);
		    GetSecretValueResult getSecretValueResult = null;

		    try {
			log.info("Connecting AWS Secret Mananger " + getSecretValueRequest.getSecretId());
		        getSecretValueResult = secretManagerClient.getSecretValue(getSecretValueRequest);
		        log.info("Application fetched secrets successfully");
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
		    if (getSecretValueResult.getSecretString() != null) {
		        secret = getSecretValueResult.getSecretString();
		        try {
					this.secretMap = objectMapper.readValue(secret, HashMap.class);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Error while Parsing the {}  JSON", secret);
					throw new SecretManagerException(e.getMessage(), e);
				}
		    }
		    else {
		        decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
		        try {
					this.secretMap = objectMapper.readValue(decodedBinarySecret, HashMap.class);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error("Error while Parsing the {}  JSON", decodedBinarySecret);
					throw new SecretManagerException(e.getMessage(), e);
				}
		    }
		}

		return this.secretMap;
	}


	public String getJDBCurl() {
		String completeJDBCUrl = null;
		if(!didSecretFetch()) {
			this.getRDSDetails();
		}
		StringBuilder jdbcUrl = new StringBuilder("jdbc");
		jdbcUrl.append(SmokefreeConstants.COLON);
		jdbcUrl.append(secretMap.get(SmokefreeConstants.DB_ENGINE)).append(SmokefreeConstants.COLON).append(SmokefreeConstants.DOUBLE_SLASH);
		jdbcUrl.append(secretMap.get(SmokefreeConstants.DB_HOST)).append(SmokefreeConstants.COLON);
		jdbcUrl.append(secretMap.get(SmokefreeConstants.DB_PORT)).append(SmokefreeConstants.SINGLE_SLASH);
		jdbcUrl.append(secretMap.get(SmokefreeConstants.DBNAME));
		completeJDBCUrl =  jdbcUrl.toString();
		log.info("Formatted JDBC URL is {}", completeJDBCUrl);
		return completeJDBCUrl;
	}

	public String getUsername() {

		return didSecretFetch()? secretMap.get(SmokefreeConstants.DB_USERNAME) : getRDSDetails().get(SmokefreeConstants.DB_USERNAME);
	}

	public String getPassword() {
		return didSecretFetch() ? secretMap.get(SmokefreeConstants.DB_PASSWORD) : getRDSDetails().get(SmokefreeConstants.DB_PASSWORD);
	}
	public String getJDBCDriverClass() {
		return driverClass;
	}

	private boolean didSecretFetch() {
		return this.secretMap != null;
	}



}
