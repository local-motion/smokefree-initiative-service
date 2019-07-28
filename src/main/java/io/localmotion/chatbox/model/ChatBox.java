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
@Entity(name="chatbox")
public class ChatBox {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "external_id")
    private String externalId;

    // Last modification time of this entity
    @Column(name = "last_update", nullable = false)
    private Date lastUpdateTime = new Date();


    /*
        Relationships
     */

    @OneToMany(mappedBy="chatBox",targetEntity= Participation.class, fetch=FetchType.EAGER)
    private Collection<Participation> participations;

}
