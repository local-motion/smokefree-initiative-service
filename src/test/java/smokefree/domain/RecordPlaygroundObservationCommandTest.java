package smokefree.domain;


import org.junit.Test;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class RecordPlaygroundObservationCommandTest {

	private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

	@Test
    public void should_allow_when_commentLengthIsBetween1To2000() {
		RecordPlaygroundObservationCommand recordPlaygroundObservationCommand = new RecordPlaygroundObservationCommand("123","dev-user",true, "Smokefree playground");
		Set<ConstraintViolation<RecordPlaygroundObservationCommand>> violations =  validatorFactory.getValidator().validate(recordPlaygroundObservationCommand);
		assertEquals(0, violations.size());
	}

	@Test
	public void should_notAllow_when_messageContainsNotAllowedCharacters() {
		RecordPlaygroundObservationCommand recordPlaygroundObservationCommand = new RecordPlaygroundObservationCommand("123","dev-user",true, "~");
		Set<ConstraintViolation<RecordPlaygroundObservationCommand>> violations = validatorFactory.getValidator().validate(recordPlaygroundObservationCommand);
		assertEquals("Please enter only allowed special charaxters: @&(),.?\": ", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());

	}

	@Test
    public void should_notAllow_when_commentIsBlank() {
		RecordPlaygroundObservationCommand recordPlaygroundObservationCommand = new RecordPlaygroundObservationCommand("123","dev-user",true, "    ");
		Set<ConstraintViolation<RecordPlaygroundObservationCommand>> violations = validatorFactory.getValidator().validate(recordPlaygroundObservationCommand);
		assertEquals("Message must have at least " + SmokefreeConstants.PlaygroundObservation.MINIMUM_COMMENT_LENGTH+ " character", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());
	}

	@Test
	public void should_notAllow_when_commentLengthExceedMaxLength() {
		RecordPlaygroundObservationCommand recordPlaygroundObservationCommand = new RecordPlaygroundObservationCommand("123","dev-user",true, getMessage(3000));
		Set<ConstraintViolation<RecordPlaygroundObservationCommand>> violations = validatorFactory.getValidator().validate(recordPlaygroundObservationCommand);
		assertEquals("Message length must not exceed "+ SmokefreeConstants.PlaygroundObservation.MAXIMUM_COMMENT_LENGTH +" characters", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());

	}

	private String getMessage(int maxLength) {
		return Stream.iterate(0, i -> i).limit(maxLength).map(i -> Integer.toString(i)).collect(Collectors.joining(""));
	}

}
