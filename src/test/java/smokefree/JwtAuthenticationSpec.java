package smokefree;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.authentication.UsernamePasswordCredentials;
import io.micronaut.security.token.jwt.render.BearerAccessRefreshToken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.micronaut.http.HttpRequest.GET;
import static io.micronaut.http.HttpRequest.POST;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationSpec {
    private static EmbeddedServer server;
    private static HttpClient client;

    @BeforeAll
    public static void setupServer() {
        server = ApplicationContext.run(EmbeddedServer.class);
        client = server
                .getApplicationContext()
                .createBean(HttpClient.class, server.getURL());
    }

    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
        }
        if (client != null) {
            client.stop();
        }
    }

    @Test
    void should_401_when_not_authenticated() {
        HttpClientResponseException exception = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(GET("/")));
        assertEquals(401, exception.getStatus().getCode());
    }

    @Test
    void should_offer_bearer_token_when_correct_credentials() throws Exception {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials("sherlock", "password");
        HttpRequest request = POST("/login", creds);
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken.class);
        assertNotNull(rsp);
        assertEquals(HttpStatus.OK, rsp.getStatus());
        BearerAccessRefreshToken token = rsp.getBody().orElseThrow();
        assertEquals("sherlock", token.getUsername());

        final JWT accessToken = JWTParser.parse(token.getAccessToken());
        assertTrue(accessToken instanceof SignedJWT);

        final JWT refreshToken = JWTParser.parse(token.getRefreshToken());
        assertTrue(refreshToken instanceof SignedJWT);

        HttpRequest requestWithAuthorization = GET("/").header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getAccessToken());
        HttpResponse<String> authenticatedRsp = client.toBlocking().exchange(requestWithAuthorization, String.class);

        assertNotNull(authenticatedRsp);
        assertEquals(HttpStatus.OK, authenticatedRsp.getStatus());
        assertEquals("sherlock", authenticatedRsp.body());
    }
}