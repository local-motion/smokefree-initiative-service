package smokefree;

import graphql.*;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.Configuration;
import smokefree.graphql.GraphqlQuery;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Slf4j
@Secured(IS_ANONYMOUS)
@Controller("${micronaut.context.path:}/graphql")
public class GraphqlController {
    @Inject
    private GraphQL graphQL;
    @Inject
    private Configuration configuration; // TODO: Trick to trigger bean creation. Can be done differently?

    @Post(value="/", consumes= MediaType.APPLICATION_JSON)
    public Map<String, Object> graphql(@Nullable Principal principal, @Size(max=4096) @Body GraphqlQuery query) {
        log.trace("Query: {}", query.getQuery());

        Assert.assertNotNull(query.getQuery());

        // execute the query
        ExecutionInput.Builder builder = new ExecutionInput.Builder()
                .query(query.getQuery())
                .variables(query.getVariables());
        builder.context(new Context(principal));
        ExecutionResult executionResult = graphQL.execute(builder);

        // build the resulting response
        Map<String, Object> result = new HashMap<>();
        result.put("data", executionResult.getData());
        result.put("extensions", executionResult.getExtensions());

        // append any errors that may have occurred
        executionResult
                .getErrors()
                .forEach(error -> result.putAll(error.toSpecification()));
        return result;
    }

    private class Context extends HashMap<String, Object> {
        public Context(Principal principal) {
            if (principal != null) {
                put("principal", principal);
            }
        }

        public Principal principal() {
            return (Principal) get("principal");
        }
    }
}
