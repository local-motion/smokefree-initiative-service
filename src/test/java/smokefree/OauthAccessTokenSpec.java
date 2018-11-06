package smokefree;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.endpoints.TokenRefreshRequest;
import io.micronaut.security.token.jwt.render.AccessRefreshToken;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.micronaut.http.HttpRequest.POST;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

class OauthAccessTokenSpec {
	private static EmbeddedServer server;
	private static HttpClient client;

	@BeforeAll
	static void setupServer() {
		server = ApplicationContext.run(EmbeddedServer.class);
		client = server
				.getApplicationContext()
				.createBean(HttpClient.class, server.getURL());
	}

	@AfterAll
	static void stopServer() {
		if (server != null) {
			server.stop();
		}
		if (client != null) {
			client.stop();
		}
	}

	@Test
	void should_offer_new_access_token_when_refresh() throws Exception {
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("sherlock", "password");
		HttpRequest request = POST("/login", creds);
		HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
		BearerAccessRefreshToken token = rsp.getBody().orElseThrow();

		sleep(1_000); // sleep for one second to give time for the issued at `iat` Claim to change
		String refreshToken = token.getRefreshToken();
		String accessToken = token.getAccessToken();

		HttpResponse<AccessRefreshToken> refreshResponse = client.toBlocking().exchange(HttpRequest.POST("/oauth/access_token",
				new TokenRefreshRequest("refresh_token", refreshToken)), AccessRefreshToken.class);

		assertNotNull(refreshResponse);
		assertEquals(HttpStatus.OK, refreshResponse.getStatus());
		assertNotEquals(accessToken, refreshResponse.getBody().orElseThrow().getAccessToken());
	}
}
