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
@Entity(name="CHATBOX")
public class ChatBox {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "EXTERNAL_ID")
    private String externalId;

    // Last modification time of this entity
    @Column(name = "LAST_UPDATE", nullable = false)
    private Date lastUpdateTime = new Date();


    /*
        Relationships
     */

    @OneToMany(mappedBy="chatBox",targetEntity= ChatBoxUser.class, fetch=FetchType.EAGER)
    private Collection<ChatBoxUser> users;

}
