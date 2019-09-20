package io.localmotion.configuration;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;


@MicronautTest(propertySources = {"classpath:application-configuration.yaml"})
class CognitoSettingsTest {

	@Inject
	CognitoSettings cognitoSettings;

	@Test
	void getRegion() {
		Assertions.assertEquals("eu-west-2", cognitoSettings.getRegion());
	}

	@Test
	void getUserPoolId() {
		Assertions.assertEquals("eu-west-1_WsTxYUHyC", cognitoSettings.getUserPoolId());
	}

	@Test
	void getUserPoolWebClientId() {
		Assertions.assertEquals("3nkh1qomocr39s893jf0dp44cd", cognitoSettings.getUserPoolWebClientId());
	}

}
