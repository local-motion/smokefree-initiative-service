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
     * Compute and return the total number of volunteers.
     * @return total volunteers count
     */
    public long totalVolunteers() {
        return initiativeProjection.playgrounds(null).stream()
                .flatMap(playground -> playground.getVolunteers().stream())
                .distinct()
                .count();
    }



}