package smokefree.domain;

import lombok.AllArgsConstructor;
import org.axonframework.messaging.MetaData;
import smokefree.DomainException;
import smokefree.aws.rds.secretmanager.SmokefreeConstants;

import static org.axonframework.common.Assert.assertNonNull;

// TODO migrate all metadata interactions to this class

/**
 * This class manages Axon's metadata object. It can be used to store and retrieve metadata attributes
 */
@AllArgsConstructor
public class MetaDataManager {
    private MetaData metaData;

    public boolean hasUserData() {
        return  metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID) != null &&
                metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME) != null &&
                metaData.get(SmokefreeConstants.JWTClaimSet.EMAIL_ADDRESS) != null;
    }

    public String getUserId() {
        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);
        assertNonNull(userId, () -> new DomainException(
                "UNAUTHENTICATED",
                "No userId present",
                "You are not logged in"));
        return userId;
    }

    public String getUserName() {
        final String userName= (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_NAME);
        assertNonNull(userName, () -> new DomainException(
                "UNAUTHENTICATED",
                "No userName present",
                "You are not logged in"));
        return userName;
    }

    public String getEmailAddress() {
        final String emailAddress= (String) metaData.get(SmokefreeConstants.JWTClaimSet.EMAIL_ADDRESS);
        assertNonNull(emailAddress, () -> new DomainException(
                "UNAUTHENTICATED",
                "No emailAddress present",
                "You are not logged in"));
        return emailAddress;
    }

}
