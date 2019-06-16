package io.localmotion.email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Value;

@Factory
public class AmazonSimpleEmailServiceFactory {


	@Value("${aws.ses.region}")
	String region;

	@Context
	public AmazonSimpleEmailService amazonSimpleEmailServiceClient() {
		return AmazonSimpleEmailServiceClientBuilder
				.standard()
				.withRegion(region)
				.build();
	}


	@Context
	@Primary
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
