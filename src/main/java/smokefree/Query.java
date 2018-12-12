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
        String userId = toContext(env).userId();
        if (userId == null) {
            return null;
        }
        return profiles.profile(userId);
    }
}