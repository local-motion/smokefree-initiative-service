package io.localmotion.user.aggregate;

import com.google.gson.Gson;
import io.localmotion.eventsourcing.axon.MetaDataManager;
import io.localmotion.user.command.CreateUserCommand;
import io.localmotion.user.command.DeleteUserCommand;
import io.localmotion.user.command.ReviveUserCommand;
import io.localmotion.user.domain.UserPII;
import io.localmotion.user.event.UserCreatedEvent;
import io.localmotion.user.event.UserDeletedEvent;
import io.localmotion.user.event.UserRevivedEvent;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.Assert;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;
import io.localmotion.personaldata.PersonalDataRecord;
import io.localmotion.personaldata.PersonalDataRepository;
import io.localmotion.application.Application;


import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class User {
    @AggregateIdentifier
    private String id;
    private String name;
    private String emailAddress;

    private boolean deleted = false;            // Users are deleted 'logically' leaving the option to rejoin
    
    // 'Injecting' using the application context
    private PersonalDataRepository personalDataRepository = Application.getApplicationContext().getBean(PersonalDataRepository.class);
  
    /*
               Commands
     */
    @CommandHandler
    public User(CreateUserCommand cmd, MetaData metaData) {
        // Verify that the data attributes in the command comply with the session data as users are only allowed to create
        // a user record on their own behalf. (Note that a new user will first enroll into Cognito and then fire off
        // the CreateUserCommand, so some user details will already be contained in the JWT token and passed on in the metadata.

        MetaDataManager metaDataManager = new MetaDataManager(metaData);

        Assert.isTrue(
                cmd.getUserId().equals(metaDataManager.getUserId()) &&
                cmd.getName().equals(metaDataManager.getUserName()) &&
                cmd.getEmailAddress().equals(metaDataManager.getEmailAddress())
        ,      () -> "ILLEGAL_UPDATE, CreateUserCommand invoked with attributes that do not match the user's attributes");

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
     * A deleted user can be revived using the original userid
     */
    @CommandHandler
    public void reviveUser(ReviveUserCommand cmd, MetaData metaData) {

        // Ignore if the user is still active
        if (!deleted)
            return;

        deleted = false;
        apply(new UserRevivedEvent(cmd.getUserId()), metaData);
    }

    @CommandHandler
    public void deleteUser(DeleteUserCommand cmd, MetaData metaData) {
        apply(new UserDeletedEvent(cmd.getUserId()), metaData);
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
        deleted = false;
    }

    @EventSourcingHandler
    void on(UserDeletedEvent evt) {
        log.info("ON EVENT {}", evt);
        deleted = true;
    }

}

