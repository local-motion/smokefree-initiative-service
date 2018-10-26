package smokefree.graphql;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
public
class GraphqlQuery {
    String query;
    Map<String, Object> variables;
}
