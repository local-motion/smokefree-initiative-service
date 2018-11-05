package smokefree.domain;

import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonMap;
import static smokefree.domain.Status.*;
import static smokefree.domain.Type.smokefree;

@SuppressWarnings("ConstantConditions")
class InitiativeTest {
    private static final String MANAGER_1 = "manager-1";
    private FixtureConfiguration<Initiative> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Initiative.class);
        fixture.registerCommandDispatchInterceptor(new BeanValidationInterceptor<>());
    }

    @Test
    void should_allow_create_initiative() {
        fixture.givenNoPriorActivity()
                .when(new CreateInitiativeCommand("initiative-1", "Test initiative", smokefree, not_started, new GeoLocation(null, null)))
                .expectSuccessfulHandlerExecution()
                .expectEvents(initiativeCreated("initiative-1", not_started));
    }

    @Test
    void should_allow_citizen_join_when_first_time() {
        fixture.given(initiativeCreated("initiative-1", not_started))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new CitizenJoinedInitiativeEvent("initiative-1", "citizen-1"));
    }


    @Test
    void should_ignore_joins_to_same_initiative_when_same_citizen() {
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        new CitizenJoinedInitiativeEvent("initiative-1", "citizen-1"))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1")) // Attempt to join again!
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }

    @Test
    void should_allow_manager_join_when_first_time() {
        fixture.given(initiativeCreated("initiative-1", not_started))
                .when(new ClaimManagerRoleCommand("initiative-1"), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectEvents(managerJoined(MANAGER_1));
    }

    @Test
    void should_ignore_joins_to_same_initiative_when_same_manager() {
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        managerJoined(MANAGER_1))
                .when(new ClaimManagerRoleCommand("initiative-1"), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }

    @Test
    void should_have_inprogress_status_when_decide_to_become_smokefree() {
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        managerJoined(MANAGER_1))
                .when(new DecideToBecomeSmokeFreeCommand("initiative-1"), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectEvents(new InitiativeProgressedEvent("initiative-1", not_started, in_progress));
    }

    @Test
    void should_ignore_decision_when_wrong_current_status() {
        expectNoEventsForStatus(finished);
        expectNoEventsForStatus(in_progress);
    }

    private void expectNoEventsForStatus(Status status) {
        fixture
                .given(
                        initiativeCreated("initiative-1", status),
                        managerJoined(MANAGER_1))
                .when(new DecideToBecomeSmokeFreeCommand("initiative-1"), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }

    @Test
    void should_have_stopped_status_when_not_deciding_to_become_smokefree() {
        String reason = "Obviously no good reason";
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        managerJoined(MANAGER_1))
                .when(new DecideToNotBecomeSmokeFreeCommand("initiative-1", reason), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectEvents(new InitiativeStoppedEvent("initiative-1", not_started, stopped, reason));

    }

    @Test
    void should_deny_status_change_when_not_manager() {
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        managerJoined("manager-XYZ"))
                .when(new DecideToBecomeSmokeFreeCommand("initiative-1"), asManager1())
                .expectException(IllegalArgumentException.class)
                .expectNoEvents();
    }

    @Test
    void should_progress_states_when_commit_to_smokefree_date() {
        LocalDate tomorrow = now().plusDays(1);
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        managerJoined(MANAGER_1))
                .when(new CommitToSmokeFreeDateCommand("initiative-1", tomorrow), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectEvents(
                        new SmokeFreeDateCommittedEvent("initiative-1", null, tomorrow),
                        new InitiativeProgressedEvent("initiative-1", not_started, finished));
    }

    private Map<String, ?> asManager1() {
        return singletonMap("user_id", MANAGER_1);
    }

    private InitiativeCreatedEvent initiativeCreated(String id, Status status) {
        return new InitiativeCreatedEvent(id, smokefree, status, "Test initiative", new GeoLocation(null, null));
    }

    private ManagerJoinedInitiativeEvent managerJoined(String managerId) {
        return new ManagerJoinedInitiativeEvent("initiative-1", managerId);
    }

}