package configuration;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;

/**
 * This class server configuration settings to the front-end. This allows such settings to be
 * configured server-side and per environment.
 */
@Slf4j
@Secured(IS_ANONYMOUS)
@Controller("${micronaut.context.path:}/configuration")
public class ConfigurationController {

    @Inject
    ConfigurationSettings configurationSettings;

    @Get
    public ConfigurationSettings getConfigurationSettings() {
        return configurationSettings;
    }

}
