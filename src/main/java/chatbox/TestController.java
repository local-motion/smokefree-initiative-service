package chatbox;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Playground;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import javax.validation.constraints.Size;
import java.util.Collection;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Slf4j
@Secured(IS_ANONYMOUS)
@Controller("${micronaut.context.path:}/test")
public class TestController {

    @Inject
//    @Named("axon")
    SessionFactory sessionFactory;

//    @Inject
//            @Named("axon")
//    DataSource dataSource;


    @Get
    public String go() {
//        return "dataSource: " + dataSource;
        return "sessionFactory: " + sessionFactory;
    }

}
