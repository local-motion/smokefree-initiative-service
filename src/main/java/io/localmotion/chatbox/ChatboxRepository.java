package io.localmotion.chatbox;

import io.localmotion.chatbox.model.*;
import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;
import org.hibernate.Hibernate;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    /**
     *
     * @return All users without the participations
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return entityManager.createQuery("select u from User u", User.class).getResultList();
    }

    /**
     *
     * @param id of the user to fetch
     * @return user including participations
     */
    @Transactional(readOnly = true)
    public User getUser(int id) {
        try {
            User user = entityManager.find(User.class, id);
            Hibernate.initialize(user.getParticipations());
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     *
     * @param externalId of the user to fetch
     * @return user including participations
     */
    @Transactional(readOnly = true)
    public User getUserWithExternalId(String externalId) {
        try {
            User user = entityManager
                    .createQuery("select m from User m where m.externalId = :externalId", User.class)
                    .setParameter("externalId", externalId)
                    .getSingleResult();
            Hibernate.initialize(user.getParticipations());
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
    public void changeUserName(int userId, String newName) {
        changeUserName(userId, newName, Instant.now());
    }
    @Transactional
    public void changeUserName(int userId, String newName, Instant updateDateTime) {
        User user = entityManager.find(User.class, userId);
        user.setName(newName);
        user.setLastUpdateTime(updateDateTime);
    }

    @Transactional
    public void deleteUser(int userId) {
        deleteUser(userId, Instant.now());
    }

    @Transactional
    public void deleteUser(int userId, Instant updateDateTime) {
        User user = entityManager.find(User.class, userId);
        user.setDeleted(true);
        user.setLastUpdateTime(updateDateTime);
    }


    /*
        ChatBox
     */

    /**
     *
     * @return all chatboxes including participations
     */
    @Transactional(readOnly = true)
    public List<ChatBox> getAllChatBoxes() {
        return entityManager.createQuery("select m from ChatBox m", ChatBox.class).getResultList();
    }

    /**
     *
     * @param id of the chatbox to fetch
     * @return chatbox including participations
     */
    @Transactional(readOnly = true)
    public ChatBox getChatBox(int id) {
        return entityManager.find(ChatBox.class, id);
    }

    /**
     *
     * @param externalId of the chatbox to fetch
     * @return chatbox including participations
     */
    @Transactional(readOnly = true)
    public ChatBox getChatBoxWithExternalId(String externalId) {
        try {
            return entityManager.createQuery(
                "SELECT m from ChatBox  m " +
                        "WHERE m.externalId = :externalId",
                    ChatBox.class
                )
                .setParameter("externalId", externalId)
                .getSingleResult();
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
    public void deleteChatBox(int chatBoxId) {
        deleteChatBox(chatBoxId, Instant.now());
    }
    @Transactional
    public void deleteChatBox(int chatBoxId, Instant updateDateTime) {
        ChatBox chatBox = entityManager.find(ChatBox.class, chatBoxId);
        chatBox.setDeleted(true);
        chatBox.setLastUpdateTime(updateDateTime);
    }


    /*
        Participation
     */

    @Transactional(readOnly = true)
    public Participation getParticipation(int chatBoxId, int userId) {
        try {
        return entityManager.find(Participation.class, new ParticipationId(chatBoxId, userId));
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Participation getParticipation(ParticipationId participationId) {
        try {
        return entityManager.find(Participation.class, participationId);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Collection<Participation> getParticipationsForUser(User user) {

        Collection<Participation> participations = entityManager
                .createQuery(
                "SELECT p from Participation p " +
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
    public Participation createParticipation(int chatBoxId, int userId) {
        return createParticipation(chatBoxId, userId, Instant.now());
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
    public void storeMessage(String externalChatboxId, String externalAuthorId, String text, Instant updateDateTime) {
        storeMessage(
                getChatBoxWithExternalId(externalChatboxId).getId(),
                getUserWithExternalId(externalAuthorId).getId(),
                text,
                updateDateTime
        );
    }

    @Transactional
    public void storeMessage(int chatBoxId, int authorId, String text) {
        storeMessage(chatBoxId, authorId, text, Instant.now());
    }

    @Transactional
    public void storeMessage(int chatBoxId, int authorId, String text, Instant updateDateTime) {
        ChatMessageV2 chatMessage = new ChatMessageV2();
        chatMessage.setChatBox(getChatBox(chatBoxId));
        chatMessage.setAuthor(getUser(authorId));
        chatMessage.setText(text);
        chatMessage.setCreationTime(updateDateTime);
        chatMessage.setLastUpdateTime(updateDateTime);

        entityManager.persist(chatMessage);
    }


    public void storeMessage(String author, ChatMessage chatMessage) {
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessages(int chatBoxId) {
        ChatBox chatBox = getChatBox(chatBoxId);

        List<ChatMessageV2> messages = entityManager.createQuery(
        "SELECT m from ChatMessageV2 m " +
                "WHERE chatBox = :chatBoxId " +
                "ORDER BY m.creationTime ASC"
            )
            .setParameter("chatBoxId", chatBox)
            .getResultList();

        return convertChatMessages(messages);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getMessagesSince(int chatBoxId, String messageId) {

        int id;
        try {
            id = Integer.valueOf(messageId);
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }

        ChatBox chatBox = getChatBox(chatBoxId);

        List<ChatMessageV2> messages = entityManager.createQuery(
        "SELECT m from ChatMessageV2 m " +
                "WHERE chatBox = :chatBoxId " +
                "AND m.creationTime > (SELECT n.creationTime FROM ChatMessageV2 n WHERE n.id = :id) " +
                "ORDER BY m.creationTime ASC"
            )
            .setParameter("chatBoxId", chatBox)
            .setParameter("id", id)
            .getResultList();

        return convertChatMessages(messages);
    }


    private List<ChatMessageDTO> convertChatMessages(Collection<ChatMessageV2> messages) {
        List<ChatMessageDTO> result = new ArrayList<>();
        for (ChatMessageV2 i : messages) {
            result.add(new ChatMessageDTO(
                    i.getId() + "",
                    i.getChatBox().getId() + "",
                    i.getAuthor().getName(),
                    i.getCreationTime(),
                    i.getText()
            ));
        }
        return result;
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
