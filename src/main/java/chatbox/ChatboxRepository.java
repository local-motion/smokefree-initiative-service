package chatbox;

import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.spring.tx.annotation.Transactional;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class ChatboxRepository {

    @PersistenceContext
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
        Query query = entityManager.createQuery("SELECT m from ChatMessage m WHERE m.chatboxId = :chatboxId");
        query.setParameter("chatboxId", chatboxId);
        return query.getResultList();
    }

}
