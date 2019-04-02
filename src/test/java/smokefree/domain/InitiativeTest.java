package smokefree.domain;

import io.localmotion.application.DomainException;
import io.localmotion.initiative.aggregate.Initiative;
import io.localmotion.smokefreeplaygrounds.command.CreatePlaygroundInitiativeCommand;
import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.command.ClaimManagerRoleCommand;
import io.localmotion.smokefreeplaygrounds.command.CommitToSmokeFreeDateCommand;
import io.localmotion.smokefreeplaygrounds.command.DecideToBecomeSmokeFreeCommand;
import io.localmotion.smokefreeplaygrounds.command.RecordPlaygroundObservationCommand;
import io.localmotion.smokefreeplaygrounds.event.*;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ValidationException;
import java.time.LocalDate;
import java.util.Map;

import static io.localmotion.smokefreeplaygrounds.domain.CreationStatus.*;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonMap;

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
                .when(new CreatePlaygroundInitiativeCommand("initiative-1", "Test initiative", ONLINE_NOT_STARTED, new GeoLocation(null, null)), metadataWithUser())
                .expectSuccessfulHandlerExecution()
                .expectEvents(initiativeCreated("initiative-1", ONLINE_NOT_STARTED));
    }

    @Test
    void should_allow_citizen_join_when_first_time() {
        fixture.given(initiativeCreated("initiative-1", ONLINE_NOT_STARTED))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1"))
                .expectSuccessfulHandlerExecution()
                .expectEvents(new MemberJoinedInitiativeEvent("initiative-1", "citizen-1"));
    }


    @Test
    void should_ignore_joins_to_same_initiative_when_same_citizen() {
        fixture
                .given(
                        initiativeCreated("initiative-1", ONLINE_NOT_STARTED),
                        new MemberJoinedInitiativeEvent("initiative-1", "citizen-1"))
                .when(new JoinInitiativeCommand("initiative-1", "citizen-1")) // Attempt to join again!
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }

    @Test
    void should_allow_manager_join_when_first_time() {
        fixture.given(initiativeCreated("initiative-1", ONLINE_NOT_STARTED))
                .when(new ClaimManagerRoleCommand("initiative-1"), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectEvents(managerJoined(MANAGER_1));
    }

    @Test
    void should_ignore_joins_to_same_initiative_when_same_manager() {
        fixture
                .given(
                        initiativeCreated("initiative-1", ONLINE_NOT_STARTED),
                        managerJoined(MANAGER_1))
                .when(new ClaimManagerRoleCommand("initiative-1"), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectNoEvents();
    }

    @Test
    void should_have_inprogress_status_when_decide_to_become_smokefree() {
        fixture
                .given(
                        initiativeCreated("initiative-1", ONLINE_NOT_STARTED),
                        managerJoined(MANAGER_1))
                .when(new DecideToBecomeSmokeFreeCommand("initiative-1"), asManager1())
                .expectSuccessfulHandlerExecution()
                .expectEvents(new SmokeFreeDecisionEvent("initiative-1", true));
    }

//    @Test
//    void should_ignore_decision_when_wrong_current_status() {
//        expectNoEventsForStatus(finished);
//        expectNoEventsForStatus(in_progress);
//    }

//    private void expectNoEventsForStatus(Status status) {
//        fixture
//                .given(
//                        initiativeCreated("initiative-1", status),
//                        managerJoined(MANAGER_1))
//                .when(new DecideToBecomeSmokeFreeCommand("initiative-1"), asManager1())
//                .expectSuccessfulHandlerExecution()
//                .expectNoEvents();
//    }

    @Test
    void should_deny_status_change_when_not_manager() {
        fixture
                .given(
                        initiativeCreated("initiative-1", ONLINE_NOT_STARTED),
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
                        initiativeCreated("initiative-1", ONLINE_NOT_STARTED),
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
                        initiativeCreated("initiative-1", ONLINE_NOT_STARTED),
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
                        initiativeCreated("initiative-1", ONLINE_NOT_STARTED),
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

    private PlaygroundInitiativeCreatedEvent initiativeCreated(String id, CreationStatus creationStatus) {
        return new PlaygroundInitiativeCreatedEvent(id, "Test initiative", creationStatus, new GeoLocation(null, null));
    }

    private ManagerJoinedInitiativeEvent managerJoined(String managerId) {
        return new ManagerJoinedInitiativeEvent("initiative-1", managerId);
    }

}