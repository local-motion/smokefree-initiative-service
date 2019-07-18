package io.localmotion.smokefreeplaygrounds.controller;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.smokefreeplaygrounds.projection.Playground;
import io.localmotion.smokefreeplaygrounds.projection.PlaygroundProjection;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class PlaygroundQuery implements GraphQLQueryResolver {

    @Inject
    private PlaygroundProjection playgroundProjection;


    public Collection<Playground> playgrounds(DataFetchingEnvironment env) {
        return playgroundProjection.playgrounds(getUserId(env));
    }

    public Playground playground(String id, DataFetchingEnvironment env) {
        return playgroundProjection.playground(id, getUserId(env));
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