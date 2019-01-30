package smokefree;

import graphql.*;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.validation.Validated;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.Configuration;
import smokefree.graphql.GraphqlQuery;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static java.util.stream.Collectors.toList;

@Slf4j
//@Validated
@Secured(IS_ANONYMOUS)
@Controller("${micronaut.context.path:}/graphql")
public class GraphqlController {
    @Inject
    private GraphQL graphQL;
    @Inject
    private Configuration configuration; // TODO: Trick to trigger bean creation. Can be done differently?

    @Post(consumes = MediaType.APPLICATION_JSON)
    public Map<String, Object> graphql(@Nullable Authentication authentication, @Size(max=4096) /* TODO Validation not yet enabled */  @Body GraphqlQuery query) throws Exception {
        log.trace("Query: {}", query.getQuery());

        Assert.assertNotNull(query.getQuery());

        // All mutations require an authenticated user
//        Assert.assertTrue(authentication != null || !query.getQuery().trim().startsWith("mutation"), "User must be authenticated");
        if (authentication == null && query.getQuery().trim().startsWith("mutation"))
            return getSingleErrorResult("User must be authenticated");

        // execute the query
        ExecutionInput.Builder builder = new ExecutionInput.Builder()
                .query(query.getQuery())
                .variables(query.getVariables());
        builder.context(new SecurityContext(authentication));
        ExecutionResult executionResult = graphQL.execute(builder);

        // build the resulting response
        Map<String, Object> result = new HashMap<>();
        result.put("data", executionResult.getData());
        result.put("extensions", executionResult.getExtensions());

        // append any errors that may have occurred
        result.put("errors", executionResult
                .getErrors()
                .stream()
                .map(error -> determineErrorAttributes(error))
                .collect(toList()));

        return result;
    }

    private Map<String, Object> determineErrorAttributes(GraphQLError error) {
        // Only show the extension to avoid exposing too much internal information
        if (error.getExtensions() != null)
            return error.getExtensions();
        else {
            Map<String, Object> result = new HashMap<>();
            result.put("code", "9-" + Math.abs(error.hashCode() % 1000));
            result.put("niceMessage", "Technische fout");
            log.info("GraphQL error: " + error);
            return result;
        }
    }

    private Map<String, Object> getSingleErrorResult(String errorMessage) {
        log.info("GraphQL error: " + errorMessage);

        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("code", "9-" + Math.abs(errorMessage.hashCode() % 1000));
        errorResult.put("niceMessage", errorMessage);
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        resultList.add(errorResult);
        result.put("errors", resultList);
        return result;
    }
}
