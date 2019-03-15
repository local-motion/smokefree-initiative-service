package configuration;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.messaging.MetaData;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.domain.CreateInitiativeCommand;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Playground;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.util.Collection;

import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

/**
 * This class server configuration settings to the front-end. This allows such settings to be
 * configured server-side and per environment.
 */
@Secured(IS_ANONYMOUS)
@Controller("${micronaut.context.path:}/configuration")
public class ConfigurationController {
    @Inject
    Configuration configuration;

    @Get
    public ConfigurationSettings getConfigurationSettings() {

        CognitoSettings cognitoSettings = new CognitoSettings(
                "eu-west-1",
                "eu-west-1_IvXqOMf7v",
                "5c2h1fooc1lvn4ir13k679cj9e",
                "techoverflow-p.auth.eu-west-1.amazoncognito.com",
                "https://techoverflow-p.aws.abnamro.org/onboarding/signin",
                "https://techoverflow-p.aws.abnamro.org/onboarding/logout"
        );
        ConfigurationSettings configurationSettings = new ConfigurationSettings(
                Environment.Dev,
                cognitoSettings
        );
        return configurationSettings;
    }

}
