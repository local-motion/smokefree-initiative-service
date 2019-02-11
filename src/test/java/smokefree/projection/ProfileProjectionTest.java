package smokefree.projection;

import org.axonframework.messaging.MetaData;
import org.junit.jupiter.api.Test;
import smokefree.domain.CitizenJoinedInitiativeEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileProjectionTest {

//    @Test
//    void should_store_profiles() {
//        ProfileProjection profileProjection = new ProfileProjection();
//        profileProjection.on(new CitizenJoinedInitiativeEvent("initiative-1", "citizen-1"), MetaData
//                .with("user_id", "citizen-1")
//                .and("user_name", "Bob Carhartt")
//                .and("email", "bob.carhartt@example.org"));
//
//        final Profile profile = profileProjection.profile("citizen-1");
//        assertEquals(new Profile("citizen-1", "Bob Carhartt", "bob.carhartt@example.org"), profile);
//    }
}