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

@Factory
@NoArgsConstructor
public class GraphqlFactory {
    @Bean
    @Singleton
    public GraphQL graphQL(Query query, Mutation mutation, SecurityService securityService, ObjectMapper objectMapper) throws IOException {
        /*
         * More information can be found at https://www.graphql-java-kickstart.com/tools/schema-definition/
         */
        InputStream input = GraphqlFactory.class.getResourceAsStream("/public/schema.graphql");
        final String schemaString = IOUtils.readText(new BufferedReader(new InputStreamReader(input)));
        final SchemaParserBuilder builder = SchemaParser.newParser()
                .options(SchemaParserOptions.newOptions().objectMapperProvider(fieldDefinition -> objectMapper).build())
                .resolvers(query, mutation)
                .schemaString(schemaString)
                .directive("auth", new AuthenticationDirective(securityService))
                .scalars(
                        new GraphQLDate(),
                        new GraphQLLocalDate(),
                        new GraphQLLocalDateTime(),
                        new GraphQLLocalTime());

        GraphQLSchema graphQLSchema = builder.build().makeExecutableSchema();
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    /**
     * Simply checks if the user is authenticated.
     *
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
