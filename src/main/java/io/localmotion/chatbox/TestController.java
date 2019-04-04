package io.localmotion.chatbox;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

import javax.inject.Inject;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;

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
