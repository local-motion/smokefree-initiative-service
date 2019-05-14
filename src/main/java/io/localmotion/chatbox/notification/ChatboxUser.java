package io.localmotion.chatbox.notification;

import lombok.*;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
public class ChatboxUser {

	@EmbeddedId
	private ChatboxUserId chatBoxUserId;

	private String readMessageId;

	private String notifiedMessageId;
}
