package configuration;


import io.micronaut.context.annotation.Bean;
import lombok.Getter;
import lombok.Value;

import javax.inject.Singleton;

/**
 * Value object that holds configuration settings to be sent to the front-end
 */

@Singleton
@Getter
public class CognitoSettings {
    @io.micronaut.context.annotation.Value("${aws.cognito.region}")
    String region;

    @io.micronaut.context.annotation.Value("${aws.cognito.userpoolid}")
    String userPoolId;

    @io.micronaut.context.annotation.Value("${aws.cognito.userpoolwebclientid}")
    String userPoolWebClientId;

    @io.micronaut.context.annotation.Value("${aws.cognito.domain}")
    String domain;
}

