package io.localmotion.chatbox.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity(name="USER")
public class User {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "NAME", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "EXTERNAL_ID")
    private String externalId;

    // Last modification time of this entity
    @Column(name = "LAST_UPDATE", nullable = false)
    private Date lastUpdateTime = new Date();


    /*
        Relationships
     */

    @OneToMany(mappedBy="user",targetEntity= ChatBoxUser.class, fetch=FetchType.EAGER)
    private Collection<ChatBox> chatBoxes;

}
