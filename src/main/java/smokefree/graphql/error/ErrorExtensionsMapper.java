package smokefree.graphql.error;

import java.util.function.Function;

/**
 * Marker interface to allow Micronaut injection
 */
public interface ErrorExtensionsMapper<T extends Throwable> {
    boolean accepts(Throwable throwable);

    ErrorExtensions map(Throwable throwable);

    /**
     * Create exception handlers that convert to Graphql API errors. Using Micronaut, example usage for handling <code>AuthenticationException</code>:
     <pre>
     \@Bean
     public ApiErrorHandler authenticationExceptionHandler() {
        return exceptionToErrorExtensionsMapper(AuthenticationException.class, e -> new ErrorExtensionsMapper(ErrorCode.OTHER, "My nice message"));
     }
     </pre>
     */
    static <T extends Throwable> ErrorExtensionsMapper<T> exceptionToErrorExtensionsMapper(Class<T> exception, Function<T, ErrorExtensions> handler) {
        return new ErrorExtensionsMapper<>() {
            @Override
            public boolean accepts(Throwable throwable) {
                return throwable.getClass().isAssignableFrom(exception);
            }

            @Override
            public ErrorExtensions map(Throwable throwable) {
                //noinspection unchecked
                return handler.apply((T)throwable);
            }
        };
    }
}
