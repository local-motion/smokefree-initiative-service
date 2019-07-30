package io.localmotion.configuration;

import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest(propertySources = {"classpath:application-configuration.yaml"})
class ConfigurationSettingsTest {

	@Inject
	ConfigurationSettings configurationSettings;

	@Inject
	CloudUserManagement cognitoUserPool;

	@Test
	void getLogicalEnvironment() {
		Assertions.assertEquals(LogicalEnvironment.Test, configurationSettings.getLogicalEnvironment());
	}

	@Test
	void getCognitoSettings() {
		when(cognitoUserPool.getRegion()).thenReturn("eu-west-3");
		when(cognitoUserPool.getUserPoolId()).thenReturn("");
		when(cognitoUserPool.getUserPoolWebClientId()).thenReturn("");
//		when(cognitoUserPool.getDomain()).thenReturn("");

		Assertions.assertEquals("eu-west-3", configurationSettings.getCognitoSettings().getRegion());

		verify(cognitoUserPool).getRegion();
	}

	@Test
	void getGoogleMapsKey() {
		Assertions.assertEquals("use_your_personal_key_for_localhost", configurationSettings.getGoogleMapsKey());
	}

	@MockBean(CognitoSettings.class)
	CloudUserManagement cognitoSettings() {
		return mock(CloudUserManagement.class);
	}
}
