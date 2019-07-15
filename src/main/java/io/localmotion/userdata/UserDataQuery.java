package io.localmotion.userdata;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.google.gson.Gson;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.security.user.SecurityContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
/**
 * The UserData Query and Mutation are meant to store data about the user and user-level usage of the system that is
 * not relevant as domain data, but helps to deliver the optimal user experience. The system should always be able
 * to function when this data would not be present.
 *
 * An example is to store the last time that the user has looked at the audit trail so and appropriate signal can be
 * presented when new entries have appeared.
 */
public class UserDataQuery implements GraphQLQueryResolver {

    @Inject
    private UserDataRepository userDataRepository;


    public UserData userData(DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        String UserDataString = userDataRepository.retrieve(userId);
        return new Gson().fromJson(UserDataString, UserData.class);
    }


    /***********
     * Utility functions
     ************/

    private SecurityContext toContext(DataFetchingEnvironment environment) {
        return environment.getContext();
    }


}