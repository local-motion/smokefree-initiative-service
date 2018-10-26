package smokefree.graphql;

import com.coxautodev.graphql.tools.SchemaParser;
import com.coxautodev.graphql.tools.SchemaParserBuilder;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

public class Toolkit {
    public static void main(String[] args) {
        final SchemaParserBuilder builder = SchemaParser.newParser()
                .resolvers(new Query(), new Mutation())
                .schemaString("schema {" +
                        "    query: Query" +
                        "    mutation: Mutation" +
                        "}" +
                        "" +
                        "type Query {" +
                        "}" +
                        "type Mutation {" +
                        "    createArticle(input: CreateArticleInput!): ArticleId!" +
                        "}" +
                        "type ArticleId {" +
                        "    id : String!" +
                        "}" +
                        "input CreateArticleInput {" +
                        "    title: String!" +
                        "    text: String!" +
                        "    authorId: Int!" +
                        "}");
        GraphQLSchema graphQLSchema = builder.build().makeExecutableSchema();
        GraphQL build = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionResult executionResult = build.execute("mutation CreateArticle($input: CreateArticleInput!) {" +
                "  createArticle(input: $input) {" +
                "    id" +
                "    title" +
                "    author {" +
                "      id" +
                "      username" +
                "    }" +
                "  }" +
                "}");

        System.out.println(executionResult.getData().toString());
    }
}
