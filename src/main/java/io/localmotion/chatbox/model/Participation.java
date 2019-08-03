package io.localmotion.chatbox.model;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name="participation")
@IdClass(ParticipationId.class)
public class Participation {

    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="chat_box", referencedColumnName="id")
    private ChatBox chatBox;

    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="chat_box_user", referencedColumnName="id")
    private User user;

    public ParticipationId getParticipationId() {
        return new ParticipationId(chatBox.getId(), user.getId());
    }

    // Last modification time of this entity
    @Column(name = "last_update", nullable = false)
    private Instant lastUpdateTime = Instant.now();


    // Soft reference to the most-recent message that was presented to the user
    @Column(name = "last_read_message_id")
    private int lastReadMessageId;

    @Column(name = "last_access_time")
    private Instant lastAccessTime;

    // Soft reference to the most-recent message that was notified to the user
    @Column(name = "last_notified_message_id")
    private int lastNotifiedMessageId;



    /*
        Other relationships
     */

//    @OneToMany(mappedBy="author",targetEntity= ChatMessageV2.class, fetch=FetchType.LAZY)
//    private Collection<ChatMessageV2> messages;

}
