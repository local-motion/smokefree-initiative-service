package smokefree.domain;

import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InitiativeTest {
    private FixtureConfiguration<Initiative> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Initiative.class);
    }

    @Test
    void should_allow_create() {
        fixture.givenNoPriorActivity()
                .when(new CreateInitiativeCommand("initiative-1", null, null, null, null, null))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new InitiativeCreatedEvent("initiative-1", null, null, null, null, null));
    }

    @Test
    void should_allow_first_time_join() {
        fixture.given(new InitiativeCreatedEvent("initiative-1", null, null, null, null, null))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new InitiativeJoinedEvent("initiative-1", "citizen-1"));
    }


    @Test
    void should_ignore_multiple_joins_to_same_initiative() {
        fixture
                .given(
                        new InitiativeCreatedEvent("initiative-1", null, null, null, null, null),
                        new InitiativeJoinedEvent("initiative-1", "citizen-1"))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1")) // Attempt to join again!
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }














































/*

    @Test
    void should_allow_first_time_join() {
        fixture.given(new InitiativeCreatedEvent("initiative-1", null, null, null, null, null))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new InitiativeJoinedEvent("initiative-1", "citizen-1"));
    }

    @Test
    void should_ignore_multiple_joins_to_same_initiative() {
        fixture
                .given(
                        new InitiativeCreatedEvent("initiative-1", null, null, null, null, null),
                        new InitiativeJoinedEvent("initiative-1", "citizen-1"))
                .when(
                        new JoinInitiativeCommand("initiative-1", "citizen-1"))
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }
    */
}