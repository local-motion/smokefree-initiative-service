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
import personaldata.PersonalDataRepository;
import smokefree.graphql.GraphqlQuery;
import smokefree.projection.ProfileProjection;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.math.BigInteger;
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

    @Inject
    private ProfileProjection profileProjection;

    @Post(consumes = MediaType.APPLICATION_JSON)
    public Map<String, Object> graphql(@Nullable Authentication authentication, @Size(max=4096) /* TODO Validation not yet enabled */  @Body GraphqlQuery query) throws Exception {
        log.trace("Query: {}", query.getQuery());

        Assert.assertNotNull(query.getQuery());


        // All mutations require an authenticated user
        if (authentication == null && query.getQuery().trim().startsWith("mutation"))
            return getSingleErrorResult("User must be authenticated");


        if ( authentication != null && profileProjection.profile(authentication.getName()) == null  && !query.getQuery().trim().startsWith("mutation CreateUser") ) {
            // Authenticated user does not have a profile yet. This will be a newly enrolled user. Fail and have the front-end do a CreateUser request
            log.trace("Authenticated user without profile: authentication.getName: " + authentication.getName() + " nr of profiles: " + profileProjection.getAllProfiles().size());
            return getSingleErrorResult("NO_PROFILE", "No user profile present");
        }


        // create a fingerprint of the request
        int fingerPrint = query.getQuery().hashCode() + query.getVariables().hashCode();    // TODO filter out meta variables?
        Object lastDigestVariable = query.getVariables().get(("_lastDigest"));
        int lastReponseDigest = lastDigestVariable != null && lastDigestVariable instanceof BigInteger ?
                                ((BigInteger) lastDigestVariable).intValue() : 0;

        // execute the query
        ExecutionInput.Builder builder = new ExecutionInput.Builder()
                .query(query.getQuery())
                .variables(query.getVariables());
        builder.context(new SecurityContext(authentication));
        ExecutionResult executionResult = graphQL.execute(builder);


        // build the resulting response
        Map<String, Object> result = new HashMap<>();


        // todo instead of optimistically hacking this into the data, create an intermediate level that holds the data and the metadata
        if (executionResult.getData() != null) {
            int dataHashcode = executionResult.getData().hashCode();
            if (executionResult.getData() instanceof Map) {
                Map data = (Map) executionResult.getData();
                if (executionResult.getErrors().isEmpty() && lastReponseDigest != 0 && dataHashcode == lastReponseDigest) {
                    data.clear();
                    data.put("status", "not_modified");
                }
                data.put("digest", dataHashcode);
            }
        }
        result.put("data", executionResult.getData());

        // Note that these extensions are not picked up by the apollo graphql client
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
        return getSingleErrorResult(null, errorMessage);
    }
    private Map<String, Object> getSingleErrorResult(String errorCode, String errorMessage) {
        log.info("GraphQL error: " + errorMessage);

        String code = errorCode != null ? errorCode : "9-" + Math.abs(errorMessage.hashCode() % 1000);
        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("code", code);
        errorResult.put("niceMessage", errorMessage);
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        resultList.add(errorResult);
        result.put("errors", resultList);
        return result;
    }
}
