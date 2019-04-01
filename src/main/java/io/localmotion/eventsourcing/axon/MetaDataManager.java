package io.localmotion.eventsourcing.axon;

import lombok.AllArgsConstructor;
import org.axonframework.messaging.MetaData;
import io.localmotion.application.DomainException;
import io.localmotion.storage.aws.rds.secretmanager.SmokefreeConstants;

import static org.axonframework.common.Assert.assertNonNull;

/**
 * This class manages Axon's metadata object. It can be used to store and retrieve metadata attributes.
 * Note that we are not storing any person identifiable information into the metadata as this will end up in the event
 * store.
 */
@AllArgsConstructor
public class MetaDataManager {
    private MetaData metaData;

    public boolean hasUserData() {
        return  metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID) != null;
    }

    public String getUserId() {
        final String userId = (String) metaData.get(SmokefreeConstants.JWTClaimSet.USER_ID);
        assertNonNull(userId, () -> new DomainException(
                "UNAUTHENTICATED",
                "No userId present",
                "You are not logged in"));
        return userId;
    }

}
