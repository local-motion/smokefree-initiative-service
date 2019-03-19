package configuration;


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

    @Inject
    CognitoSettings cognitoSettings;

    @Value("${google.maps.key}")
    String googleMapsKey;
}


