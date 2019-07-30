package io.localmotion.chatbox.model;

import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity(name="chat_message")
public class ChatMessageV2 {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "creation_time", nullable = false)
    private Instant creationTime = Instant.now();

    @Column(name = "text", nullable = false)
    @NotBlank(message = "Message must have at least " + SmokefreeConstants.ChatBox.MINIMUM_MESSAGE_LENGTH + " characters")
    //@Pattern(regexp = "^[A-Za-z0-9\\n!@&(),.?\": ]+$", message = "Please enter only allowed special charaxters: @&(),.?\": ")
    @Size(max = SmokefreeConstants.ChatBox.MAXIMUM_MESSAGE_LENGTH, message = "Message length must not exceed {max} characters")
    private String text;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "moderated", nullable = false)
    private boolean moderated = false;

    // Last modification time of this entity
    @Column(name = "last_update", nullable = false)
    private Instant lastUpdateTime = Instant.now();


    /*
        Relationships
     */

    @ManyToOne(targetEntity = User.class, optional=false)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;

    @ManyToOne(targetEntity = ChatBox.class, optional=false)
    @JoinColumn(name = "chat_box_id", referencedColumnName = "id")
    private ChatBox chatBox;

}
