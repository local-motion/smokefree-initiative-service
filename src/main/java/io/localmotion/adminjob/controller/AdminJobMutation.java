package io.localmotion.adminjob.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.JobResult;
import io.localmotion.application.DomainException;
import io.localmotion.initiative.controller.InputAcceptedResponse;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.interfacing.graphql.error.ErrorCode;
import io.localmotion.user.projection.ProfileProjection;
import io.micronaut.validation.Validated;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;

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

    @Inject
    private AdminJobController adminJobController;

    public JobResult runAdminJob(RunAdminJobInput input, DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        String userEmail = toContext(env).emailId();

        AdminJobCommandRecord adminJobCommandRecord = adminJobController.readAdminJobCommandRecord();
        if (adminJobCommandRecord == null || !userEmail.equals(adminJobCommandRecord.getOperatorEmail()))
            throw new DomainException(ErrorCode.UNAUTHORIZED.toString(),
                    "User is not authorised to perform this operation");

        return adminJobController.runCurrentCommand(
                input.getValidationCode(),
                input.getRetainCommandFile() != null && input.getRetainCommandFile() == true,
                toContext(env)
        );
    }

    public InputAcceptedResponse deleteAdminCommand(String input, DataFetchingEnvironment env) {
        String userEmail = toContext(env).emailId();

        AdminJobCommandRecord adminJobCommandRecord = adminJobController.readAdminJobCommandRecord();
        if (adminJobCommandRecord == null || !userEmail.equals(adminJobCommandRecord.getOperatorEmail()))
            throw new DomainException(ErrorCode.UNAUTHORIZED.toString(),
                    "User is not authorised to perform this operation");

        adminJobController.deleteCurrentCommand();
        return new InputAcceptedResponse("ok");
    }



    /***********
     * Utility functions
     ************/

    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }

}