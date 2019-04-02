package smokefree;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
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
import smokefree.aws.rds.secretmanager.SmokefreeConstants;
import smokefree.domain.*;
import smokefree.graphql.CreateInitiativeInput;
import smokefree.graphql.InputAcceptedResponse;
import smokefree.graphql.JoinInitiativeInput;
import smokefree.graphql.error.ErrorCode;
import smokefree.projection.InitiativeProjection;
import smokefree.projection.Profile;
import smokefree.projection.ProfileProjection;

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
public class Mutation implements GraphQLMutationResolver {
    @Inject
    private CommandGateway gateway;

    @Inject
    private InitiativeProjection initiativeProjection;

    @Inject
    private ProfileProjection profileProjection;


    @SneakyThrows
    public InputAcceptedResponse createInitiative(@Valid CreateInitiativeInput input, DataFetchingEnvironment env) {
        final CreateInitiativeCommand command = new CreateInitiativeCommand(
                input.getInitiativeId(),
                input.getName(),
                input.getType(),
                input.getStatus(),
                new GeoLocation(input.getLat(), input.getLng()));
        validateMaximumPlaygroundCapacity();
        validateDuplicatePlaygroundNames(input.getName());
        validatePlaygroundsRange(new GeoLocation(input.getLat(), input.getLng()), SmokefreeConstants.MAXIMUM_PLAYGROUNDS_DISTANCE);
        final CompletableFuture<String> result = gateway.send(decorateWithMetaData(command, env));
        final InputAcceptedResponse response = InputAcceptedResponse.fromFuture(result);

        final String playgroundId = response.getId();
        return joinInitiative(new JoinInitiativeInput(response.getId()), env);
    }

    @SneakyThrows
    public InputAcceptedResponse joinInitiative(JoinInitiativeInput input, DataFetchingEnvironment env) {
        String citizenId = toContext(env).requireUserId();

        JoinInitiativeCommand cmd = new JoinInitiativeCommand(input.getInitiativeId(), citizenId);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(input.getInitiativeId());
    }

    @SneakyThrows
    public InputAcceptedResponse recordPlaygroundObservation(@Valid RecordPlaygroundObservationCommand input, DataFetchingEnvironment env) {
        if(!(input.getObserver().equals(toContext(env).requireUserId()))) {
            throw new ValidationException("Observer must be equal to the userId");
        }
        gateway.sendAndWait(decorateWithMetaData(input, env));
        return new InputAcceptedResponse(input.getInitiativeId());
    }

    @SneakyThrows
    public InputAcceptedResponse updateChecklist(UpdateChecklistCommand input, DataFetchingEnvironment env) {
        if(!(input.getActor().equals(toContext(env).requireUserId())))
            throw new ValidationException("Actor must be equal to the userId");
        gateway.sendAndWait(decorateWithMetaData(input, env));
        return new InputAcceptedResponse(input.getInitiativeId());
    }


    /***********
     * Playground Manager related functionality
     ************/

    public InputAcceptedResponse claimManagerRole(ClaimManagerRoleCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToBecomeSmokeFree(DecideToBecomeSmokeFreeCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse decideToNotBecomeSmokeFree(DecideToNotBecomeSmokeFreeCommand cmd, DataFetchingEnvironment env) {
        if(initiativeProjection.playground(cmd.getInitiativeId(), toContext(env).requireUserId()).getStatus().equals(Status.stopped)) {
            // throw new DomainException("PLAYGROUND_INITIATIVE_ALREADY_STOPPED", "The Initiative for this playground is already stopped" , "Please contact help line for more details");
            return new InputAcceptedResponse(cmd.getInitiativeId());
        }
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }

    public InputAcceptedResponse commitToSmokeFreeDate(CommitToSmokeFreeDateCommand cmd, DataFetchingEnvironment env) {
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(cmd.getInitiativeId());
    }


    /***********
     * User related functionality
     ************/

    public Profile createUser(String input, DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        String userName = toContext(env).requireUserName();
        String emailAddress = toContext(env).emailId();

        // First check if the user is not already present. If so just ignore the request and return the profile.
        Profile profile = profileProjection.profile(userId);
        if (profile != null)
            return profile;

        // Next check if the user has been logically deleted, in which case we will revive the user
        if (profileProjection.getDeletedProfile(userId) != null) {
            ReviveUserCommand cmd = new ReviveUserCommand(userId);
            gateway.sendAndWait(decorateWithMetaData(cmd, env));
        }
        else {
            CreateUserCommand cmd = new CreateUserCommand(userId, userName, emailAddress);
            try {
                gateway.sendAndWait(decorateWithMetaData(cmd, env));
            }
            catch (Exception e) {
                if (e.getMessage().endsWith(" was already inserted")) {
                    // The user may have
                    // Ignore exception, duplicate createUser request or profile was not up to date resulting in an unnecessary createUser call
                    // Anyhow the user is present, so we can return a success response.
                    // Note that it is possible to due the race conditional as the profiles are updated a-synchronously, that the user is present,
                    // but logically deleted, but the profile was not yet loaded before to detect this. In this, rare case, the end user will just
                    // have to refresh.
                }
                else
                    throw(e);
            }
        }
        return new Profile(userId, userName, emailAddress);
    }

    public InputAcceptedResponse deleteUser(String input, DataFetchingEnvironment env) {
        String userId = toContext(env).requireUserId();
        DeleteUserCommand cmd = new DeleteUserCommand(userId);
        gateway.sendAndWait(decorateWithMetaData(cmd, env));
        return new InputAcceptedResponse(userId);
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
                .with(SmokefreeConstants.JWTClaimSet.USER_ID, toContext(env).requireUserId())
                .and(SmokefreeConstants.JWTClaimSet.USER_NAME, toContext(env).requireUserName())
                .and(SmokefreeConstants.JWTClaimSet.EMAIL_ADDRESS, toContext(env).emailId())
                // Added this so support both USER_NAME and COGNITO_USER_NAME, both returns same, will be refactor later
                .and(SmokefreeConstants.JWTClaimSet.COGNITO_USER_NAME, toContext(env).requireUserName());
        return new GenericCommandMessage<>(cmd, metaData);
    }

    // validation for Name, maximum capacity and range
    // To-Do - need to figure out that How to Inject projections into Aggregates

    private void validateMaximumPlaygroundCapacity() {
        if(initiativeProjection.getAllPlaygrounds().size() >= SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED) {
            throw new DomainException(ErrorCode.MAXIMUM_PLAYGROUNDS_CAPACITY_REACHED.toString(),
                    "Can not add more than " + SmokefreeConstants.MAXIMUM_PLAYGROUNDS_ALLOWED + " playgrounds",
                    "Sorry, Maximum playgrounds capacity is reached, please contact helpline");
        }
    }

    private void validateDuplicatePlaygroundNames(String playgroundName) {
        initiativeProjection.getAllPlaygrounds().stream()
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
        initiativeProjection.getAllPlaygrounds().stream()
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