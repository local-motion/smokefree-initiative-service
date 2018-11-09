package smokefree;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.IOUtils;
import com.nimbusds.jose.util.JSONObjectUtils;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
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

@Slf4j
@Factory
@NoArgsConstructor
public class RsaSignatureConfigurationFactory {
	//	@Value("") // TODO: Read from config.
	private String jwkUrl = "https://cognito-idp.eu-west-1.amazonaws.com/eu-west-1_WsTxYUHyC/.well-known/jwks.json";

	@Bean
	public SignatureConfiguration awsCognitoSignatureConfiguration() {
		//noinspection Convert2Lambda
		return new RSASignature(new RSASignatureConfiguration() {
			@SneakyThrows
			@Override
			public RSAPublicKey getPublicKey() {
				final URL url = new URL(jwkUrl);
				String json = IOUtils.readInputStreamToString(url.openStream(), Charset.forName("UTF-8"));
				final JSONObject jwkWithMultipleKeys = JSONObjectUtils.parse(json);
				final JSONArray keys = JSONObjectUtils.getJSONArray(jwkWithMultipleKeys, "keys");
				final JSONObject singleJsonKey = (JSONObject)keys.get(0);
				return new RSAKey.Builder(RSAKey.parse(singleJsonKey)).build().toRSAPublicKey();
			}
		});
	}
}
