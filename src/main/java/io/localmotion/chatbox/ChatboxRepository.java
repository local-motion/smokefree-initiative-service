package io.localmotion.chatbox;

import java.util.Collection;

public interface ChatboxRepository {

	void storeMessage(String chatboxId, ChatMessage chatMessage);

	Collection<ChatMessage> getMessages(String chatboxId);

	Collection<ChatMessage> getMessagesSince(String chatboxId, String messageId);

	ChatMessage getMessageById(String messageId);
}
