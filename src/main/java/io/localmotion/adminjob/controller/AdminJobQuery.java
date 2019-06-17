package io.localmotion.adminjob.controller;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.adminjob.domain.AdminJobCommandRecord;
import io.localmotion.adminjob.domain.AdminJobController;
import io.localmotion.interfacing.graphql.SecurityContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class AdminJobQuery implements GraphQLQueryResolver {

    @Inject
    private AdminJobController adminJobController;

    public AdminJobCommandRecord adminCommand(DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        String userName = toContext(env).requireUserName();
        String userEmail = toContext(env).emailId();

        return adminJobController.readAdminJobCommandRecord();
    }



    /***********
     * Utility functions
     ************/

    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }


}