package io.localmotion.user.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.application.DomainException;
import io.localmotion.eventsourcing.axon.MetaDataManager;
import io.localmotion.initiative.controller.InputAcceptedResponse;
import io.localmotion.interfacing.graphql.SecurityContext;
import io.localmotion.interfacing.graphql.error.ErrorCode;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import io.localmotion.user.aggregate.User;
import io.localmotion.user.command.*;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.validation.Validated;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateNotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;

@Slf4j
@Singleton
@NoArgsConstructor
@Validated
@SuppressWarnings("unused")
public class UserMutation implements GraphQLMutationResolver {
    @Inject
    private CommandGateway gateway;

    @Inject
    private ProfileProjection profileProjection;


    public InputAcceptedResponse createUser(String input, DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        String userName = toContext(env).requireUserName();
        String emailAddress = toContext(env).emailId();

        // First check if the user is not already present. If so revive if deleted or just ignore the request and return.
        User user = tryRetrieveUser(userId);
        if (user != null) {
            if (user.isDeleted()) {
                ReviveUserCommand cmd = new ReviveUserCommand(userId);
                gateway.sendAndWait(decorateWithMetaData(cmd, env));
            }
            return new InputAcceptedResponse(userId);
        }
        else {
            CreateUserCommand cmd = new CreateUserCommand(userId, userName, emailAddress);
            try {
                gateway.sendAndWait(decorateWithMetaData(cmd, env));
            }
            catch (Exception e) {
                if (e.getMessage().endsWith(" was already inserted")) {
                    // Ignore exception, duplicate createUser request or profile was not up to date resulting in an unnecessary createUser call
                    // Anyhow the user is present, so we can return a success response.
                    // Note that it is possible to due the race condition as the profiles are updated a-synchronously, that the user is present,
                    // but logically deleted, but the profile was not yet loaded before to detect this. In this, rare case, the end user will just
                    // have to refresh.
                }
                else
                    throw(e);
            }
        }
        return new InputAcceptedResponse(userId);
    }

    public InputAcceptedResponse deleteUser(String input, DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        DeleteUserCommand cmd = new DeleteUserCommand(userId);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(userId);
    }

    public InputAcceptedResponse setNotificationPreferences(SetNotificationPreferencesCommand cmd, DataFetchingEnvironment env) {
        validateActorIsAuthorisedForUser(cmd.getUserId(), env);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getUserId());
    }


    /***********
     * Validations
     ************/

    private void validateActorIsAuthorisedForUser(String userId, DataFetchingEnvironment env) {
        String actor = toContext(env).requireUserId();
        if(!actor.equals(userId)) {
            throw new DomainException(ErrorCode.UNAUTHORIZED.toString(),
                    "User is not authorised to perform this operation");
        }
    }


    /***********
     * Utility functions
     ************/

    /**
     * Check for the existence of a user (to avoid race conditions when checking using a projection)
     * @param userId of the user to check
     * @return the user aggregate if user exists and null otherwise
     */
    private User tryRetrieveUser(String userId) {
        try {
            Object result = gateway.sendAndWait(new RetrieveUserCommand(userId));
            return (User) result;
        }
        catch (AggregateNotFoundException e) {
            return null;
        }
    }

    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }

    private String getUserId(DataFetchingEnvironment env) {
        return toContext(env).userId();
    }

    private GenericCommandMessage<?> decorateWithMetaData(Object cmd, DataFetchingEnvironment env) {
        MetaData metaData = MetaData
                .with(SmokefreeConstants.JWTClaimSet.USER_ID, toContext(env).requireUserId());
        return new GenericCommandMessage<>(cmd, metaData);
    }

}