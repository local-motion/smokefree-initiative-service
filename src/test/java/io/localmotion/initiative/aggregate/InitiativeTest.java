package io.localmotion.initiative.aggregate;

import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.initiative.event.MemberJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.aggregate.PlaygroundInitiative;
import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import io.localmotion.smokefreeplaygrounds.event.ManagerJoinedInitiativeEvent;
import io.localmotion.smokefreeplaygrounds.event.PlaygroundInitiativeCreatedEvent;
import io.micronaut.test.annotation.MicronautTest;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.localmotion.smokefreeplaygrounds.domain.CreationStatus.ONLINE_NOT_STARTED;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class InitiativeTest {

	private static final String MANAGER_1 = "manager-1";
	private FixtureConfiguration<PlaygroundInitiative> fixture;

	@BeforeEach
	void setUp() {
		fixture = new AggregateTestFixture<>(PlaygroundInitiative.class);
		fixture.registerCommandDispatchInterceptor(new BeanValidationInterceptor<>());
	}

	private Map<String, Object> metadataWithUser() {
		return singletonMap("user_id", "citizen-1");
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
	void checklistUpdate() {

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
