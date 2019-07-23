package io.localmotion.user.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.application.DomainException;
import io.localmotion.initiative.controller.InputAcceptedResponse;
import io.localmotion.interfacing.graphql.error.ErrorCode;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import io.localmotion.user.aggregate.User;
import io.localmotion.user.command.*;
import io.localmotion.user.domain.ProfileStatus;
import io.localmotion.user.projection.Profile;
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
        SecurityContext securityContext = toContext(env);

        Profile toBeCreatedProfile = securityContext.getProfile();
        CreateUserCommand cmd = new CreateUserCommand(toBeCreatedProfile.getId(), toBeCreatedProfile.getUsername(), toBeCreatedProfile.getEmailAddress());



        // During system startup a (still logged in) user could access the system while the profile has not been loaded in the projection
        // yet causing the front-end to submit a createUser command. Therefore we need to check whether the aggregate actually does not
        // exist. If so, send an appropriate signal to the frontend.
        // Note that this will be rare as the GraphQLController not checks for the projections to be up-to-date before allowing mutations in the system.


        // Note that we are doing this check here as opposed to just letting the event creation fail in the aggregate to avoid
        // the User aggregate from create PII records prior to the event failing.
        // NOTE: This will only work for users that have not been revived before. For revived (Cognito) users the userid from the authentication
        // object will differ from the aggregate id, so no collision can be detected.
        if (tryRetrieveUser(cmd.getUserId()) != null)
            throw new DomainException("USER_PROFILE_ALREADY_BEING_CREATED", "User is already registered and profile will be created");

        // This check should be done after the tryCreateUser to prevent it from triggering if the user is already active
        validateProfileIsReadyToBeCreated(securityContext);

        try {
            gateway.sendAndWait(decorateWithMetaDataFutureUser(cmd, env));
        }
        catch (Exception e) {
            if (e.getMessage().endsWith(" was already inserted"))
                throw new DomainException("USER_PROFILE_ALREADY_BEING_CREATED", "User is already registered and profile will be created");
            else
                throw(e);
        }

        return new InputAcceptedResponse(toBeCreatedProfile.getId());
    }

    public InputAcceptedResponse reviveUser(String input, DataFetchingEnvironment env) {
        SecurityContext securityContext = toContext(env);

        validateProfileIsReadyToBeRevived(securityContext);

        Profile toBeCreatedProfile = securityContext.getProfile();
        ReviveUserCommand cmd = new ReviveUserCommand(toBeCreatedProfile.getId(), securityContext.getNewUserName());
        gateway.sendAndWait(decorateWithMetaDataFutureUser(cmd, env));

        return new InputAcceptedResponse(toBeCreatedProfile.getId());
    }

//    public InputAcceptedResponse createUser(String input, DataFetchingEnvironment env) {
//        String userId = toContext(env).requireUserId();
//        String userName = toContext(env).requireUserName();
//        String emailAddress = toContext(env).emailId();
//
//        // First check if the user is not already present. If so, revive if deleted or just ignore the request and return.
//        User user = tryRetrieveUser(userId);
//        if (user != null) {
//            if (user.isDeleted()) {
//                ReviveUserCommand cmd = new ReviveUserCommand(userId);
//                gateway.sendAndWait(decorateWithMetaData(cmd, env));
//            }
//            return new InputAcceptedResponse(userId);
//        }
//        else {
//            CreateUserCommand cmd = new CreateUserCommand(userId, userName, emailAddress);
//            try {
//                gateway.sendAndWait(decorateWithMetaData(cmd, env));
//            }
//            catch (Exception e) {
//                if (e.getMessage().endsWith(" was already inserted")) {
//                    // Ignore exception, duplicate createUser request or profile was not up to date resulting in an unnecessary createUser call
//                    // Anyhow the user is present, so we can return a success response.
//                    // Note that it is possible to due the race condition as the profiles are updated a-synchronously, that the user is present,
//                    // but logically deleted, but the profile was not yet loaded before to detect this. In this, rare case, the end user will just
//                    // have to refresh.
//                }
//                else
//                    throw(e);
//            }
//        }
//        return new InputAcceptedResponse(userId);
//    }
//
    public InputAcceptedResponse deleteUser(String input, DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        DeleteUserCommand cmd = new DeleteUserCommand(userId);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(userId);
    }

    public InputAcceptedResponse changeUserName(String newName, DataFetchingEnvironment env) {
        SecurityContext securityContext = toContext(env);

        validateProfileIsReadyToBeRenamedTo(securityContext, newName);

        Profile profile = securityContext.getProfile();
        RenameUserCommand cmd = new RenameUserCommand(profile.getId(), newName);
        gateway.sendAndWait(decorateWithMetaDataFutureUser(cmd, env));

        return new InputAcceptedResponse(profile.getId());
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
        if (!actor.equals(userId)) {
            throw new DomainException(ErrorCode.UNAUTHORIZED.toString(),
                    "User is not authorised to perform this operation");
        }
    }

    private void validateProfileIsReadyToBeCreated(SecurityContext securityContext) {
        if (securityContext.getProfileStatus() != ProfileStatus.NEW) {
            throw new DomainException("INVALID_PROFILE_STATUS",
                    "A new user cannot be created (profile status: " + securityContext.getProfileStatus() + ")");
        }
    }

    private void validateProfileIsReadyToBeRevived(SecurityContext securityContext) {
        if ( !(securityContext.getProfileStatus() == ProfileStatus.DELETED || securityContext.getProfileStatus() == ProfileStatus.DELETED_USER_NAME_CHANGED) ) {
            throw new DomainException("INVALID_PROFILE_STATUS",
                    "A user cannot be revived");
        }
    }

    private void validateProfileIsReadyToBeRenamedTo(SecurityContext securityContext, String newName) {
        if ( !(securityContext.getProfileStatus() == ProfileStatus.ACTIVE_USER_NAME_CHANGED && securityContext.getNewUserName().equals(newName)) ) {
            throw new DomainException("CANNOT_RENAME_USER",
                    "The user's name cannot be changed");
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

    private GenericCommandMessage<?> decorateWithMetaDataFutureUser(Object cmd, DataFetchingEnvironment env) {
        MetaData metaData = MetaData
                .with(SmokefreeConstants.JWTClaimSet.USER_ID, toContext(env).getProfile().getId());
        return new GenericCommandMessage<>(cmd, metaData);
    }

}
