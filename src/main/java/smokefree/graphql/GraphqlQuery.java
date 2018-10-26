package smokefree.graphql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GraphqlQuery {
    String query;
    Map<String, Object> variables;
}
