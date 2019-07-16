package io.localmotion.user.aggregate;

import com.google.gson.Gson;
import io.localmotion.application.DomainException;
import io.localmotion.user.command.*;
import io.localmotion.user.domain.UserPII;
import io.localmotion.user.event.*;
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

    private Instant deletionTimestamp = null;   // When not equal to null the user is 'logically' deleted leaving the option to rejoin

    // 'Injecting' using the application context
    private PersonalDataRepository personalDataRepository = Application.getApplicationContext().getBean(PersonalDataRepository.class);
    private ProfileProjection profileProjection = Application.getApplicationContext().getBean(ProfileProjection.class);



    /*
               Properties
     */
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
        String piiString = new Gson().toJson(userPII);

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
        validatePersonalDataNotRemoved(name);
        validateUsernameIsUnique(cmd.getUserName());
        validateEmailAddressIsUnique(emailAddress);

        // Allow for a cooldown period where the user cannot be revived after having been deleted. This to avoid race situations where
        // the user is revived again after just having been deleted, while the user credentials (Cognito) have already been destroyed.
        validateRevivalCooldownPeriod();

        apply(new UserRevivedEvent(cmd.getUserId(), cmd.getUserName() == null || name.equals(cmd.getUserName()) ? null : cmd.getUserName()), metaData);
    }

    @CommandHandler
    public void deleteUser(DeleteUserCommand cmd, MetaData metaData) {
        if (!isDeleted())
            apply(new UserDeletedEvent(cmd.getUserId()), metaData);
    }

    @CommandHandler
    public int deletePersonalData(DeletePersonalDataCommand cmd, MetaData metaData) {
        if (!isDeleted())
            throw new DomainException("USER_NOT_DELETED", "Cannot deleted personal data as the user is still active");

        apply(new PersonalDataDeletedEvent(cmd.getUserId()), metaData);
        int deletedCount = personalDataRepository.deleteRecordsOfPerson(cmd.getUserId());

        return deletedCount;
    }

    @CommandHandler
    public void setNotificationPreferences(SetNotificationPreferencesCommand cmd, MetaData metaData) {
        apply(new NotificationSettingsUpdatedEvent(cmd.getUserId(), cmd.getNotificationLevel()), metaData);
    }


    /*
               Validations
     */


    private void validatePersonalDataNotRemoved(String username) {
        if (username == null) {
            throw new DomainException("PERSONAL_DATA_REMOVED",
                    "The user's personal data has been removed");
        }
    }

    private void validateUsernameIsUnique(String username) {
        if (profileProjection.getProfileByName(username) != null) {
            throw new DomainException("DUPLICATE_USERNAME",
                    "A user with the name " + username + " already exists");
        }
    }

    private void validateEmailAddressIsUnique(String emailAddress) {
        if (profileProjection.emailExists(emailAddress)) {
            throw new DomainException("DUPLICATE_EMAIL_ADDRESS",
                    "A user with the email address " + emailAddress + " already exists");
        }
    }

    private void validateRevivalCooldownPeriod() {
        if (deletionTimestamp == null || System.currentTimeMillis() < deletionTimestamp.toEpochMilli() + REVIVAL_COOLDOWN_PERIOD*1000 )
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
        PersonalDataRecord personalDataRecord = personalDataRepository.getRecord(evt.getPiiRecordId());
        if (personalDataRecord != null) {
            UserPII userPII = new Gson().fromJson(personalDataRecord.getData(), UserPII.class);
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
        deletionTimestamp = null;
        name = evt.getUserName() != null ? evt.getUserName() : name;
    }

    @EventSourcingHandler
    void on(UserDeletedEvent evt, EventMessage<?> eventMessage) {
        log.info("ON EVENT {}", evt);
        deletionTimestamp = eventMessage.getTimestamp();
    }

}

