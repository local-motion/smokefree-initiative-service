package smokefree.graphql.error;

import graphql.ErrorType;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

/**
 * The standard handling of data fetcher error involves placing a {@link ExceptionWhileDataFetching} error
 * into the error collection
 *
 * <pre>
 * {
 *   "extensions" : null,
 *   "data" : null,
 *   "errors" : [ {
 *     "message" : "Exception while fetching data (/decideToBecomeSmokeFree) : 2020e808-4cb1-472e-b89e-aa2e227b7103 is not a manager",
 *     "locations" : [ {
 *       "line" : 2,
 *       "column" : 3
 *     } ],
 *     "path" : [ "decideToBecomeSmokeFree" ],
 *     "extensions" : {
 *       "code" : "UNAUTHENTICATED",
 *       "niceMessage" : "This is a nice message!"
 *     }
 *   } ]
 * }
 * </pre>
 */
@Slf4j
@Value
public class ConfigurableDataFetcherExceptionHandler implements DataFetcherExceptionHandler {
    private List<ErrorExtensionsMapper> errorExtensionsMappers;

    /**
     * Similar implementation to the official {@link graphql.execution.SimpleDataFetcherExceptionHandler} with
     * the exception that in case of framework exceptions, such as {@link javax.validation.ValidationException}
     * or {@link io.micronaut.security.authentication.AuthenticationException} it allows for mapping
     * an exception the an ErrorExtensions.
     *
     * Subsequently, the ErrorExtensions is sent to the frontend
     */
    @Override
    public void accept(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = handlerParameters.getException();
        SourceLocation sourceLocation = handlerParameters.getField().getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();

        Throwable throwableWithErrorExtensions = toThrowableWithExtensions(exception);
        ExceptionWhileDataFetching error = new ExceptionWhileDataFetching(path, throwableWithErrorExtensions, sourceLocation);
        handlerParameters.getExecutionContext().addError(error);

        log.warn(error.getMessage(), throwableWithErrorExtensions);
    }

    private Throwable toThrowableWithExtensions(Throwable exception) {
        if (exception instanceof GraphQLError) return exception;

        for (ErrorExtensionsMapper errorExtensionsMapper : errorExtensionsMappers) {
            if (errorExtensionsMapper.accepts(exception)) {
                ErrorExtensions errorExtensions = errorExtensionsMapper.map(exception);
                return new ExtensionsDataGraphQLErrorAdapter(exception, errorExtensions);
            }
        }

        ErrorExtensions defaultExtensions = new ErrorExtensions(ErrorCode.OTHER, "Sorry, something went wrong. We're on it...");
        return new ExtensionsDataGraphQLErrorAdapter(exception, defaultExtensions);
    }

    /**
     * Workaround where ExceptionWhileDataFetching only accepts 'extensions' if
     * an exception is of type GraphQLError.
     */
    @Value
    @EqualsAndHashCode(callSuper = true)
    private class ExtensionsDataGraphQLErrorAdapter extends RuntimeException implements GraphQLError {
        Throwable throwable;
        ErrorExtensions errorExtensions;

        @Override
        public String getMessage() {
            return throwable.getMessage();
        }

        @Override
        public List<SourceLocation> getLocations() {
            return null;
        }

        @Override
        public ErrorType getErrorType() {
            return null;
        }

        @Override
        public Map<String, Object> getExtensions() {
            Map<String, Object> extensions = newHashMap();
            extensions.put("code", errorExtensions.getCode());
            extensions.put("niceMessage", errorExtensions.getNiceMessage());
            return extensions;
        }
    };
}
