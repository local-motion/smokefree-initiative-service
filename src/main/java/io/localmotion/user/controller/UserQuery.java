package io.localmotion.user.controller;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.user.domain.ProfileStatus;
import io.localmotion.user.projection.Profile;
import io.localmotion.user.projection.ProfileProjection;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class UserQuery implements GraphQLQueryResolver {

    @Inject
    private ProfileProjection profiles;


    public ProfileResponse profile(DataFetchingEnvironment env) {

        SecurityContext securityContext = toContext(env);
        return new ProfileResponse(securityContext.getProfileStatus(), securityContext.getProfile(), securityContext.getNewUserName());

//        switch (securityContext.getProfileStatus()) {
//            case NONE:
//                return new ProfileResponse(securityContext.getProfileStatus(), null, null);
//            case ACTIVE:
//                return new ProfileResponse(securityContext.getProfileStatus(), securityContext.getProfile(), null);
//
//        }

    }

//    public Profile profile(DataFetchingEnvironment env) {
//        String userId = toContext(env).userId();
//        if (userId == null) {
//            return null;
//        }
//        return profiles.profile(userId);
//    }
//
    public boolean emailExists(String emailAddress) {
        return profiles.emailExists(emailAddress);
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

}