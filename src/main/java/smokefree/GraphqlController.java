package smokefree;

import graphql.*;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.config.Configuration;
import smokefree.graphql.GraphqlQuery;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static java.util.stream.Collectors.toList;

@Slf4j
@Secured(IS_ANONYMOUS)
@Controller("${micronaut.context.path:}/graphql")
public class GraphqlController {
    @Inject
    private GraphQL graphQL;
    @Inject
    private Configuration configuration; // TODO: Trick to trigger bean creation. Can be done differently?

    @Post(consumes = MediaType.APPLICATION_JSON)
    public Map<String, Object> graphql(@Nullable Authentication authentication, @Size(max=4096) @Body GraphqlQuery query) throws Exception {
        log.trace("Query: {}", query.getQuery());

        Assert.assertNotNull(query.getQuery());

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
                .map(GraphQLError::toSpecification)
                .collect(toList()));
        return result;
    }

}
