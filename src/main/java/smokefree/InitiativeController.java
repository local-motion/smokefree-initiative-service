package smokefree;

import graphql.Assert;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.Configuration;
import smokefree.graphql.GraphqlQuery;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Slf4j
@Secured("isAuthenticated()")
@Controller("/graphql")
public class InitiativeController {
    @Inject
    private GraphQL graphQL;
    @Inject
    private Configuration configuration; // TODO: Trick to trigger bean creation. Can be done differently?

    @Post(value="/", consumes= MediaType.APPLICATION_JSON)
    public Map<String, Object> graphqlPost(Principal principal, @Size(max=4096) @Body GraphqlQuery query) {
        log.info("Principal: {}", principal.getName());

        Assert.assertNotNull(query.getQuery());

        // execute the query
        ExecutionInput.Builder builder = new ExecutionInput.Builder()
                .query(query.getQuery())
                .variables(query.getVariables());
        ExecutionResult executionResult = graphQL.execute(builder);

        // build the resulting response
        Map<String, Object> result = new HashMap<>();
        result.put("data", executionResult.getData());

        // append any errors that may have occurred
        List<?> errors = executionResult.getErrors();
        if (errors != null && !errors.isEmpty()) {
            result.put("errors", errors);
        }

        // add any extension data
        result.put("extensions", executionResult.getExtensions());

        // return the resulting data
        return result;
    }
}
