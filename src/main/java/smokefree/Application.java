package smokefree;

import io.micronaut.context.annotation.Bean;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.hateos.JsonError;
import io.micronaut.http.hateos.Link;
import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;
import smokefree.projection.InitiativeProjection;

@Slf4j
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }

    @Bean
    public InitiativeProjection initiativeProjection() {
        return new InitiativeProjection();
    }

    @Error(global = true)
    public HttpResponse<JsonError> error(HttpRequest request, Throwable e) {
        JsonError error = new JsonError(e.getMessage())
                .link(Link.SELF, Link.of(request.getUri()));
        log.error("Unhandled exception", e);

        return HttpResponse.<JsonError>serverError()
                .body(error);
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    public HttpResponse<JsonError> notFound(HttpRequest request) {
        JsonError error = new JsonError("Page Not Found")
                .link(Link.SELF, Link.of(request.getUri()));
        log.warn("Page not found [{}]", request.getUri());
        return HttpResponse.<JsonError>notFound()
                .body(error);
    }
}