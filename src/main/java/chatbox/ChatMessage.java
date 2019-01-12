package chatbox;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class ChatMessage {

    @Id
    private String messageId;

    @NotBlank
    private String chatboxId;

    @NotBlank
    private String author;

    private final Date creationTime = new Date();

    @NotBlank
    private String text;


}
