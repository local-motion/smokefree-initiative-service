package io.localmotion.user.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.initiative.controller.InputAcceptedResponse;
import io.localmotion.interfacing.graphql.SecurityContext;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import io.localmotion.user.command.RetrieveUserCommand;
import io.localmotion.user.command.CreateUserCommand;
import io.localmotion.user.command.DeleteUserCommand;
import io.localmotion.user.command.ReviveUserCommand;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.validation.Validated;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.IncompatibleAggregateException;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateNotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;

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

        // First check if the user is not already present. If so just ignore the request and return.
        if (userExists(userId))
            return new InputAcceptedResponse(userId);

        // Next check if the user has been logically deleted, in which case we will revive the user
        if (profileProjection.getDeletedProfile(userId) != null) {
            ReviveUserCommand cmd = new ReviveUserCommand(userId);
            gateway.sendAndWait(decorateWithMetaData(cmd, env));
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


    /***********
     * Utility functions
     ************/

    /**
     * Check for the existence of a user (to avoid race conditions when checking using a projection)
     * @param userId of the user to check
     * @return true if user exists
     */
    private boolean userExists(String userId) {
        try {
            gateway.sendAndWait(new RetrieveUserCommand(userId));
            return true;
        }
        catch (AggregateNotFoundException e) {
            return false;
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