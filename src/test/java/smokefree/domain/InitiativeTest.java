package smokefree.domain;

import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static smokefree.domain.Status.*;
import static smokefree.domain.Type.smokefree;

@SuppressWarnings("ConstantConditions")
class InitiativeTest {
    private FixtureConfiguration<Initiative> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Initiative.class);
        fixture.registerCommandDispatchInterceptor(new BeanValidationInterceptor<>());
    }

    @Test
    void should_allow_create() {
        fixture.givenNoPriorActivity()
                .when(new CreateInitiativeCommand("initiative-1", "Test initiative", smokefree, not_started, new GeoLocation(null, null)))
                .expectSuccessfulHandlerExecution()
                .expectEvents(initiativeCreated("initiative-1", not_started));
    }

    @Test
    void should_allow_first_time_join() {
        fixture.given(initiativeCreated("initiative-1", not_started))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new InitiativeJoinedEvent("initiative-1", "citizen-1"));
    }


    @Test
    void should_ignore_multiple_joins_to_same_initiative() {
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        new InitiativeJoinedEvent("initiative-1", "citizen-1"))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1")) // Attempt to join again!
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }

    @Test
    void should_have_inprogress_status_when_decide_to_become_smokefree() {
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        new InitiativeJoinedEvent("initiative-1", "citizen-1"),
                        new InitiativeJoinedEvent("initiative-1", "manager-1"))
                .when(new DecideToBecomeSmokeFreeCommand("initiative-1"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new InitiativeProgressedEvent("initiative-1", not_started, in_progress));
    }

    @Test
    void should_have_stopped_status_when_not_deciding_to_become_smokefree() {
        String reason = "Obviously no good reason";
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started))
                .when(new DecideToNotBecomeSmokeFreeCommand("initiative-1", reason))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new InitiativeStoppedEvent("initiative-1", not_started, stopped, reason));

    }

    private InitiativeCreatedEvent initiativeCreated(String id, Status status) {
        return new InitiativeCreatedEvent(id, smokefree, status, "Test initiative", new GeoLocation(null, null));
    }

}