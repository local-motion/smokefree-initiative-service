package io.localmotion.chatbox;

import io.localmotion.chatbox.model.*;
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;
import org.hibernate.Hibernate;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
    public List<User> getAllUsers() {
        // Note that a join fetch is required here as otherwise the participations should be lazily loaded, which in practice result in an LazyInitializationException
        return entityManager.createQuery("select u from chat_box_user u join fetch u.participations where u.id > 0   ", User.class).getResultList();
    }

    @Transactional(readOnly = true)
    public User getUser(int id) {
        try {
            // Note that a join fetch is required here as otherwise the participations should be lazily loaded, which in practice result in an LazyInitializationException
//            return entityManager
            User user = entityManager
                    .createQuery("select u from chat_box_user u left join u.participations where u.id = :id", User.class)
//                    .createQuery("select u from chat_box_user u  where u.id = :id", User.class)
                    .setParameter("id", id)
                    .getSingleResult();
            Hibernate.initialize(user.getParticipations());
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public User getUserWithExternalId(String externalId) {
        try {
            Query query = entityManager.createQuery(
                    "SELECT m from chat_box_user m " +
                            "WHERE m.externalId = :externalId " +
                            "AND deleted = false"
            );
            query.setParameter("externalId", externalId);
            User user = (User) query.getSingleResult();
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public User createUser(String name, String externalId) {
        return createUser(name, externalId, Instant.now());
    }
    @Transactional
    public User createUser(String name, String externalId, Instant updateDateTime) {
        User user = new User();
        user.setName(name);
        user.setExternalId(externalId);
        user.setLastUpdateTime(updateDateTime);
        entityManager.persist(user);
        return user;
    }

    @Transactional
    public void changeUserName(User user, String newName) {
        user.setName(newName);
    }
    @Transactional
    public void changeUserName(User u, String newName, Instant updateDateTime) {
        User user = entityManager.find(User.class, u.getId());

        user.setName(newName);
        user.setLastUpdateTime(updateDateTime);
    }

    @Transactional
    public void deleteUser(User user) {
        deleteUser(user, Instant.now());
    }

    @Transactional
    public void deleteUser(User user, Instant updateDateTime) {
        user.setDeleted(true);
        user.setLastUpdateTime(updateDateTime);
    }



    /*
        ChatBox
     */

    @Transactional(readOnly = true)
    public List<ChatBox> getAllChatBoxes() {
        return entityManager.createQuery("select m from chat_box m", ChatBox.class).getResultList();
    }

    @Transactional(readOnly = true)
    public ChatBox getChatBox(int id) {
        return entityManager.find(ChatBox.class, id);
    }

    @Transactional(readOnly = true)
    public ChatBox getChatBoxWithExternalId(String externalId) {
        try {
            Query query = entityManager.createQuery(
                    "SELECT m from chat_box  m " +
                            "WHERE m.externalId = :externalId " +
                            "AND deleted = false"
            );
            query.setParameter("externalId", externalId);
            return (ChatBox) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public ChatBox createChatBox(String externalId) {
        return createChatBox(externalId, Instant.now());
    }
    @Transactional
    public ChatBox createChatBox(String externalId, Instant updateDateTime) {
        ChatBox chatBox = new ChatBox();
        chatBox.setExternalId(externalId);
        chatBox.setLastUpdateTime(updateDateTime);
        entityManager.persist(chatBox);
        return chatBox;
    }

    @Transactional
    public void deleteChatBox(ChatBox chatBox) {
        deleteChatBox(chatBox, Instant.now());
    }
    @Transactional
    public void deleteChatBox(ChatBox chatBox, Instant updateDateTime) {
        chatBox.setDeleted(true);
        chatBox.setLastUpdateTime(updateDateTime);
    }


    /*
        Participation
     */

    @Transactional(readOnly = true)
    public Participation getParticipation(ChatBox chatBox, User user) {
        return entityManager.find(Participation.class, new ParticipationId(chatBox.getId(), user.getId()));
    }

    @Transactional(readOnly = true)
    public Participation getParticipation(int chatBoxId, int userId) {
        return entityManager.find(Participation.class, new ParticipationId(chatBoxId, userId));
    }

    @Transactional(readOnly = true)
    public Participation getParticipation(ParticipationId participationId) {
        return entityManager.find(Participation.class, participationId);
    }

    @Transactional(readOnly = true)
    public Collection<Participation> getParticipationsForUser(User user) {

        Collection<Participation> participations = entityManager
                .createQuery(
                "SELECT p from participation p " +
                        "join fetch p.user " +
                        "join fetch p.chatBox " +
                        "WHERE p.user = :user "

                        ,
                        Participation.class
                )
                .setParameter("user", user)
                .getResultList();
        return participations;
    }

    @Transactional
    public Participation createParticipation(ChatBox chatBox, User user) {
        return createParticipation(chatBox, user, Instant.now());
    }
    @Transactional
    public Participation createParticipation(ChatBox chatBox, User user, Instant updateDateTime) {
        Participation participation = new Participation();
        participation.setChatBox(chatBox);
        participation.setUser(user);
        participation.setLastUpdateTime(updateDateTime);
        entityManager.persist(participation);
        return participation;
    }

    @Transactional
    public Participation createParticipation(int chatBoxId, int userId, Instant updateDateTime) {

        ChatBox chatBox = getChatBox(chatBoxId);
        if (chatBox == null)
            throw new IllegalArgumentException("There exists no chat box with id " + chatBoxId);
        User user = getUser(userId);
        if (user == null)
            throw new IllegalArgumentException("There exists no user with id " + userId);

        Participation participation = new Participation();
        participation.setChatBox(chatBox);
        participation.setUser(user);
        participation.setLastUpdateTime(updateDateTime);
        entityManager.persist(participation);
        return participation;
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
                "SELECT m from chat_message " +
                        "WHERE chatBox = :chatboxId " +
                        "ORDER BY m.creationTime ASC"
        );
        query.setParameter("chatboxId", chatbox.getId());
        return query.getResultList();
    }

    @Transactional(readOnly = true)
    public Collection<ChatMessage> getMessagesSince(ChatBox chatbox, String messageId) {
        Query query = entityManager.createQuery(
                "SELECT m from chat_message " +
                        "WHERE chatBox = :chatboxId " +
                        "AND m.creationTime > (SELECT n.creationTime FROM chat_message n WHERE n.messageId = :messageId) " +
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
