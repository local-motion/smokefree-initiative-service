package io.localmotion.initiative.controller;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import io.localmotion.initiative.projection.InitiativeProjection;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
@NoArgsConstructor
@SuppressWarnings("unused")
public class InitiativeQuery implements GraphQLQueryResolver {

    @Inject
    private InitiativeProjection initiativeProjection;



    /**
     * Compute and return the total number of members.
     * @return total member count
     */
    public long totalVolunteers() {
        return initiativeProjection.getInitiatives(null).stream()
                .flatMap(playground -> playground.getMembers().stream())
                .distinct()
                .count();
    }

}