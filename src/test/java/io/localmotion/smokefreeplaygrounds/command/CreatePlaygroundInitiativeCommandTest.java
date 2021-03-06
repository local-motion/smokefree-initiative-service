package io.localmotion.smokefreeplaygrounds.command;

import io.localmotion.initiative.controller.CreateInitiativeInput;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static io.localmotion.smokefreeplaygrounds.domain.CreationStatus.ONLINE_NOT_STARTED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class CreatePlaygroundInitiativeCommandTest {

	private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

	void when_html_input_should_fail() {
		CreatePlaygroundInitiativeCommand cmd = new CreatePlaygroundInitiativeCommand(
				"initiative-1",
				"<script>alert('hello');</script>",
				ONLINE_NOT_STARTED,
				null
		);

		Set<ConstraintViolation<CreatePlaygroundInitiativeCommand>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(1, violations.size());
		assertEquals("may have unsafe html content", violations.iterator().next().getMessage());
	}

	@Test
	void should_haveConstraintViolation_when_playgroundNameHasMoreThan40Characters() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				"initiative-1",
				"Happy DiemenHappy DiemenHappy DiemenHappy DiemenHappy Diemen",
				22223.32,
				32.322
		);
		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(1, violations.size());
		assertEquals("The name must be less than 40 characters", violations.iterator().next().getMessage());
	}

	@Test
	void should_NotHaveConstraintViolation_when_playgroundNameHas3To40Characters() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				"initiative-1",
				"Happy Diemen",
				22223.32,
				32.322
		);
		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(0, violations.size());
	}

	@Test
	void should_haveConstraintViolation_when_playgroundInitiativeIdIsNull() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				null,
				"Happy Diemen",
				22223.32,
				32.322
		);
		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(1, violations.size());
		assertEquals("The initiativeId must have a value", violations.iterator().next().getMessage());
	}
	@Test
	void should_haveConstraintViolation_when_playgroundInitiativeIdIsBlank() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				"",
				"Happy Diemen",
				22223.32,
				32.322
		);
		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(1, violations.size());
		assertEquals("The initiativeId must not be blank", violations.iterator().next().getMessage());
	}

//    @Test
//    void should_haveConstraintViolation_when_playgroundStatusIsNull() {
//        CreateInitiativeInput cmd = new CreateInitiativeInput(
//                "Name",
//                null,
//                "Happy Diemen",
//                22223.32,
//                32.322
//        );
//        Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
//        assertEquals(1, violations.size());
//        assertEquals("The status must not be blank", violations.iterator().next().getMessage());
//    }

	@Test
	void should_haveConstraintViolation_whenPlaygroundLongitudeIsNull() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				"Name",
				"Happy Diemen",
				32.234566,
				null
		);
		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(1, violations.size());
		assertEquals("The lng must not be blank", violations.iterator().next().getMessage());

	}

	@Test
	void should_haveConstraintViolation_whenPlaygroundLatitudeIsNull() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				"Name",
				"Happy Diemen",
				null,
				32.234566
		);
		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(1, violations.size());
		assertEquals("The lat must not be blank", violations.iterator().next().getMessage());

	}

}
