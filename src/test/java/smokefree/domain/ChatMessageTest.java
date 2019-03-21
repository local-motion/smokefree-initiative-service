package smokefree.domain;

import chatbox.ChatMessage;
import org.junit.jupiter.api.Test;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatMessageTest {

	private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

	@Test
	void should_allow_when_messageIsValid() {
		ChatMessage message = new ChatMessage();
		message.setMessageId("123");
		message.setChatboxId("2345");
		message.setAuthor("dev-user-1");
		message.setText("Hi Volunteers, Can this \n playground be smokefree");

		Set<ConstraintViolation<ChatMessage>> violations = validatorFactory.getValidator().validate(message);
		assertEquals(0, violations.size());

	}

	@Test
	void should_notAllow_when_messageContainsNotAllowedCharacters() {
		ChatMessage message = new ChatMessage();
		message.setMessageId("123");
		message.setChatboxId("2345");
		message.setAuthor("dev-user-1");
		message.setText("~");

		Set<ConstraintViolation<ChatMessage>> violations = validatorFactory.getValidator().validate(message);
		assertEquals("Please enter only allowed special charaxters: @&(),.?\": ", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());

	}

	@Test
	void should_notAllow_when_messageHasOnlySpaces() {
		ChatMessage message = new ChatMessage();
		message.setMessageId("123");
		message.setChatboxId("2345");
		message.setAuthor("dev-user-1");
		message.setText("                                         ");

		Set<ConstraintViolation<ChatMessage>> violations = validatorFactory.getValidator().validate(message);
		assertEquals("Message must have at least " + SmokefreeConstants.ChatBox.MINIMUM_MESSAGE_LENGTH+ " characters", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());

	}

	@Test
	void should_notAllow_when_messageIsExceedMaxLength() {
		ChatMessage message = new ChatMessage();
		message.setMessageId("123");
		message.setChatboxId("2345");
		message.setAuthor("dev-user-1");
		message.setText(getMessage(4000));

		Set<ConstraintViolation<ChatMessage>> violations = validatorFactory.getValidator().validate(message);
		assertEquals("Message length must not exceed "+ SmokefreeConstants.ChatBox.MAXIMUM_MESSAGE_LENGTH +" characters", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());

	}


	private String getMessage(int maxLength) {
		return Stream.iterate(0, i -> i).limit(maxLength).map(i -> Integer.toString(i)).collect(Collectors.joining(""));
	}
}
