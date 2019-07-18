package io.localmotion.chatbox.notification;

import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Singleton
public class ChatboxUserRepositoryImplementation implements ChatboxUserRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public ChatboxUserRepositoryImplementation(@CurrentSession EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public static final String FIND_DISTINCT_PERSONS_QUERY = "select distinct m.chatBoxUserId.personId from ChatboxUser m ";
	@Override
	@Transactional(readOnly = true)
	public List<String> findDistinctPersons() {
		return entityManager.createQuery(FIND_DISTINCT_PERSONS_QUERY,String.class).getResultList();
	}

	public static final String FIND_ALL_CHAT_BOXES_BY_PERSON_QUERY = "select m from ChatboxUser m " +
															"where m.chatBoxUserId.personId = :personId" ;
	@Override
	@Transactional(readOnly = true)
	public List<ChatboxUser> findAllChatBoxesByPerson(String person) {
		return entityManager.createQuery(FIND_ALL_CHAT_BOXES_BY_PERSON_QUERY, ChatboxUser.class)
				.setParameter("personId", person)
				.getResultList();
	}

	public static final String FIND_READ_MESSAGE_ID_BY_PERSON_AND_CHAT_BOX_QUERY = "select m from ChatboxUser m " +
																			"where m.chatBoxUserId.personId = :personId AND m.chatBoxUserId.chatBoxId = :chatBoxId";
	@Override
	@Transactional(readOnly = true)
	public ChatboxUser findReadMessageIdByPersonAndChatBox(String person, String chatBox) {
		return entityManager.createQuery(FIND_READ_MESSAGE_ID_BY_PERSON_AND_CHAT_BOX_QUERY, ChatboxUser.class)
				.setParameter("personId",person)
				.setParameter("chatBoxId", chatBox)
				.getSingleResult();
	}

	public static final String FIND_BY_MESSAGE_ID_QUERY = "select m from ChatboxUser m " +
			                                                  "where m.readMessageId = :readMessageId";
	@Override
	@Transactional(readOnly = true)
	public ChatboxUser findByMessageId(String readMessageId) {
		return entityManager.createQuery(FIND_BY_MESSAGE_ID_QUERY, ChatboxUser.class)
				.setParameter("readMessageId", readMessageId)
				.getSingleResult();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void update(ChatboxUser notification) {
		entityManager.merge(notification);
	}

	@Override
	public void create(ChatboxUser newChatboxUser) {
		 entityManager.persist(newChatboxUser);
	}
}
