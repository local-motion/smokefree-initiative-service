package smokefree.domain;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.common.Assert;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.messaging.MetaData;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateRoot;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Slf4j
@NoArgsConstructor
@AggregateRoot
public class User {
    @AggregateIdentifier
    private String id;
    private String name;
    private String emailAddress;


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

        apply(new UserCreatedEvent(cmd.getUserId(), cmd.getName(), cmd.getEmailAddress()), metaData);
    }


    /*
               Events
     */

    @EventSourcingHandler
    void on(UserCreatedEvent evt) {
        this.id = evt.getUserId();
        this.name = evt.getName();
        this.emailAddress = evt.getEmailAddress();
    }

}

