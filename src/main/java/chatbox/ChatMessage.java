package chatbox;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChatMessage {
    private String messageId;

    @NotBlank
    private String name;

    private final Date creationTime = new Date();

    @NotBlank
    private String message;


}
