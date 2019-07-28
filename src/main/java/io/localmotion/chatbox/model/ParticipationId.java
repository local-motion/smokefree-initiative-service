package io.localmotion.chatbox.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
@EqualsAndHashCode
@AllArgsConstructor
public class ParticipationId implements Serializable {
    private int chatBox;
    private int user;
}
