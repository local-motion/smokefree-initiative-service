package io.localmotion.initiative.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.initiative.command.UpdateChecklistCommand;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import io.micronaut.validation.Validated;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@NoArgsConstructor
@Validated
@SuppressWarnings("unused")
public class InitiativeMutation implements GraphQLMutationResolver {
    @Inject
    private CommandGateway gateway;


    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input, DataFetchingEnvironment env) {
        String memberId = toContext(env).requireUserId();

        JoinInitiativeCommand cmd = new JoinInitiativeCommand(input.getInitiativeId(), memberId);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(input.getInitiativeId());
    }


    @SneakyThrows
    public InputAcceptedResponse updateChecklist(UpdateChecklistCommand input, DataFetchingEnvironment env) {
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
                .with(SmokefreeConstants.JWTClaimSet.USER_ID, toContext(env).requireUserId());
        return new GenericCommandMessage<>(cmd, metaData);
    }

}