package io.localmotion.smokefreeplaygrounds.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.initiative.controller.InputAcceptedResponse;
import io.localmotion.initiative.domain.Status;
import io.localmotion.initiative.projection.InitiativeProjection;
import io.localmotion.interfacing.graphql.SecurityContext;
import io.localmotion.smokefreeplaygrounds.command.*;
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
import javax.validation.ValidationException;

@Slf4j
@Singleton
@NoArgsConstructor
@Validated
@SuppressWarnings("unused")
public class PlaygroundMutation implements GraphQLMutationResolver {
    @Inject
    private CommandGateway gateway;

    @Inject
    private InitiativeProjection initiativeProjection;

    @Inject
    private ProfileProjection profileProjection;



    public InputAcceptedResponse claimManagerRole(ClaimManagerRoleCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToNotBecomeSmokeFree(DecideToNotBecomeSmokeFreeCommand cmd, DataFetchingEnvironment env) {
        if(initiativeProjection.playground(cmd.getInitiativeId(), toContext(env).requireUserId()).getStatus().equals(Status.stopped)) {
            // throw new DomainException("PLAYGROUND_INITIATIVE_ALREADY_STOPPED", "The Initiative for this playground is already stopped" , "Please contact help line for more details");
            return new InputAcceptedResponse(cmd.getInitiativeId());
        }
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    @SneakyThrows
    public InputAcceptedResponse recordPlaygroundObservation(RecordPlaygroundObservationCommand input, DataFetchingEnvironment env) {
        if(!(input.getObserver().equals(toContext(env).requireUserId()))) {
            throw new ValidationException("Observer must be equal to the userId");
        }
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