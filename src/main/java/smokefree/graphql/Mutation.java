package smokefree.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import smokefree.domain.JoinInitiativeCommand;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@NoArgsConstructor
public class Mutation implements GraphQLMutationResolver {
    @Inject
    CommandGateway gateway;

    public ArticleId createArticle(CreateArticleInput input) {
        log.warn("Creating and 'saving' article based on input {}", input);
        final Article article = new Article("9999", input.getTitle(), input.getText(), input.getAuthorId());
        return new ArticleId(article.getId());
    }

    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input) {
        gateway.send(new JoinInitiativeCommand(input.getInitiativeId(), input.getCitizenId()));
        return new InputAcceptedResponse("888");
    }
}