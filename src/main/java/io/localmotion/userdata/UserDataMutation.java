package io.localmotion.userdata;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.google.gson.Gson;
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
/**
 * The UserData Query and Mutation are meant to store data about the user and user-level usage of the system that is
 * not relevant as domain data, but helps to deliver the optimal user experience. The system should always be able
 * to function when this data would not be present.
 *
 * An example is to store the last time that the user has looked at the audit trail so and appropriate signal can be
 * presented when new entries have appeared.
 */
public class UserDataMutation implements GraphQLMutationResolver {

    @Inject
    private UserDataRepository userDataRepository;


    public InputAcceptedResponse storeUserData(UserData userData, DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        String userDataString = new Gson().toJson(userData);
        userDataRepository.store(userId, userDataString);
        return new InputAcceptedResponse(userId);
    }


    /***********
     * Utility functions
     ************/


    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }

}