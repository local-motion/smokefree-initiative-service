package io.localmotion.chatbox;

import io.localmotion.chatbox.model.*;
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Collection;
import java.util.Date;

@Singleton
public class ChatboxRepository {

//    @PersistenceContext
    private EntityManager entityManager;

    public ChatboxRepository(@CurrentSession EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /*
        User
     */

    @Transactional(readOnly = true)
    public User getUser(int id) {
        return entityManager.find(User.class, id);
    }

    @Transactional(readOnly = true)
    public User getUserWithExternalId(String externalId) {
        try {
            Query query = entityManager.createQuery(
                    "SELECT m from USER m " +
                            "WHERE m.externalId = :externalId"
            );
            query.setParameter("externalId", externalId);
            return (User) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public User createUser(String name, String externalId) {
        User user = new User();
        user.setName(name);
        user.setExternalId(externalId);
        entityManager.persist(user);
        return user;
    }

    @Transactional
    public void changeUserName(User user, String newName) {
        user.setName(newName);
    }

    @Transactional
    public void deleteUser(User user) {
        deleteUser(user, new Date());
    }

    @Transactional
    public void deleteUser(User user, Date updateDateTime) {
        user.setDeleted(true);
        user.setLastUpdateTime(updateDateTime);
    }



    /*
        ChatBox
     */

    @Transactional(readOnly = true)
    public ChatBox getChatBox(int id) {
        return entityManager.find(ChatBox.class, id);
    }

    @Transactional(readOnly = true)
    public ChatBox getChatBoxWithExternalId(String externalId) {
        try {
            Query query = entityManager.createQuery(
                    "SELECT m from CHATBOX  m " +
                            "WHERE m.externalId = :externalId"
            );
            query.setParameter("externalId", externalId);
            return (ChatBox) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    /*
        Participation
     */

    @Transactional(readOnly = true)
    public Participation getParticipation(ChatBox chatBox, User user) {
        return entityManager.find(Participation.class, new ParticipationId(chatBox.getId(), user.getId()));
    }


    /*
        Message
     */

    @Transactional
    public void storeMessage(String externalChatboxId, String externalAuthorId, String text) {
        storeMessage(getChatBoxWithExternalId(externalChatboxId), getUserWithExternalId(externalAuthorId), text);
    }

    @Transactional
    public void storeMessage(ChatBox chatBox, User author, String text) {
        ChatMessageV2 chatMessage = new ChatMessageV2();
        chatMessage.setChatBox(chatBox);
        chatMessage.setAuthor(author);
        chatMessage.setText(text);

        entityManager.persist(chatMessage);
    }


    public void storeMessage(String author, ChatMessage chatMessage) {
    }

    @Transactional(readOnly = true)
    public Collection<ChatMessage> getMessages(ChatBox chatbox) {
        Query query = entityManager.createQuery(
                "SELECT m from ChatMessageV2 " +
                        "WHERE chatBox = :chatboxId " +
                        "ORDER BY m.creationTime ASC"
        );
        query.setParameter("chatboxId", chatbox.getId());
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Collection<ChatMessage> getMessagesSince(ChatBox chatbox, String messageId) {
        Query query = entityManager.createQuery(
                "SELECT m from ChatMessageV2 " +
                        "WHERE chatBox = :chatboxId " +
                        "AND m.creationTime > (SELECT n.creationTime FROM ChatMessageV2 n WHERE n.messageId = :messageId) " +
                        "ORDER BY m.creationTime ASC"
        );
        query.setParameter("chatboxId", chatbox.getId());
        query.setParameter("messageId", messageId);
        return query.getResultList();
    }




//    public Collection<ChatMessage> getMessages(String chatbox) {
//        return null;
//    }
//
//
//
//
//    @Transactional(readOnly = true)
//    public Collection<ChatMessage> getMessagesSince(String chatboxId, String messageId) {
//        Query query = entityManager.createQuery(
//                "SELECT m from ChatMessage m " +
//                        "WHERE m.chatboxId = :chatboxId " +
//                        "AND m.creationTime > (SELECT n.creationTime FROM ChatMessage n WHERE n.messageId = :messageId) " +
//                        "ORDER BY m.creationTime ASC"
//        );
//        query.setParameter("chatboxId", chatboxId);
//        query.setParameter("messageId", messageId);
//        return query.getResultList();
//    }

}
