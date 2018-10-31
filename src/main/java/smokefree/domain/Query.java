package smokefree.domain;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
@NoArgsConstructor
public class Query implements GraphQLQueryResolver {
    @Inject
    QueryGateway queryGateway;

//    public int add(AdditionInput input) {
//        return input.getFirst() + input.getSecond();
//    }
    public List<Playground> playgrounds() {
        final List<Playground> playgrounds = new ArrayList<Playground>();
        playgrounds.add(new Playground("1", "Linnaeushof", 52.327292, 4.603781));
        playgrounds.add(new Playground("2", "Jan Miense Molenaerplein 12", 52.359360, 4.627239));
        return playgrounds;
    }
}