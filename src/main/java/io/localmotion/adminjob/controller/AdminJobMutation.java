package io.localmotion.adminjob.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.application.DomainException;
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

@Slf4j
@Singleton
@NoArgsConstructor
@Validated
@SuppressWarnings("unused")
public class AdminJobMutation implements GraphQLMutationResolver {
    @Inject
    private CommandGateway gateway;

    @Inject
    private ProfileProjection profileProjection;


    public InputAcceptedResponse runAdminJob(String input, DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        String userName = toContext(env).requireUserName();
        String emailAddress = toContext(env).emailId();


        return new InputAcceptedResponse(userId);
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