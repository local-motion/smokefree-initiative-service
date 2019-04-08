package io.localmotion.configuration;


import io.micronaut.context.annotation.Value;
import lombok.Getter;

import javax.inject.Singleton;

/**
 * Value object that holds configuration settings to be sent to the front-end
 */

@Singleton
@Getter
public class CognitoSettings implements CloudUserManagement {

    @Value("${aws.cognito.region}")
    private String region;

    @Value("${aws.cognito.userpoolid}")
    private String userPoolId;

    @Value("${aws.cognito.userpoolwebclientid}")
    private String userPoolWebClientId;

    @Value("${aws.cognito.domain}")
    private String domain;

    @Override
    public String getRegion() {
        return this.region;
    }

    @Override
    public String getUserPoolId() {
        return this.userPoolId;
    }

    @Override
    public String getUserPoolWebClientId() {
        return this.userPoolWebClientId;
    }

    @Override
    public String getDomain() {
        return this.domain;
    }
}

