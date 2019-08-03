package io.localmotion.chatbox.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="chat_box_user")
public class User {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "name", nullable = false)
    @NotBlank
    private String name;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "deleted")
    private boolean deleted;

    // Last modification time of this entity
    @Column(name = "last_update", nullable = false)
    private Instant lastUpdateTime = Instant.now();


    /*
        Relationships
     */

    @OneToMany(mappedBy="user",targetEntity= Participation.class, fetch=FetchType.LAZY)
    private Collection<Participation> participations;

    @Override
    public String toString() {
        return id + "-" + name;
    }
}
