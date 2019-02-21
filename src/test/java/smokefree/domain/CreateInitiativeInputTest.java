package smokefree.domain;

import org.junit.jupiter.api.Test;
import smokefree.graphql.CreateInitiativeInput;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateInitiativeInputTest {

	private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

	@Test
	void testInvalidPlaygroundNameIfNameHasLessThan3Characters() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				"initiative-1",
				Type.smokefree,
				Status.not_started,
				"Hi",
				22223.32,
				32.322
		);

		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(1, violations.size());
		assertEquals("The name must be at least 3 characters", violations.iterator().next().getMessage());
	}

	@Test
	void testInvalidPlaygroundNameIfNameHasMoreThan40Characters() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				"initiative-1",
				Type.smokefree,
				Status.not_started,
				"Happy DiemenHappy DiemenHappy DiemenHappy DiemenHappy Diemen",
				22223.32,
				32.322
		);

		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(1, violations.size());
		assertEquals("The name must be less than 40 characters", violations.iterator().next().getMessage());
	}

	@Test
	void testValidPlaygroundNameIfNameContaines3To40Characters() {
		CreateInitiativeInput cmd = new CreateInitiativeInput(
				"initiative-1",
				Type.smokefree,
				Status.not_started,
				"Happy Diemen",
				22223.32,
				32.322
		);

		Set<ConstraintViolation<CreateInitiativeInput>> violations = validatorFactory.getValidator().validate(cmd);
		assertEquals(0, violations.size());
	}
}
