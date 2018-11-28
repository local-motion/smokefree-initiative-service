package smokefree.aws.rds.secretmanager;

import javax.inject.Singleton;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;

@Factory
public class AWSSecretManagerConfiguration {


	@Value("${secret.region}")
	private String secretRegion;

	@Singleton
	public AWSSecretsManager awsSecretManager() {
		return AWSSecretsManagerClientBuilder.standard()
                .withRegion(secretRegion)
                .build();
	}



}
