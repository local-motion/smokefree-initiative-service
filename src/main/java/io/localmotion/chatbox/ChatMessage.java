package io.localmotion.chatbox;

import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class ChatMessage {

    @Id
    private String messageId;

    @NotBlank
    private String chatboxId;

    @NotBlank
    private String author;

    private final Date creationTime = new Date();

    @NotBlank(message = "Message must have at least " + SmokefreeConstants.ChatBox.MINIMUM_MESSAGE_LENGTH + " characters")
    //@Pattern(regexp = "^[A-Za-z0-9\\n!@&(),.?\": ]+$", message = "Please enter only allowed special charaxters: @&(),.?\": ")
    @Size(max = SmokefreeConstants.ChatBox.MAXIMUM_MESSAGE_LENGTH, message = "Message length must not exceed {max} characters")
    private String text;


}
