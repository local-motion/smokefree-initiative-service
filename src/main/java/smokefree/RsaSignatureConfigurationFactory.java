package smokefree;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.IOUtils;
import com.nimbusds.jose.util.JSONObjectUtils;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import io.micronaut.security.token.jwt.signature.SignatureConfiguration;
import io.micronaut.security.token.jwt.signature.rsa.RSASignature;
import io.micronaut.security.token.jwt.signature.rsa.RSASignatureConfiguration;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import java.net.URL;
import java.nio.charset.Charset;
import java.security.interfaces.RSAPublicKey;

import javax.inject.Singleton;

@Slf4j
@Factory
@NoArgsConstructor
public class RsaSignatureConfigurationFactory {
	@Value("${aws.cognito.region}")
	private String region;
	
	@Value("${aws.cognito.userpoolid}")
	private String userPoolId;

	@Bean
	@Singleton
	public SignatureConfiguration awsCognitoSignatureConfiguration() {
		//noinspection Convert2Lambda
		return new RSASignature(new RSASignatureConfiguration() {
			@SneakyThrows
			@Override
			public RSAPublicKey getPublicKey() {
				final URL url = new URL(this.getJwkUrl());
				String json = IOUtils.readInputStreamToString(url.openStream(), Charset.forName("UTF-8"));
				final JSONObject jwkWithMultipleKeys = JSONObjectUtils.parse(json);
				final JSONArray keys = JSONObjectUtils.getJSONArray(jwkWithMultipleKeys, "keys");
				final JSONObject singleJsonKey = (JSONObject)keys.get(0);
				return new RSAKey.Builder(RSAKey.parse(singleJsonKey)).build().toRSAPublicKey();
			}
			
			private String getJwkUrl() {
				String jwk = "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId + "/.well-known/jwks.json";
				log.info("Using region [{}] and userpool [{}] to read JWK from {}", region, userPoolId, jwk);
				return jwk;
			}
		});
	}
	
	
}
