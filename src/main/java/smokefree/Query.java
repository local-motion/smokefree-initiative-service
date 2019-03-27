package smokefree;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.initiative.projection.Initiative;
import io.localmotion.interfacing.graphql.SecurityContext;
import io.localmotion.smokefreeplaygrounds.projection.Playground;
import io.localmotion.smokefreeplaygrounds.projection.PlaygroundProjection;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class Query implements GraphQLQueryResolver {
//    @Inject
//    private InitiativeProjection initiatives;
    @Inject
    private ProfileProjection profiles;

    @Inject
    private PlaygroundProjection playgroundProjection;


    public Collection<Playground> playgrounds(DataFetchingEnvironment env) {
        return playgroundProjection.playgrounds(getUserId(env));
    }

    public Playground playground(String id, DataFetchingEnvironment env) {
        return playgroundProjection.playground(id, getUserId(env));
    }

    public Profile profile(DataFetchingEnvironment env) {
        String userId = toContext(env).userId();
        if (userId == null) {
            return null;
        }
        return profiles.profile(userId);
    }

    /**
     * Compute and return the total number of volunteers.
     * @return total volunteers count
     */
    public long totalVolunteers() {
        return playgroundProjection.playgrounds(null).stream()
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