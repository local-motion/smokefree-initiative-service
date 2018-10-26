package smokefree.domain;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@NoArgsConstructor
public class Query implements GraphQLQueryResolver {
    @Inject
    QueryGateway queryGateway;

//    public int add(AdditionInput input) {
//        return input.getFirst() + input.getSecond();
//    }
}