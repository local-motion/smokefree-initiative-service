package configuration;


import lombok.Value;

/**
 * Value object that holds configuration settings to be sent to the front-end
 */
@Value
public class ConfigurationSettings {
    Environment environment;
    CognitoSettings cognitoSettings;
}

@Value
class CognitoSettings {
    String region;
    String userPoolId;
    String userPoolWebClientId;
    String domain;
    String redirectSignIn;
    String redirectSignOut;
}

