package io.localmotion.batch.notification;

import io.localmotion.chatbox.ChatMessage;
import io.localmotion.chatbox.ChatboxRepository;
import io.localmotion.chatbox.notification.ChatboxUserRepository;
import io.localmotion.personaldata.IPersonalDataRepository;
import io.micronaut.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Singleton
public class ChatBoxNotificationBatch {

	@Inject
	ChatboxRepository chatboxRepository;

	@Inject
    ChatboxUserRepository chatBoxNotificationRepository;

	@Inject
	IPersonalDataRepository personalDataRepository;



	// job is triggered once a month
	@Scheduled(cron = "0 0 0 1 * ?")
	void notifyUnreadMessages() {
		log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Notification Batch Job Started at: {} ~~~~~~~~~~~~~~~~~~~~~~~~~~~", Instant.now());
		// TODO: get all distinct user ids from ChatNotification table
		List<String> users = chatBoxNotificationRepository.findDistinctPersons();
		log.info("List of Persons : {}", users);
		users.stream()
				.forEach( user -> {
                    UserNotification userNotification = new UserNotification();
					log.info("Sending Notification for Person: {}", user);
					Map<String, List<ChatMessage>> missedMessages = new HashMap<>();
					chatBoxNotificationRepository.findAllChatBoxesByPerson(user)
							.stream()
							.forEach( chatBoxNotification -> {
								log.info("Sending Notification for Person {} for ChatBox {}", user, chatBoxNotification.getChatBoxUserId().getChatBoxId());
								// ChatboxUser chatBoxNotification = chatBoxNotificationRepository.findReadMessageIdByPersonAndChatBox(user, chatBox.getChatBoxUserId().getChatBoxId());
								ChatMessage readChatMessage = chatBoxNotification.getReadMessageId() != null ?
										chatboxRepository.getMessageById(chatBoxNotification.getReadMessageId()): null;
								ChatMessage notifiedChatMessage = chatBoxNotification.getNotifiedMessageId() != null?
										chatboxRepository.getMessageById(chatBoxNotification.getNotifiedMessageId()): null;
								String lastReadOrNotifiedMessage  = computeLastReadOrNotifiedMessage(readChatMessage, notifiedChatMessage);
								log.info("Last Read or Notified Message is {}", lastReadOrNotifiedMessage);
								if(lastReadOrNotifiedMessage != null) {
									List<ChatMessage> messagesToBeNotified = new ArrayList<>(chatboxRepository.getMessagesSince(chatBoxNotification.getChatBoxUserId().getChatBoxId(), lastReadOrNotifiedMessage));
									if(messagesToBeNotified.size() != 0) {
										missedMessages.put(chatBoxNotification.getChatBoxUserId().getChatBoxId(), messagesToBeNotified);
										log.info("Identified missed messages are {}", missedMessages);

										userNotification.setMissedConversions(missedMessages);
										userNotification.setPersonalDataRecord(personalDataRepository.getRecordByPersonId(user));
										log.info("Sending Notification Data to EMail Service {}", userNotification);

										// TODO: Call Email Service API to send an Email

										chatBoxNotification.setNotifiedMessageId(messagesToBeNotified.get(messagesToBeNotified.size() - 1).getMessageId());
										chatBoxNotificationRepository.update(chatBoxNotification);
									}else {
										log.info("There are no messages to notify Person {} in the  ChatBox {}", user, chatBoxNotification);
									}

								} else {
									log.info("There are no messages to notify for Person {} in the Chatbox {}", user, chatBoxNotification);
								}

							});
					missedMessages.clear();
				});
		log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Notification Batch Job  is completed: {}~~~~~~~~~~~~~~~~~~~~~~~~~~~~" , Instant.now());
	}

	private String computeLastReadOrNotifiedMessage(ChatMessage readChatMessage, ChatMessage notifiedChatMessage) {
		if(readChatMessage == null && notifiedChatMessage == null) {
			return null;
		} else if(readChatMessage == null ) {
			return notifiedChatMessage.getMessageId();
		} else if(notifiedChatMessage == null) {
			return readChatMessage.getMessageId();
		} else {
			int messageTimeComparison = readChatMessage.getCreationTime().compareTo(notifiedChatMessage.getCreationTime());
			return messageTimeComparison >= 0 ? readChatMessage.getMessageId(): notifiedChatMessage.getMessageId();
		}
	}
}
