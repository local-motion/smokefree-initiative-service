package smokefree.graphql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserInput {
    String id;          // irrelevant, will not be inspected, but seems to be required to allow GraphQL to function (?)
}