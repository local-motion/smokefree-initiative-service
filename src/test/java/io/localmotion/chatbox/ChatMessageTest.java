package io.localmotion.chatbox;

import io.localmotion.chatbox.model.ChatMessage;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ChatMessageTest {

	private ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

	@Test
	void should_allow_when_messageIsValid() {
		ChatMessage message = createChatMessage();
		message.setText("Hi Volunteers, Can this \n playground be smokefree");

		Set<ConstraintViolation<ChatMessage>> violations = validatorFactory.getValidator().validate(message);
		assertEquals(0, violations.size());

	}

	@Disabled("Currently we allow all characters")
	@Test
	void should_notAllow_when_messageContainsNotAllowedCharacters() {
		ChatMessage message = createChatMessage();
		message.setText("~");

		Set<ConstraintViolation<ChatMessage>> violations = validatorFactory.getValidator().validate(message);
		assertEquals("Please enter only allowed special charaxters: @&(),.?\": ", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());
	}

	@Test
	void should_notAllow_when_messageHasOnlySpaces() {
		ChatMessage message = createChatMessage();
		message.setText("                                         ");

		Set<ConstraintViolation<ChatMessage>> violations = validatorFactory.getValidator().validate(message);
		assertEquals("Message must have at least " + SmokefreeConstants.ChatBox.MINIMUM_MESSAGE_LENGTH+ " characters", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());

	}

	@Test
	void should_notAllow_when_messageIsExceedMaxLength() {
		ChatMessage message = createChatMessage();
		message.setText(getMessageText(4000));

		Set<ConstraintViolation<ChatMessage>> violations = validatorFactory.getValidator().validate(message);
		assertEquals("Message length must not exceed "+ SmokefreeConstants.ChatBox.MAXIMUM_MESSAGE_LENGTH +" characters", violations.iterator().next().getMessage());
		assertEquals(1, violations.size());

	}

	private ChatMessage createChatMessage() {
		ChatMessage chatMessage = new ChatMessage();
		return chatMessage;
	}

	private String getMessageText(int length) {
		return Stream.iterate(0, i -> i).limit(length).map(i -> Integer.toString(i)).collect(Collectors.joining(""));
	}

}
