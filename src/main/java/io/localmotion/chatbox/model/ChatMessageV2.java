package io.localmotion.chatbox.model;

import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity(name="CHATMESSAGE_V2")
public class ChatMessageV2 {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "CREATION_TIME", nullable = false)
    private final Date creationTime = new Date();

    @Column(name = "TEXT", nullable = false)
    @NotBlank(message = "Message must have at least " + SmokefreeConstants.ChatBox.MINIMUM_MESSAGE_LENGTH + " characters")
    //@Pattern(regexp = "^[A-Za-z0-9\\n!@&(),.?\": ]+$", message = "Please enter only allowed special charaxters: @&(),.?\": ")
    @Size(max = SmokefreeConstants.ChatBox.MAXIMUM_MESSAGE_LENGTH, message = "Message length must not exceed {max} characters")
    private String text;

    @Column(name = "DELETED", nullable = false)
    private boolean deleted = false;

    @Column(name = "MODERATED", nullable = false)
    private boolean moderated = false;

    // Last modification time of this entity
    @Column(name = "LAST_UPDATE", nullable = false)
    private Date lastUpdateTime = new Date();


    /*
        Relationships
     */

    @ManyToOne(targetEntity = ChatBoxUser.class, optional=false)
    @JoinColumns({
        @JoinColumn(name = "CHATBOX_ID", referencedColumnName = "CHATBOX"),
        @JoinColumn(name = "USER_ID", referencedColumnName = "USER")
    })
    private ChatBoxUser author;

//    @ManyToOne(targetEntity = User.class, optional=false)
//    @JoinColumn(name = "AUTHOR_ID", referencedColumnName = "ID")
//    private User user;
//
//    @ManyToOne(targetEntity = ChatBox.class, optional=false)
//    @JoinColumn(name = "CHATBOX_ID", referencedColumnName = "ID")
//    private ChatBox chatBox;

}
