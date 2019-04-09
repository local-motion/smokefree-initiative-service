package io.localmotion.configuration;


import io.micronaut.context.annotation.Value;
import lombok.Getter;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Value object that holds configuration settings to be sent to the front-end
 */
@Singleton
@Getter
public class ConfigurationSettings {

    @Value("${localmotion.logicalenvironment}")
    LogicalEnvironment logicalEnvironment;


    @Value("${google.maps.key}")
    String googleMapsKey;

    // It support other cloud solutions for user managemnt and
    // easier to test if we have interface and implementations to inject different  implementations
    private CloudUserManagement cognitoSettings;

    public ConfigurationSettings(CloudUserManagement cognitoSettings) {
        this.cognitoSettings = cognitoSettings;
    }
}


