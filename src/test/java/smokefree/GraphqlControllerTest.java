package smokefree;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.AuthenticationUserDetailsAdapter;
import io.micronaut.security.authentication.UserDetails;
import io.micronaut.security.token.validator.TokenValidator;
import io.micronaut.test.annotation.MicronautTest;
import io.reactivex.Flowable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import smokefree.graphql.GraphqlQuery;

import javax.inject.Inject;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static io.micronaut.http.HttpRequest.GET;
import static org.junit.jupiter.api.Assertions.*;

//@Disabled("Use in-memory Axon setup instead of AxonServer")
@SuppressWarnings("WeakerAccess")
@MicronautTest
class GraphqlControllerTest {
    @Inject
    @Client("/")
    HttpClient client;

    String fakeJwt = "fake_jwt_from_test";

    @Test
    void should_401_when_not_authenticated() {
        HttpClientResponseException exception = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(GET("/")));
        assertEquals(401, exception.getStatus().getCode());
    }

    @Test
    void should_not_accept_invalid_graphql_syntax() {
        String invalidSyntaxResponse = "{\n" +
                "  \"errors\" : [ {\n" +
                "    \"message\" : \"Invalid Syntax\",\n" +
                "    \"locations\" : [ {\n" +
                "      \"line\" : 1,\n" +
                "      \"column\" : 2\n" +
                "    } ],\n" +
                "    \"errorType\" : \"InvalidSyntax\"\n" +
                "  } ]\n" +
                "}";

        HttpRequest<?> request = HttpRequest.POST("/graphql", new GraphqlQuery("~~INVALID_SYNTAX~~", newHashMap()))
                .header("Authorization", "Bearer " + fakeJwt);
        String body = client.toBlocking().retrieve(request);
        assertNotNull(body);
        assertEquals(invalidSyntaxResponse, body);
    }
}