package io.localmotion.initiative.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.initiative.command.CreateInitiativeCommand;
import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.initiative.command.UpdateChecklistCommand;
import io.localmotion.initiative.controller.CreateInitiativeInput;
import io.localmotion.initiative.controller.InputAcceptedResponse;
import io.localmotion.initiative.controller.JoinInitiativeInput;
import io.localmotion.initiative.domain.GeoLocation;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.interfacing.graphql.SecurityContext;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.validation.Validated;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
@NoArgsConstructor
@Validated
@SuppressWarnings("unused")
public class InitiativeMutation implements GraphQLMutationResolver {
    @Inject
    private CommandGateway gateway;

    @Inject
    private InitiativeProjection initiativeProjection;

    @Inject
    private ProfileProjection profileProjection;


    @SneakyThrows
    public InputAcceptedResponse createInitiative(@Valid CreateInitiativeInput input, DataFetchingEnvironment env) {
        final CreateInitiativeCommand command = new CreateInitiativeCommand(
                input.getInitiativeId(),
                input.getName(),
                input.getType(),
                input.getStatus(),
                new GeoLocation(input.getLat(), input.getLng()));
        final CompletableFuture<String> result = gateway.send(decorateWithMetaData(command, env));
        final String playgroundId = result.get();
        return joinInitiative(new JoinInitiativeInput(playgroundId), env);
    }

    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input, DataFetchingEnvironment env) {
        String citizenId = toContext(env).requireUserId();

        JoinInitiativeCommand cmd = new JoinInitiativeCommand(input.getInitiativeId(), citizenId);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(input.getInitiativeId());
    }


    @SneakyThrows
    public InputAcceptedResponse updateChecklist(UpdateChecklistCommand input, DataFetchingEnvironment env) {
        if(!(input.getActor().equals(toContext(env).requireUserId())))
            throw new ValidationException("Actor must be equal to the userId");
        gateway.sendAndWait(decorateWithMetaData(input, env));
        return new InputAcceptedResponse(input.getInitiativeId());
    }


    /***********
     * Utility functions
     ************/

    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }

    private String getUserId(DataFetchingEnvironment env) {
        return toContext(env).userId();
    }

    private GenericCommandMessage<?> decorateWithMetaData(Object cmd, DataFetchingEnvironment env) {
        MetaData metaData = MetaData
                .with(SmokefreeConstants.JWTClaimSet.USER_ID, toContext(env).requireUserId())
                .and(SmokefreeConstants.JWTClaimSet.USER_NAME, toContext(env).requireUserName())
                .and(SmokefreeConstants.JWTClaimSet.EMAIL_ADDRESS, toContext(env).emailId())
                // Added this so support both USER_NAME and COGNITO_USER_NAME, both returns same, will be refactor later
                .and(SmokefreeConstants.JWTClaimSet.COGNITO_USER_NAME, toContext(env).requireUserName());
        return new GenericCommandMessage<>(cmd, metaData);
    }

}