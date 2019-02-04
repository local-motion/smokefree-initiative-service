package smokefree;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import smokefree.projection.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class Query implements GraphQLQueryResolver {
    @Inject InitiativeProjection initiatives;
    @Inject ProfileProjection profiles;

    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }

    public Collection<Playground> playgrounds() {
        return initiatives.playgrounds();
    }

    public Playground playground(String id) {
        return initiatives.playground(id);
    }

    public Progress progress() {
        return initiatives.progress();
    }

    public Profile profile(DataFetchingEnvironment env) {

        // For now we do not use the profile projection (see note in that class), but retrieve the info straight from the session
//        String userId = toContext(env).userId();
//        if (userId == null) {
//            return null;
//        }
//        return profiles.profile(userId);
        return new Profile(toContext(env).requireUserId(), toContext(env).requireUserName());
    }

    public Playground.PlaygroundObservations validation(String id) {
        return initiatives.validation(id);
    }
}