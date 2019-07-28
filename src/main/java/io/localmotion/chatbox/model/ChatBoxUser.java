package io.localmotion.chatbox.model;

import lombok.*;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity(name="CHATBOX_USER")
@IdClass(ChatBoxId.class)
public class ChatBoxUser {

    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="CHATBOX", referencedColumnName="ID")
    private ChatBox chatBox;

    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="USER", referencedColumnName="ID")
    private User user;

    // Last modification time of this entity
    @Column(name = "LAST_UPDATE", nullable = false)
    private Date lastUpdateTime = new Date();


    // Soft reference to the most-recent message that was presented to the user
    @Column(name = "LAST_READ_MESSAGE_ID")
    private int lastReadMessageId;

    @Column(name = "LAST_ACCESS_TIME")
    private Date lastAccessTime;

    // Soft reference to the most-recent message that was notified to the user
    @Column(name = "LAST_NOTIFIED_MESSAGE_ID")
    private int lastNotifiedMessageId;



    /*
        Other relationships
     */

    @OneToMany(mappedBy="author",targetEntity= ChatMessageV2.class, fetch=FetchType.LAZY)
    private Collection<ChatMessageV2> messages;

}
