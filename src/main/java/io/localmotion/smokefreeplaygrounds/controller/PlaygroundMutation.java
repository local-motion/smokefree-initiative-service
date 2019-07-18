package io.localmotion.smokefreeplaygrounds.controller;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import io.localmotion.application.DomainException;
import io.localmotion.initiative.command.JoinInitiativeCommand;
import io.localmotion.initiative.controller.CreateInitiativeInput;
import io.localmotion.initiative.controller.InputAcceptedResponse;
import io.localmotion.security.user.SecurityContext;
import io.localmotion.interfacing.graphql.error.ErrorCode;
import io.localmotion.smokefreeplaygrounds.command.*;
import io.localmotion.smokefreeplaygrounds.domain.CreationStatus;
import io.localmotion.smokefreeplaygrounds.domain.GeoLocation;
import io.localmotion.smokefreeplaygrounds.projection.PlaygroundProjection;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;
import io.micronaut.validation.Validated;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalPosition;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
@NoArgsConstructor
@Validated
@SuppressWarnings("unused")
public class PlaygroundMutation implements GraphQLMutationResolver {
    @Inject
    private CommandGateway gateway;

    @Inject
    PlaygroundProjection playgroundProjection;


    @SneakyThrows
    public InputAcceptedResponse createInitiative(@Valid CreateInitiativeInput input, DataFetchingEnvironment env) {
        final CreatePlaygroundInitiativeCommand command = new CreatePlaygroundInitiativeCommand(
                input.getInitiativeId(),
                input.getName(),
                CreationStatus.ONLINE_NOT_STARTED,
                new GeoLocation(input.getLat(), input.getLng()));
        validateMaximumPlaygroundCapacity();
        validateDuplicatePlaygroundNames(input.getName());
        validatePlaygroundsRange(new GeoLocation(input.getLat(), input.getLng()), SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE);

        final CompletableFuture<String> result = gateway.send(decorateWithMetaData(command, env));
        final String playgroundId = result.get();

        String memberId = toContext(env).requireUserId();
        JoinInitiativeCommand cmd = new JoinInitiativeCommand(input.getInitiativeId(), memberId);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(playgroundId);
    }

    public InputAcceptedResponse claimManagerRole(ClaimManagerRoleCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    @SneakyThrows
    public InputAcceptedResponse recordPlaygroundObservation(@Valid RecordPlaygroundObservationCommand input, DataFetchingEnvironment env) {
        if(!(input.getObserver().equals(toContext(env).requireUserId()))) {
            throw new ValidationException("Observer must be equal to the userId");
        }
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

    // validation for Name, maximum capacity and range
    // To-Do - need to figure out that How to Inject projections into Aggregates

    private void validateMaximumPlaygroundCapacity() {
        if(playgroundProjection.getAllPlaygrounds().size() >= SmokefreeConstants.MAXIMUM_NR_OF_PLAYGROUNDS) {
            throw new DomainException(ErrorCode.MAXIMUM_PLAYGROUNDS_CAPACITY_REACHED.toString(),
                    "Can not add more than " + SmokefreeConstants.MAXIMUM_NR_OF_PLAYGROUNDS + " playgrounds",
                    "Sorry, Maximum playgrounds capacity is reached, please contact helpline");
        }
    }

    private void validateDuplicatePlaygroundNames(String playgroundName) {
        playgroundProjection.getAllPlaygrounds().stream()
                .filter( playground -> playground.getName().equals(playgroundName))
                .findFirst()
                .ifPresent( p -> {
                    throw new DomainException(ErrorCode.DUPLICATE_PLAYGROUND_NAME.toString(),
                            "Playground " + playgroundName + " does already exist, please choose a different name",
                            "Playground name does already exist");
                });

    }

    private void validatePlaygroundsRange(GeoLocation newPlaygroundLocation, long distance) {
        GeodeticCalculator geodeticCalculator = new GeodeticCalculator();
        final Ellipsoid ellipsoidsReference = Ellipsoid.WGS84;
        final GlobalPosition newPlaygroundPosition = new GlobalPosition(newPlaygroundLocation.getLat(), newPlaygroundLocation.getLng(), 0.0);
        playgroundProjection.getAllPlaygrounds().stream()
                .filter( playground -> {
                    GlobalPosition currentPlaygroundPosition = new GlobalPosition(playground.getLat() , playground.getLng(), 0.0);
                    double playgroundsDistance = geodeticCalculator.calculateGeodeticCurve(ellipsoidsReference, currentPlaygroundPosition, newPlaygroundPosition).getEllipsoidalDistance();
                    return  playgroundsDistance < distance;
                })
                .findFirst()
                .ifPresent(p -> {
                    throw new DomainException(ErrorCode.PLAYGROUNS_LOCATED_CLOSELY.toString(),
                            "Two playgrounds can not exist within " + SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE+ " Meters",
                            "playground does already exists within "+ SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE+ " Meters");
                });
    }

}