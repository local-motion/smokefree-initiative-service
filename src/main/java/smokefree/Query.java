package smokefree;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.micronaut.security.utils.SecurityService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import smokefree.projection.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class Query implements GraphQLQueryResolver {
    @Inject SecurityService securityService;
    @Inject InitiativeProjection initiatives;
    @Inject ProfileProjection profiles;

    private @Nullable String userId() {
        return securityService.username().orElse(null);
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

    public Profile profile() {
        String userId = userId();
        if (userId == null) {
            return null;
        }
        return profiles.profile(userId);
    }
}