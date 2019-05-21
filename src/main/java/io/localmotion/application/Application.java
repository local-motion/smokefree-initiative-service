package io.localmotion.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateos.JsonError;
import io.micronaut.http.hateos.Link;
import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Application {

    // Store the application context so classes can use programmatic dependency injection
    private static ApplicationContext applicationContext = null;
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void main(String[] args) {

        applicationContext = Micronaut.run(Application.class);
    }


    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    // Todo this code is deprecated / disfunctional in micronaut 1.1.0, to be fixed ...
//    @Error(global = true)
//    public HttpResponse<JsonError> error(HttpRequest request, Throwable e) {
//        JsonError error = new JsonError(e.getMessage())
//                .link(Link.SELF, Link.of(request.getUri()));
//        log.error("Unhandled exception", e);
//
//        return HttpResponse.<JsonError>serverError()
//                .body(error);
//    }
//
//    @Error(status = HttpStatus.NOT_FOUND, global = true)
//    public HttpResponse<JsonError> notFound(HttpRequest request) {
//        JsonError error = new JsonError("Page Not Found")
//                .link(Link.SELF, Link.of(request.getUri()));
//        log.warn("Page not found [{}]", request.getUri());
//        return HttpResponse.<JsonError>notFound()
//                .body(error);
//    }

}