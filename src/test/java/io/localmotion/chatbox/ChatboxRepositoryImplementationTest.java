package io.localmotion.chatbox;

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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@Slf4j
class ChatboxRepositoryImplementationTest {

	@PersistenceContext
	EntityManager entityManager;

	@Inject
	ChatboxRepositoryImplementation chatboxRepository;

	@Inject
	PlatformTransactionManager platformTransactionManager;

	@BeforeEach
	void refreshDatabase() {
		TransactionStatus tx = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

		//Delete all rows from ChatMessage Entity
		CriteriaDelete<ChatMessage> deleteChatMessage = entityManager.getCriteriaBuilder().createCriteriaDelete(ChatMessage.class);
		deleteChatMessage.from(ChatMessage.class);
		entityManager.createQuery(deleteChatMessage).executeUpdate();
		platformTransactionManager.commit(tx);
	}

	@Test
	void getMessageById() {
		// Given
		String messageId_1 = UUID.randomUUID().toString();
		String chatBoxId_1 = UUID.randomUUID().toString();
		ChatMessage message_1 = new ChatMessage(messageId_1, chatBoxId_1,"user-1","Hi");
		entityManager.persist(message_1);
		ChatMessage expectedMessage = message_1;

		// When
		ChatMessage actualMessage = chatboxRepository.getMessageById(messageId_1);

		// Then
		assertEquals(expectedMessage, actualMessage);
	}
}
