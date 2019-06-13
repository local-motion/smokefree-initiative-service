package io.localmotion.chatbox;

import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Collection;

@Singleton
public class ChatboxRepository {

//    @PersistenceContext
    private EntityManager entityManager;

    public ChatboxRepository(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public void storeMessage(String chatboxId, ChatMessage chatMessage) {
        entityManager.persist(chatMessage);
    }

    @Transactional(readOnly = true)
    public Collection<ChatMessage> getMessages(String chatboxId) {
        Query query = entityManager.createQuery(
                "SELECT m from UserDataRecord m " +
                        "WHERE m.chatboxId = :chatboxId " +
                        "ORDER BY m.creationTime ASC"
        );
        query.setParameter("chatboxId", chatboxId);
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Collection<ChatMessage> getMessagesSince(String chatboxId, String messageId) {
        Query query = entityManager.createQuery(
                "SELECT m from UserDataRecord m " +
                        "WHERE m.chatboxId = :chatboxId " +
                        "AND m.creationTime > (SELECT n.creationTime FROM UserDataRecord n WHERE n.messageId = :messageId) " +
                        "ORDER BY m.creationTime ASC"
        );
        query.setParameter("chatboxId", chatboxId);
        query.setParameter("messageId", messageId);
        return query.getResultList();
    }

}
