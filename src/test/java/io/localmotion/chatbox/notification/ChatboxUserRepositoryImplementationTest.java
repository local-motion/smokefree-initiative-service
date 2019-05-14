package io.localmotion.chatbox.notification;

import io.micronaut.test.annotation.MicronautTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaDelete;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@MicronautTest
@Slf4j
class ChatboxUserRepositoryImplementationTest {

	@Inject
	ChatboxUserRepositoryImplementation chatBoxUserRepositoryImplementation;

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	PlatformTransactionManager platformTransactionManager;

	@BeforeEach
	void refreshDataBase() {
		TransactionStatus txStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

		// Delete all ChatboxUser before each test case
		CriteriaDelete<ChatboxUser> deleteChatBoxNotification = entityManager.getCriteriaBuilder().createCriteriaDelete(ChatboxUser.class);
		deleteChatBoxNotification.from(ChatboxUser.class);
		entityManager.createQuery(deleteChatBoxNotification).executeUpdate();

		platformTransactionManager.commit(txStatus);
	}

	@Test
	void findDistinctPersons() {
		// Given
		String personId_1 = UUID.randomUUID().toString();
		String personId_2 = UUID.randomUUID().toString();
		String chatBoxId_1 = UUID.randomUUID().toString();
		String chatBoxId_2 = UUID.randomUUID().toString();

		ChatboxUser notification_1 = new ChatboxUser(new ChatboxUserId(personId_1, chatBoxId_1), null, null);
		ChatboxUser notification_2 = new ChatboxUser(new ChatboxUserId(personId_1, chatBoxId_2), null, null);
		ChatboxUser notification_3 = new ChatboxUser(new ChatboxUserId(personId_2, chatBoxId_1), null,null);
		entityManager.persist(notification_1);
		entityManager.persist(notification_2);
		entityManager.persist(notification_3);

		List<String> expectedDistinctPersonIds = new ArrayList<>();
		expectedDistinctPersonIds.add(personId_2);
		expectedDistinctPersonIds.add(personId_1);
		expectedDistinctPersonIds = expectedDistinctPersonIds.stream().sorted().collect(Collectors.toList());
		//expectedDistinctPersonIds.forEach(log::info);

		// When
		List<String> actualDistinctPersonIds =  chatBoxUserRepositoryImplementation.findDistinctPersons();
		actualDistinctPersonIds = actualDistinctPersonIds.stream().sorted().collect(Collectors.toList());
		//actualDistinctPersonIds.forEach(log::info);
		// Then
		assertIterableEquals(expectedDistinctPersonIds,actualDistinctPersonIds );
	}

	@Test
	void findAllChatBoxesByPerson() {
		// Given
		String messageId_1 = UUID.randomUUID().toString();
		String messageId_2 = UUID.randomUUID().toString();
		String chatBoxId_1 = UUID.randomUUID().toString();
		String chatBoxId_2 = UUID.randomUUID().toString();
		String personId = UUID.randomUUID().toString();
		ChatboxUser notification_1 = new ChatboxUser(new ChatboxUserId(personId, chatBoxId_1),messageId_1,null);
		ChatboxUser notification_2 = new ChatboxUser(new ChatboxUserId(personId, chatBoxId_2),messageId_2,null);
		entityManager.persist(notification_1);
		entityManager.persist(notification_2);

		List<ChatboxUser> expectedChatBoxIds = new ArrayList<>();
		expectedChatBoxIds.add(notification_1);
		expectedChatBoxIds.add(notification_2);
		expectedChatBoxIds.stream().sorted();
		// When
		List<ChatboxUser> actualChatboxUsers =  chatBoxUserRepositoryImplementation.findAllChatBoxesByPerson(personId);
		actualChatboxUsers.stream().sorted();

		// THen
		assertEquals(expectedChatBoxIds, actualChatboxUsers);
	}

	@Test
	void findReadMessageIdByPersonAndChatBox() {
		// Given
		String personId_1 = UUID.randomUUID().toString();
		String chatBox_Id = UUID.randomUUID().toString();
		String messageId_1 = UUID.randomUUID().toString();
		String messageId_2 = UUID.randomUUID().toString();

		ChatboxUser notification_1 = new ChatboxUser(new ChatboxUserId(personId_1, chatBox_Id), messageId_1, null);
		entityManager.persist(notification_1);

		ChatboxUser expectedChatboxUser = notification_1;

		// When
		ChatboxUser actualNotification = chatBoxUserRepositoryImplementation.findReadMessageIdByPersonAndChatBox(personId_1,chatBox_Id);

		// Then
		assertEquals(expectedChatboxUser, actualNotification);

	}

	@Test
	void findByMessageId() {
		// Given
		String personId_1 = UUID.randomUUID().toString();
		String chatBoxId_1 = UUID.randomUUID().toString();
		String messageId_1 = UUID.randomUUID().toString();

		ChatboxUser notification_1 = new ChatboxUser(new ChatboxUserId(personId_1, chatBoxId_1), messageId_1, null);
		entityManager.persist(notification_1);

		ChatboxUser expectedChatboxUser = notification_1;

		// When
		ChatboxUser actualChatboxUser =  chatBoxUserRepositoryImplementation.findByMessageId(messageId_1);

		// Then
		assertEquals(expectedChatboxUser, actualChatboxUser);
	}

	@Test
	void update() {
		// Given
		String personId_1 = UUID.randomUUID().toString();
		String chatBoxId_1 = UUID.randomUUID().toString();
		String messageId_1 = UUID.randomUUID().toString();
		ChatboxUser notification_1 = new ChatboxUser(new ChatboxUserId(personId_1, chatBoxId_1), messageId_1, null);
		entityManager.persist(notification_1);

		// When
		String notifiedMessageId_1 = UUID.randomUUID().toString();
		notification_1.setNotifiedMessageId(notifiedMessageId_1);
		chatBoxUserRepositoryImplementation.update(notification_1);

		// THen
		assertEquals(notifiedMessageId_1, entityManager.find(ChatboxUser.class, new ChatboxUserId(personId_1,chatBoxId_1)).getNotifiedMessageId());

	}

	@Test
	void create() {
		// Given
		String personId_1 = UUID.randomUUID().toString();
		String chatBoxId_1 = UUID.randomUUID().toString();
		String messageId_1 = UUID.randomUUID().toString();

		// When
		ChatboxUser notification_1 = new ChatboxUser(new ChatboxUserId(personId_1, chatBoxId_1), messageId_1, null);
		ChatboxUser expectedChatboxUser = notification_1;
		chatBoxUserRepositoryImplementation.create(notification_1);

		// Then
		assertEquals(expectedChatboxUser, entityManager.find(ChatboxUser.class, new ChatboxUserId(personId_1, chatBoxId_1)));
	}
}
