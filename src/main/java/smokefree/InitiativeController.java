package smokefree;

import smokefree.graphql.GraphqlQuery;
import graphql.Assert;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("Duplicates")
@Controller("/graphql")
public class InitiativeController {
    private GraphQL graphQL;

    public InitiativeController(GraphQL graphQL) {
        this.graphQL = graphQL;
    }

    @Post(value="/", consumes= MediaType.APPLICATION_JSON)
    public Map<String, Object> graphqlPost(@Size(max=4096) @Body GraphqlQuery query) {
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
