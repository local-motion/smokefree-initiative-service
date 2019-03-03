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
    private String fakeJwt = "fakeJwt";

    //@Test
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

    @Disabled("Use in-memory Axon setup so projections can be verified")
    @Test
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

    @Disabled
    @Test
    void when_no_one_smoking_in_playground() {
        String query = "mutation recordPlaygroundObservation($input: RecordPlaygroundObservationCommand!) {\n" +
                "    recordPlaygroundObservation(input: $input) {\n" +
                "        id\n" +
                "        playgroundObservations { \n smokefree \n observationDate \n comment \n}" +
                "    }\n" +
                "}";



        Map<String, Object> variables = newHashMap();
        variables.put("initiativeId", "test-1");
        variables.put("smokefree", true);
        variables.put("comment", "I do not see anyone smoking");

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