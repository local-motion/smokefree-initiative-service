package io.localmotion.batch.notification;

import io.localmotion.chatbox.ChatMessage;
import io.localmotion.chatbox.ChatboxRepositoryImplementation;
import io.localmotion.chatbox.ChatboxRepository;
import io.localmotion.chatbox.notification.ChatboxUser;
import io.localmotion.chatbox.notification.ChatboxUserId;
import io.localmotion.chatbox.notification.ChatboxUserRepositoryImplementation;
import io.localmotion.chatbox.notification.ChatboxUserRepository;
import io.localmotion.personaldata.IPersonalDataRepository;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.micronaut.context.annotation.Primary;
import io.micronaut.test.annotation.MicronautTest;
import io.micronaut.test.annotation.MockBean;
import org.junit.jupiter.api.Disabled;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest
class ChatboxNotificationBatchTest {

	@Inject
	ChatBoxNotificationBatch chatBoxNotificationBatch;

	@Inject
	ChatboxUserRepository chatBoxNotificationRepository;

	@Inject
	ChatboxRepository chatboxRepository;

	@Inject
	IPersonalDataRepository personalDataRepository;

	@Disabled
	void notifyUnreadMessages() throws InterruptedException {
		// Given
		List<ChatboxUser> notifications = new ArrayList<>();
		String personId_1 = UUID.randomUUID().toString();
		String chatBoxId_1 = UUID.randomUUID().toString();
		String chatBoxId_2 = UUID.randomUUID().toString();
		String messageId_1 = UUID.randomUUID().toString();
		String messageId_2 = UUID.randomUUID().toString();

		ChatboxUser notification_1 = new ChatboxUser(new ChatboxUserId(personId_1,chatBoxId_1), messageId_1, null);
		ChatboxUser notification_2 = new ChatboxUser(new ChatboxUserId(personId_1, chatBoxId_2), messageId_2, null);
		notifications.add(notification_1);
		notifications.add(notification_2);
		when(chatBoxNotificationRepository.findDistinctPersons()).
				thenReturn(notifications.stream().map(n -> n.getChatBoxUserId().getPersonId()).collect(Collectors.toList()).subList(0,1));
		when(chatBoxNotificationRepository.findAllChatBoxesByPerson(personId_1)).thenReturn(notifications);
		when(chatBoxNotificationRepository.findReadMessageIdByPersonAndChatBox(personId_1,chatBoxId_1))
				.thenReturn(notification_1);
		when(chatBoxNotificationRepository.findReadMessageIdByPersonAndChatBox(personId_1,chatBoxId_2))
				.thenReturn(notification_2);

		ChatMessage message_1 = new ChatMessage(messageId_1, chatBoxId_1, "user-1", "Hi");
		ChatMessage message_2 = new ChatMessage(messageId_2, chatBoxId_2, "user-2", "Hello");

		when(chatboxRepository.getMessageById(messageId_1)).thenReturn(message_1);
		when(chatboxRepository.getMessageById(messageId_2)).thenReturn(message_2);
		when(chatboxRepository.getMessagesSince(chatBoxId_1, messageId_1)).thenReturn(new ArrayList<>(){{ add(message_1);}});
		when(chatboxRepository.getMessagesSince(chatBoxId_2,messageId_2)).thenReturn(new ArrayList<>(){{add(message_2);}});

		long recordId = 1;
		String data = "{\"name\":\"ta-user-5\",\"emailAddress\":\"anandaili08@gmail.com\"}";
		PersonalDataRecord record_1 = new PersonalDataRecord(recordId,personId_1,data);
		when(personalDataRepository.getRecordByPersonId(personId_1)).thenReturn(record_1);

		// When
		chatBoxNotificationBatch.notifyUnreadMessages();


		// Then
		verify(chatBoxNotificationRepository).findDistinctPersons();
		verify(chatBoxNotificationRepository).findAllChatBoxesByPerson(personId_1);
		verify(chatBoxNotificationRepository).findReadMessageIdByPersonAndChatBox(personId_1,chatBoxId_1);
		verify(chatBoxNotificationRepository).findReadMessageIdByPersonAndChatBox(personId_1,chatBoxId_2);
		verify(chatboxRepository).getMessageById(messageId_1);
		verify(chatboxRepository).getMessageById(messageId_2);
		verify(chatboxRepository).getMessagesSince(chatBoxId_1, messageId_1);
		verify(chatboxRepository).getMessagesSince(chatBoxId_2, messageId_2);
	}

	private ChatboxUser updateNotification(ChatboxUser notification_1, String messageId_1) {
		notification_1.setNotifiedMessageId(messageId_1);
		return notification_1;
	}

	@Primary
	@MockBean(ChatboxUserRepositoryImplementation.class)
	public ChatboxUserRepository chatBoxNotificationRepository() {
		return mock(ChatboxUserRepository.class);
	}

	@Primary
	@MockBean(ChatboxRepositoryImplementation.class)
	public ChatboxRepository chatboxRepository() {
		return mock(ChatboxRepository.class);
	}

	@Primary
	@MockBean(PersonalDataRepository.class)
	public IPersonalDataRepository personalDataRepository() {
		return mock(IPersonalDataRepository.class);
	}
}
