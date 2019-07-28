package io.localmotion.chatbox;

import io.localmotion.chatbox.model.*;
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
    public void storeMessage(String externalchatboxId, String externalAuthorId, String text) {

        User user = entityManager.find(User.class, externalAuthorId);
        ChatBox chatBox = entityManager.find(ChatBox.class, externalchatboxId);
        ChatBoxId chatBoxId = new ChatBoxId(chatBox.getId(), user.getId());
        ChatBoxUser chatBoxUser = entityManager.find(ChatBoxUser.class, chatBoxId);

        ChatMessageV2 chatMessage = new ChatMessageV2();
        chatMessage.setAuthor(chatBoxUser);
        chatMessage.setText(text);

        entityManager.persist(chatMessage);
    }

    @Transactional
    public void storeMessage(ChatBoxUser author, String text) {
        ChatMessageV2 chatMessage = new ChatMessageV2();
        chatMessage.setAuthor(author);
        chatMessage.setText(text);
        entityManager.persist(chatMessage);
    }

    public void storeMessage(String author, ChatMessage chatMessage) {
    }

        @Transactional(readOnly = true)
    public User getUser(int id) {
        return entityManager.find(User.class, id);
    }

    @Transactional(readOnly = true)
    public User getUserWithExternalId(String externalId) {
        Query query = entityManager.createQuery(
                "SELECT m from User m " +
                        "WHERE m.externalId = :externalId"
        );
        query.setParameter("externalId", externalId);
        return (User) query.getSingleResult();
    }

    @Transactional(readOnly = true)
    public ChatBox getChatBox(int id) {
        return entityManager.find(ChatBox.class, id);
    }

    @Transactional(readOnly = true)
    public ChatBox getChatBoxWithExternalId(String externalId) {
        Query query = entityManager.createQuery(
                "SELECT m from ChatBox m " +
                        "WHERE m.externalId = :externalId"
        );
        query.setParameter("externalId", externalId);
        return (ChatBox) query.getSingleResult();
    }

    public Collection<ChatMessage> getMessages(String chatbox) {
        return null;
    }


//    @Transactional(readOnly = true)
//    public Collection<ChatMessage> getMessages(ChatBox chatbox) {
//        Query query = entityManager.createQuery(
//                "SELECT m from ChatMessage m, ChatBoxUser n, ChatBox o " +
//                        "WHERE o.id = :chatboxId " +
//                        "AND m.author = n."
//                        "ORDER BY m.creationTime ASC"
//        );
//        query.setParameter("chatboxId", chatbox.getId());
//        return query.getResultList();
//    }

    @Transactional(readOnly = true)
    public Collection<ChatMessage> getMessagesSince(String chatboxId, String messageId) {
        Query query = entityManager.createQuery(
                "SELECT m from ChatMessage m " +
                        "WHERE m.chatboxId = :chatboxId " +
                        "AND m.creationTime > (SELECT n.creationTime FROM ChatMessage n WHERE n.messageId = :messageId) " +
                        "ORDER BY m.creationTime ASC"
        );
        query.setParameter("chatboxId", chatboxId);
        query.setParameter("messageId", messageId);
        return query.getResultList();
    }

}
