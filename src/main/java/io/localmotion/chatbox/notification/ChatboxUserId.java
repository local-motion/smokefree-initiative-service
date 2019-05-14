package io.localmotion.chatbox.notification;

import lombok.*;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Embeddable
public class ChatboxUserId implements Serializable {

	@NotBlank
	private String personId;
	@NotBlank
	private String chatBoxId;

}
