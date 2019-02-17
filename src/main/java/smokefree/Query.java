package smokefree;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import smokefree.projection.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class Query implements GraphQLQueryResolver {
    @Inject InitiativeProjection initiatives;
    @Inject ProfileProjection profiles;


    public Collection<Playground> playgrounds(DataFetchingEnvironment env) {
        return initiatives.playgrounds(getUserId(env));
    }

    public Playground playground(String id, DataFetchingEnvironment env) {
        return initiatives.playground(id, getUserId(env));
    }

    public Progress progress() {
        return initiatives.progress();
    }

    public Profile profile(DataFetchingEnvironment env) {

        // For now we do not use the profile projection (see note in that class), but retrieve the info straight from the session
        String userId = toContext(env).userId();
        if (userId == null) {
            return null;
        }
        return profiles.profile(userId);
//        return new Profile(toContext(env).requireUserId(), toContext(env).requireUserName());
    }

    /**
     * It will compute the total number of volunteers.
     * @return total volunteers count
     */
    public long totalVolunteers() {
        return initiatives.playgrounds(null).stream()
                .flatMap(playground -> playground.getVolunteers().stream())
                .distinct()
                .count();
    }


    /***********
     * Utility functions
     ************/

    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }

    private String getUserId(DataFetchingEnvironment env) {
        return toContext(env).userId();
    }

}