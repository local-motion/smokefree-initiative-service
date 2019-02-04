package smokefree;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import smokefree.graphql.GraphqlQuery;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Playground;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static io.micronaut.http.HttpRequest.GET;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@MicronautTest
class GraphqlControllerTest {
    @Inject
    @Client("/")
    HttpClient client;

    //    @Inject
//    InitiativeProjection initiatives;
    private String fakeJwt = "eyJraWQiOiJ5UUdFMTQ2Z2JtNWYwVm9MZUZkMSt2bEpWK3laZ3B4YTFIc1wvVE5ZR0VEcz0iLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiI5YTAxZTg4Mi1jODY5LTQ0ZjItYTZhMy03YzZhMzljNDk5ZmIiLCJhdWQiOiI2MWFyYnZvbW1pN202YmlzaGhxNGpscmJkIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsImV2ZW50X2lkIjoiNjlhYmU1MTEtMjU1ZC0xMWU5LThkZDEtMjE2YWZlMmU5ZjVlIiwidG9rZW5fdXNlIjoiaWQiLCJhdXRoX3RpbWUiOjE1NDg5NDE4NzQsImlzcyI6Imh0dHBzOlwvXC9jb2duaXRvLWlkcC5ldS13ZXN0LTEuYW1hem9uYXdzLmNvbVwvZXUtd2VzdC0xX29KalM5aWVJZCIsImNvZ25pdG86dXNlcm5hbWUiOiJkZXYtdXNlcjIiLCJleHAiOjE1NDg5NDU0NzQsImlhdCI6MTU0ODk0MTg3NCwiZW1haWwiOiJhbmFuZGFpbGkwOEBnbWFpbC5jb20ifQ.EOWIPf83r0TYOtPQtXmKC_Ta-cVZHMouXmYT94cKyBAbBOPS-UOc1l3gdz6cg3CtHe79qva6wTvfsRWD1tCoIwjOihraXoZmoMLz6Ot_BaFoF7UL7joUjHUBMJUy0OAHFwC6YAtndUM6DLKICgH73PVTqZiZ-NoVNtmhwxmJdLc28uR7DHi9SmTxBGtL6wXOWT4l6ej7Qs6xt5t0Nu9UJ-dUfmDSPpdc3yKWSVUqNfqLv29gJBHQDx7NztWXnp3DeVmiNggcE0qZZLpnbbYrRyMikTD1oB4UJrNhTw9uFp8khPmrRfBMkB8E97XF2k_DMGasQMG_SR0Bf96DaXUPwQ";

    //@Test
    void should_401_when_not_authenticated() {
        HttpClientResponseException exception = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(GET("/")));
        assertEquals(401, exception.getStatus().getCode());
    }

    //@Test
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

    @Disabled("Use in-memory Axon setup so projections can be verified")
    //@Test
    void when_mutation_should_prevent_xss() {
        String query = "mutation CreateInitiative($input: CreateInitiativeInput!) {\n" +
                "    createInitiative(input: $input) {\n" +
                "        id\n" +
                "    }\n" +
                "}";



        Map<String, Object> variables = newHashMap();
        variables.put("initiativeId", "test-1");
        variables.put("type", "smokefree");
        variables.put("status", "not_started");
        variables.put("name", "<script>alert('xss attack');</script>");
        variables.put("lat", "52.327293");
        variables.put("lng", "6.603781");

        Map<String, Object> input = newHashMap();
        input.put("input", variables);

        HttpRequest<?> request = HttpRequest.POST("/graphql", new GraphqlQuery(query, input))
                .header("Authorization", "Bearer " + fakeJwt);
        String body = client.toBlocking().retrieve(request);
        log.info("Response: {}", body);

//        Thread.sleep(100);
//        final Collection<Playground> playgrounds = initiatives.playgrounds();
//        assertEquals(1, playgrounds.size());
    }

    @Disabled("Use in-memory Axon setup so projections can be verified")
    @Test
    void when_positive_smokefreeplayground_observation() {
        String query = "mutation CreateInitiative($input: CreateInitiativeInput!) {\n" +
                "    createInitiative(input: $input) {\n" +
                "        id\n" +
                "    }\n" +
                "}";



        Map<String, Object> variables = newHashMap();
        variables.put("initiativeId", "test-1");
        variables.put("type", "smokefree");
        variables.put("status", "not_started");
        variables.put("name", "<script>alert('xss attack');</script>");
        variables.put("lat", "52.327293");
        variables.put("lng", "6.603781");

        Map<String, Object> input = newHashMap();
        input.put("input", variables);

        HttpRequest<?> request = HttpRequest.POST("/graphql", new GraphqlQuery(query, input))
                .header("Authorization", "Bearer " + fakeJwt);
        String body = client.toBlocking().retrieve(request);
        log.info("Response: {}", body);

//        Thread.sleep(100);
//        final Collection<Playground> playgrounds = initiatives.playgrounds();
//        assertEquals(1, playgrounds.size());
    }
}