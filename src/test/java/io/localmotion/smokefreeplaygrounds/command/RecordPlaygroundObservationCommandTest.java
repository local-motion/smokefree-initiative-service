package io.localmotion.smokefreeplaygrounds.command;

import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MicronautTest
class RecordPlaygroundObservationCommandTest {

	private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

	@Test
	void should_allow_when_commentLengthIsBetween1To2000() {
		RecordPlaygroundObservationCommand recordPlaygroundObservationCommand = new RecordPlaygroundObservationCommand("123","dev-user",true, "Smokefree playground");
		Set<ConstraintViolation<RecordPlaygroundObservationCommand>> violations =  validatorFactory.getValidator().validate(recordPlaygroundObservationCommand);
		Assertions.assertEquals(0, violations.size());
	}

	/*@Test
	void should_notAllow_when_messageContainsNotAllowedCharacters() {
		RecordPlaygroundObservationCommand recordPlaygroundObservationCommand = new RecordPlaygroundObservationCommand("123","dev-user",true, "~");
		Set<ConstraintViolation<RecordPlaygroundObservationCommand>> violations = validatorFactory.getValidator().validate(recordPlaygroundObservationCommand);
		Assertions.assertEquals("Please enter only allowed special charaxters: @&(),.?\": ", violations.iterator().next().getMessage());
		Assertions.assertEquals(1, violations.size());

	}*/

	@Disabled("user is not obliged to enter a comment")
	@Test
	void should_notAllow_when_commentIsBlank() {
		RecordPlaygroundObservationCommand recordPlaygroundObservationCommand = new RecordPlaygroundObservationCommand("123","dev-user",true, "    ");
		Set<ConstraintViolation<RecordPlaygroundObservationCommand>> violations = validatorFactory.getValidator().validate(recordPlaygroundObservationCommand);
		Assertions.assertEquals("Message must have at least " + SmokefreeConstants.PlaygroundObservation.MINIMUM_COMMENT_LENGTH+ " character", violations.iterator().next().getMessage());
		Assertions.assertEquals(1, violations.size());
	}

	@Test
	void should_notAllow_when_commentLengthExceedMaxLength() {
		RecordPlaygroundObservationCommand recordPlaygroundObservationCommand = new RecordPlaygroundObservationCommand("123","dev-user",true, getMessage(3000));
		Set<ConstraintViolation<RecordPlaygroundObservationCommand>> violations = validatorFactory.getValidator().validate(recordPlaygroundObservationCommand);
		Assertions.assertEquals("Message length must not exceed "+ SmokefreeConstants.PlaygroundObservation.MAXIMUM_COMMENT_LENGTH +" characters", violations.iterator().next().getMessage());
		Assertions.assertEquals(1, violations.size());

	}

	private String getMessage(int length) {
		return Stream.iterate(0, i -> i).limit(length).map(i -> Integer.toString(i)).collect(Collectors.joining(""));
	}

}
