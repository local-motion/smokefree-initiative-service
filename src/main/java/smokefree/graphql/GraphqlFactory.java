package smokefree.graphql;

import com.coxautodev.graphql.tools.SchemaParser;
import com.coxautodev.graphql.tools.SchemaParserBuilder;
import com.zhokhov.graphql.datetime.GraphQLDate;
import com.zhokhov.graphql.datetime.GraphQLLocalDate;
import com.zhokhov.graphql.datetime.GraphQLLocalDateTime;
import com.zhokhov.graphql.datetime.GraphQLLocalTime;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.IOUtils;
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
    public GraphQL graphQL(Query query, Mutation mutation) throws IOException {
        InputStream input = GraphqlFactory.class.getResourceAsStream("/public/schema.graphql");
        final String schemaString = IOUtils.readText(new BufferedReader(new InputStreamReader(input)));
        final SchemaParserBuilder builder = SchemaParser.newParser()
                .resolvers(query, mutation)
                .schemaString(schemaString)
                .scalars(
                        new GraphQLDate(),
                        new GraphQLLocalDate(),
                        new GraphQLLocalDateTime(),
                        new GraphQLLocalTime());
        GraphQLSchema graphQLSchema = builder.build().makeExecutableSchema();
        return GraphQL.newGraphQL(graphQLSchema).build();
    }
}
