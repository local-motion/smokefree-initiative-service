package smokefree;

import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import smokefree.graphql.GraphqlQuery;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.jupiter.api.Assertions.*;

@Disabled("Use in-memory Axon setup instead of AxonServer")
@SuppressWarnings("WeakerAccess")
class InitiativeControllerTest {
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

        HttpRequest<?> request = HttpRequest.POST("/graphql", new GraphqlQuery("~~INVALID_SYNTAX~~", newHashMap()));
        String body = client.toBlocking().retrieve(request);
        assertNotNull(body);
        assertEquals(invalidSyntaxResponse, body);
    }
}