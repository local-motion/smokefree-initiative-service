package smokefree.graphql;

import com.coxautodev.graphql.tools.SchemaParser;
import com.coxautodev.graphql.tools.SchemaParserBuilder;
import com.coxautodev.graphql.tools.SchemaParserOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhokhov.graphql.datetime.GraphQLDate;
import com.zhokhov.graphql.datetime.GraphQLLocalDate;
import com.zhokhov.graphql.datetime.GraphQLLocalDateTime;
import com.zhokhov.graphql.datetime.GraphQLLocalTime;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.IOUtils;
import io.micronaut.security.utils.SecurityService;
import lombok.NoArgsConstructor;
import smokefree.Mutation;
import smokefree.Query;

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@Factory
@NoArgsConstructor
public class GraphqlFactory {
    @Bean
    @Singleton
    public GraphQL graphQL(Query query, Mutation mutation, SecurityService securityService, ObjectMapper objectMapper) throws IOException {
        InputStream input = GraphqlFactory.class.getResourceAsStream("/public/schema.graphql");
        final String schemaString = IOUtils.readText(new BufferedReader(new InputStreamReader(input)));
        final SchemaParserBuilder builder = SchemaParser.newParser()
                .options(SchemaParserOptions.newOptions().objectMapperProvider(fieldDefinition -> objectMapper).build())
                .resolvers(query, mutation)
                .schemaString(schemaString)
                .directive("auth", new AuthorisationDirective(securityService))
                .scalars(
                        new GraphQLDate(),
                        new GraphQLLocalDate(),
                        new GraphQLLocalDateTime(),
                        new GraphQLLocalTime());

        GraphQLSchema graphQLSchema = builder.build().makeExecutableSchema();
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    class AuthorisationDirective implements SchemaDirectiveWiring {
        private SecurityService securityService;

        AuthorisationDirective(SecurityService securityService) {
            this.securityService = securityService;
        }

        @Override
        public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> schemaDirectiveWiringEnv) {
            String targetAuthRole = (String) schemaDirectiveWiringEnv.getDirective().getArgument("role").getValue();

            GraphQLFieldDefinition field = schemaDirectiveWiringEnv.getElement();
            //
            // build a data fetcher that first checks authorisation roles before then calling the original data fetcher
            //
            DataFetcher originalDataFetcher = field.getDataFetcher();
            DataFetcher authDataFetcher = dataFetchingEnvironment -> {
                Map<String, Object> contextMap = dataFetchingEnvironment.getContext();
//                AuthorisationCtx authContext = (AuthorisationCtx) contextMap.get("authContext");
                if (!securityService.getAuthentication().isPresent()) {
                    return null;
                }

//                if (authContext.hasRole(targetAuthRole)) {
                if (1 == 1) {
                    return originalDataFetcher.get(dataFetchingEnvironment);
                } else {
                    return null;
                }
            };
            //
            // now change the field definition to have the new authorising data fetcher
            return field.transform(builder -> builder.dataFetcher(authDataFetcher));
        }
    }

}
