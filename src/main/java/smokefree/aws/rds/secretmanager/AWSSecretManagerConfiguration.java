package smokefree.aws.rds.secretmanager;

import javax.inject.Singleton;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;

/**
 * AWS Secret Manager related Configuration, creation of required beans in order to connect to AWS goes here.
 *
 */
@Factory
public class AWSSecretManagerConfiguration {


	/**
	 * AWS Region where Secrets Manager is running.
	 */
	@Value("${secret.region}")
	private String secretRegion;

	/**
	 * AWS Secret Manager Client API
	 * @return
	 */
	@Singleton
	public AWSSecretsManager awsSecretManager() {
		return AWSSecretsManagerClientBuilder.standard()
                .withRegion(secretRegion)
                .build();
	}



}
