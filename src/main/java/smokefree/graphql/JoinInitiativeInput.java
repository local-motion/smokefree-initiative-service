package smokefree.graphql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinInitiativeInput {
    private String initiativeId;
    private String citizenId;
}