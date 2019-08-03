package io.localmotion.chatbox.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationId implements Serializable {
    private int chatBox;
    private int user;
}
