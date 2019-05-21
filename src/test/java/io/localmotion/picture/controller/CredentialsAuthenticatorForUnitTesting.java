package io.localmotion.picture.controller;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.security.authentication.*;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.ArrayList;

@Singleton
@Requires(env = "test")
@Slf4j
public class CredentialsAuthenticatorForUnitTesting implements AuthenticationProvider {

    @Value("${login.username}")
    private String username;

    @Value("${login.password}")
    private String password;

    @Override
    public Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        if(authenticationRequest.getIdentity().equals("test") && authenticationRequest.getSecret().equals("password")) {
            return Flowable.just(new UserDetails((String) authenticationRequest.getIdentity(), new ArrayList<>()));
        } else {
            return Flowable.just(new AuthenticationFailed());
        }
    }
}
