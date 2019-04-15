package io.localmotion.security.aws.cognito;

import io.micronaut.security.token.jwt.generator.claims.JwtClaims;
import io.micronaut.security.token.jwt.validator.GenericJwtClaimsValidator;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;


@Slf4j
@Singleton
public class LocalmotionJWTClaimsValidator implements GenericJwtClaimsValidator {
    private static final String EMAIL_VERIFIED_CLAIM = "email_verified";
    private static final String EMAIL_CLAIM = "email";

    @Override
    public boolean validate(JwtClaims claims) {

        boolean validClaim =    claims.contains(EMAIL_VERIFIED_CLAIM) &&
                claims.get(EMAIL_VERIFIED_CLAIM) instanceof Boolean &&
                ((Boolean) claims.get(EMAIL_VERIFIED_CLAIM)) ==  true;

        if (!validClaim)
            log.info("Invalid claim email address not verfied: " + claims.get(EMAIL_CLAIM));

        return validClaim;
    }
}
