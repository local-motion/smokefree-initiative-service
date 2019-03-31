package smokefree.domain;

import io.localmotion.initiative.aggregate.Initiative;
import io.localmotion.initiative.command.CreateInitiativeCommand;
import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.initiative.domain.GeoLocation;
import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.command.*;
import io.localmotion.smokefreeplaygrounds.event.*;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.localmotion.application.DomainException;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.Map;

import static java.time.LocalDate.now;
import static java.util.Collections.singletonMap;
import static io.localmotion.initiative.domain.Status.*;
import static io.localmotion.initiative.domain.Type.smokefree;

@SuppressWarnings("ConstantConditions")
class InitiativeTest {
    private static final String MANAGER_1 = "manager-1";
    private FixtureConfiguration<Initiative> fixture;

    @BeforeEach
    void setUp() {
        fixture = new AggregateTestFixture<>(Initiative.class);
        fixture.registerCommandDispatchInterceptor(new BeanValidationInterceptor<>());
    }

    private Map<String, Object> metadataWithUser() {
        return singletonMap("user_id", "citizen-1");
    }

    @Test
    void should_allow_create_initiative() {
        fixture.givenNoPriorActivity()
                .when(new CreateInitiativeCommand("initiative-1", "Test initiative", smokefree, not_started, new GeoLocation(null, null)), metadataWithUser())
                .expectSuccessfulHandlerExecution()
                .expectEvents(initiativeCreated("initiative-1", not_started));
    }

    @Test
    void should_allow_citizen_join_when_first_time() {
        fixture.given(initiativeCreated("initiative-1", not_started))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new MemberJoinedInitiativeEvent("initiative-1", "citizen-1"));
    }


    @Test
    void should_ignore_joins_to_same_initiative_when_same_citizen() {
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        new MemberJoinedInitiativeEvent("initiative-1", "citizen-1"))
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
                .expectEvents(new SmokeFreeDecisionEvent("initiative-1", true));
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
    void should_deny_status_change_when_not_manager() {
        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        managerJoined("manager-XYZ"))
                .when(new DecideToBecomeSmokeFreeCommand("initiative-1"), asManager1())
                .expectException(DomainException.class)
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
                        new SmokeFreeDateCommittedEvent("initiative-1", tomorrow));
    }

    @Test
    void should_not_accept_updated_smokefree_date_when_original_date_in_past() {
        LocalDate yesterday = now().minusDays(1);
        LocalDate today = now();

        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        managerJoined(MANAGER_1),
                        new SmokeFreeDateCommittedEvent("initiative-1", yesterday))
                .when(new CommitToSmokeFreeDateCommand("initiative-1", today), asManager1())
                .expectException(ValidationException.class);
    }

    @Test
    void should_allow_record_smokefreeobservation() {
        LocalDate yesterday = now().minusDays(1);
        LocalDate today = now();

        fixture
                .given(
                        initiativeCreated("initiative-1", not_started),
                        managerJoined(MANAGER_1),
                        new SmokeFreeDateCommittedEvent("initiative-1", yesterday))
                .when(new RecordPlaygroundObservationCommand("initiative-1", "citizen-1", true, "Dont see anyone smoking"), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectEvents(PlaygroundObservationEvent.class
                );
    }

    private Map<String, ?> asManager1() {
        return singletonMap("user_id", MANAGER_1);
    }

    private PlaygroundInitiativeCreatedEvent initiativeCreated(String id, Status status) {
        return new PlaygroundInitiativeCreatedEvent(id, smokefree, status, "Test initiative", new GeoLocation(null, null));
    }

    private ManagerJoinedInitiativeEvent managerJoined(String managerId) {
        return new ManagerJoinedInitiativeEvent("initiative-1", managerId);
    }

}