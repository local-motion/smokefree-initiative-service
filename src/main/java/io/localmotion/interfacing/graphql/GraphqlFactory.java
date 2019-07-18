package io.localmotion.interfacing.graphql;

import com.coxautodev.graphql.tools.SchemaParser;
import com.coxautodev.graphql.tools.SchemaParserBuilder;
import com.coxautodev.graphql.tools.SchemaParserOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhokhov.graphql.datetime.GraphQLDate;
import com.zhokhov.graphql.datetime.GraphQLLocalDate;
import com.zhokhov.graphql.datetime.GraphQLLocalDateTime;
import com.zhokhov.graphql.datetime.GraphQLLocalTime;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import io.localmotion.adminjob.controller.AdminJobMutation;
import io.localmotion.adminjob.controller.AdminJobQuery;
import io.localmotion.audittrail.controller.AuditTrailQuery;
import io.localmotion.initiative.controller.InitiativeQuery;
import io.localmotion.user.controller.UserQuery;
import io.localmotion.userdata.UserDataMutation;
import io.localmotion.userdata.UserDataQuery;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.IOUtils;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.utils.SecurityService;
import lombok.NoArgsConstructor;
import io.localmotion.application.DomainException;
import io.localmotion.initiative.controller.InitiativeMutation;
import io.localmotion.smokefreeplaygrounds.controller.PlaygroundMutation;
import io.localmotion.smokefreeplaygrounds.controller.PlaygroundQuery;
import io.localmotion.interfacing.graphql.error.ConfigurableDataFetcherExceptionHandler;
import io.localmotion.interfacing.graphql.error.ErrorCode;
import io.localmotion.interfacing.graphql.error.ErrorExtensions;
import io.localmotion.interfacing.graphql.error.ErrorExtensionsMapper;
import io.localmotion.user.controller.UserMutation;

import javax.inject.Singleton;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutionException;
import static io.localmotion.interfacing.graphql.error.ErrorExtensionsMapper.exceptionToErrorExtensionsMapper;

@Factory
@NoArgsConstructor
public class GraphqlFactory {
    @Bean
    public ErrorExtensionsMapper<AuthenticationException> mapAuthenticationExceptionToErrorExtension() {
        return exceptionToErrorExtensionsMapper(AuthenticationException.class, e -> new ErrorExtensions(ErrorCode.UNAUTHENTICATED, "Please login"));
    }

    @Bean
    public ErrorExtensionsMapper<ValidationException> mapValidationExceptionToErrorExtension() {
        return exceptionToErrorExtensionsMapper(ValidationException.class, e -> new ErrorExtensions(ErrorCode.VALIDATION, e.getMessage()));
    }

    @Bean
    public ErrorExtensionsMapper<ExecutionException> mapDomainExceptionToErrorExtensions() {

        return exceptionToErrorExtensionsMapper(ExecutionException.class, e -> {

            if(e.getCause() instanceof DomainException) {
                DomainException exception = (DomainException) e.getCause();
                return new ErrorExtensions(ErrorCode.valueOf(exception.getExtensions().get("code").toString()), exception.getExtensions().get("niceMessage").toString());
            } else {
                return new ErrorExtensions(ErrorCode.OTHER, e.getMessage().split(":")[1]);
            }

        });
    }

    @Bean
    public ErrorExtensionsMapper<ConstraintViolationException> mapConstraintViolationToErrorExtensions() {
        return exceptionToErrorExtensionsMapper(ConstraintViolationException.class, e -> new ErrorExtensions(ErrorCode.VALIDATION, e.getMessage().split(":")[1]));
    }
    @Bean
    public DataFetcherExceptionHandler dataFetcherExceptionHandler(List<ErrorExtensionsMapper> errorExtensionsMappers) {
        return new ConfigurableDataFetcherExceptionHandler(errorExtensionsMappers);
    }

    @Bean
    @Singleton
    public GraphQL graphQL(InitiativeQuery initiativeQuery, PlaygroundQuery playgroundQuery, UserQuery userQuery, AuditTrailQuery auditTrailQuery, UserDataQuery userDataQuery,
                           AdminJobQuery adminJobQuery,
                           InitiativeMutation initiativeMutation, PlaygroundMutation playgroundMutation, UserMutation userMutation, UserDataMutation userDataMutation,
                           AdminJobMutation adminJobMutation,
                           SecurityService securityService, ObjectMapper objectMapper, DataFetcherExceptionHandler exceptionHandler) throws IOException {
        /*
         * More information can be found at https://www.graphql-java-kickstart.com/tools/schema-definition/
         */
        InputStream input = GraphqlFactory.class.getResourceAsStream("/public/schema.graphql");
        final String schemaString = IOUtils.readText(new BufferedReader(new InputStreamReader(input)));
        final SchemaParserBuilder builder = SchemaParser.newParser()
                .options(SchemaParserOptions.newOptions().objectMapperProvider(fieldDefinition -> objectMapper).build())
                .resolvers(
                            initiativeQuery, playgroundQuery, userQuery, auditTrailQuery, userDataQuery, adminJobQuery,
                            initiativeMutation, playgroundMutation, userMutation, userDataMutation, adminJobMutation
                )
                .schemaString(schemaString)
                .directive("auth", new AuthenticationDirective(securityService))
                .scalars(
                        new GraphQLDate(),
                        new GraphQLLocalDate(),
                        new GraphQLLocalDateTime(),
                        new GraphQLLocalTime());

        GraphQLSchema graphQLSchema = builder.build().makeExecutableSchema();
        return GraphQL
                .newGraphQL(graphQLSchema)
                .mutationExecutionStrategy(new AsyncSerialExecutionStrategy(exceptionHandler))
                .queryExecutionStrategy(new AsyncExecutionStrategy(exceptionHandler))
                .subscriptionExecutionStrategy(new SubscriptionExecutionStrategy(exceptionHandler))
                .build();
    }

    /**
     * Simply checks if the user is authenticated.
     * <p>
     * Local-Motion does not have the notion of roles (yet). This could be the
     * hook where role based checks for GraphQL would happen.
     */
    class AuthenticationDirective implements SchemaDirectiveWiring {
        private SecurityService securityService;

        AuthenticationDirective(SecurityService securityService) {
            this.securityService = securityService;
        }

        @Override
        public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> schemaDirectiveWiringEnv) {
//            String targetAuthRole = (String) schemaDirectiveWiringEnv.getDirective().getArgument("role").getValue();

            //
            // build a data fetcher that first checks authorisation roles before then calling the original data fetcher
            //
            GraphQLFieldDefinition field = schemaDirectiveWiringEnv.getElement();
            DataFetcher originalDataFetcher = field.getDataFetcher();
            DataFetcher authDataFetcher = dataFetchingEnvironment -> {
                if (!securityService.getAuthentication().isPresent()) {
                    return null;
                }
                return originalDataFetcher.get(dataFetchingEnvironment);
            };
            //
            // now change the field definition to have the new authorising data fetcher
            return field.transform(builder -> builder.dataFetcher(authDataFetcher));
        }
    }

}
