package io.localmotion.chatbox.notification;

import java.util.List;

public interface ChatboxUserRepository {
	List<String> findDistinctPersons();

	List<ChatboxUser> findAllChatBoxesByPerson(String person);

	ChatboxUser findReadMessageIdByPersonAndChatBox(String person, String chatBox);

	ChatboxUser findByMessageId(String readMessageId);

	void update(ChatboxUser notification);

	void create(ChatboxUser newChatboxUser);
}
