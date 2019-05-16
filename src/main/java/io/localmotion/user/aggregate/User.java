package io.localmotion.user.aggregate;

import com.google.gson.Gson;
import io.localmotion.application.DomainException;
import io.localmotion.user.command.RetrieveUserCommand;
import io.localmotion.user.command.CreateUserCommand;
import io.localmotion.user.command.DeleteUserCommand;
import io.localmotion.user.command.ReviveUserCommand;
import io.localmotion.user.domain.UserPII;
import io.localmotion.user.event.UserCreatedEvent;
import io.localmotion.user.event.UserDeletedEvent;
import io.localmotion.user.event.UserRevivedEvent;
import io.localmotion.user.projection.ProfileProjection;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.application.Application;


import java.time.Instant;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class User {

    // Constants
    private static final int REVIVAL_COOLDOWN_PERIOD = 60;          // in seconds


    // Member fields

    @AggregateIdentifier
    private String id;
    private String name;
    private String emailAddress;

//    private boolean deleted = false;            // Users are deleted 'logically' leaving the option to rejoin
    private Instant deletionTimestamp = null;   // When not equal to null the user is 'logically' deleted leaving the option to rejoin

    // 'Injecting' using the application context
    private PersonalDataRepository personalDataRepository = Application.getApplicationContext().getBean(PersonalDataRepository.class);
    private ProfileProjection profileProjection = Application.getApplicationContext().getBean(ProfileProjection.class);



    /*
               Properties
     */
//    public boolean isDeleted() {
//        return  deleted;
//    }
    public boolean isDeleted() {
        return  deletionTimestamp != null;
    }

    /*
               Commands
     */
    @CommandHandler
    public User(CreateUserCommand cmd, MetaData metaData) {
        // Verify that no other active users exist with the same username or email address (Should not occurs through normal usage of the LocalMotion frontend)
        validateUsernameIsUnique(cmd.getName());
        validateEmailAddressIsUnique(cmd.getEmailAddress());

        UserPII userPII = new UserPII(cmd.getName(), cmd.getEmailAddress());
        Gson gson = new Gson();
        String piiString = gson.toJson(userPII);

        PersonalDataRecord personalDataRecord = new PersonalDataRecord(cmd.getUserId(), piiString);
        personalDataRepository.storeRecord(personalDataRecord);

        long recordId = personalDataRecord.getRecordId();
        log.info("created pii record " + recordId + " for " + cmd.getUserId() + " with data " + piiString);
        apply(new UserCreatedEvent(cmd.getUserId(), recordId), metaData);
    }

    /**
     * Return this aggregate
     */
    @CommandHandler
    public User retrieveUser(RetrieveUserCommand cmd, MetaData metaData) {
        return this;
    }

    /**
     * A deleted user can be revived using the original userid
     */
    @CommandHandler
    public void reviveUser(ReviveUserCommand cmd, MetaData metaData) {

        // Ignore if the user is still active
        if (!isDeleted())
            return;

        // Verify that no other active users exist with the same username or email address (Should not occurs through normal usage of the LocalMotion frontend)
        validateUsernameIsUnique(name);
        validateEmailAddressIsUnique(emailAddress);

        // Allow for a cooldown period where the user cannot be revived after having been deleted. This to avoid race situations where
        // the user is revived again after just having been deleted, while the user credentials (Cognito) have already been destroyed.
        validateRevivalCooldownPeriod();

        apply(new UserRevivedEvent(cmd.getUserId()), metaData);
    }

    @CommandHandler
    public void deleteUser(DeleteUserCommand cmd, MetaData metaData) {
        if (!isDeleted())
            apply(new UserDeletedEvent(cmd.getUserId()), metaData);
    }


    /*
               Validations
     */


    private void validateUsernameIsUnique(String username) {
        if (profileProjection.getProfileByName(username) != null) {
            throw new DomainException("DUPLICATE_USERNAME",
                    "A user with the name " + username + " already exists",
                    "A user with the name " + username + " already exists");
        }
    }

    private void validateEmailAddressIsUnique(String emailAddress) {
        if (profileProjection.emailExists(emailAddress)) {
            throw new DomainException("DUPLICATE_EMAIL_ADDRESS",
                    "A user with the email address " + emailAddress + " already exists",
                    "A user with the email address " + emailAddress + " already exists");
        }
    }

    private void validateRevivalCooldownPeriod() {
        if (deletionTimestamp != null && System.currentTimeMillis() - deletionTimestamp.toEpochMilli() < REVIVAL_COOLDOWN_PERIOD*1000 )
            throw new DomainException("REVIVAL_COOLDOWN_ACTIVE",
                    "A user cannot be revived within " + REVIVAL_COOLDOWN_PERIOD + " seconds of being deleted");
    }


    /*
               Events
     */

    @EventHandler
    public void on(UserCreatedEvent evt, MetaData metaData) {
        log.info("ON EVENT {}", evt);

        this.id = evt.getUserId();
        if (evt.getPiiRecordId() != 0) {
            PersonalDataRecord personalDataRecord = personalDataRepository.getRecord(evt.getPiiRecordId());
            Gson gson = new Gson();
            UserPII userPII = gson.fromJson(personalDataRecord.getData(), UserPII.class);
            this.name = userPII.getName();
            this.emailAddress = userPII.getEmailAddress();
        }
        else {
            this.name = null;
            this.emailAddress = null;
        }
    }

    @EventSourcingHandler
    void on(UserRevivedEvent evt) {
        log.info("ON EVENT {}", evt);
//        deleted = false;
        deletionTimestamp = null;
    }

    @EventSourcingHandler
    void on(UserDeletedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
//        deleted = true;
        deletionTimestamp = eventMessage.getTimestamp();
    }

}

