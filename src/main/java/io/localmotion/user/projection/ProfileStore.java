package io.localmotion.user.projection;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;

public class ProfileStore {
    private final Map<String, Profile> profilesById = newConcurrentMap();
    private final Map<String, Profile> profilesByName = newConcurrentMap();
    private final Map<String, Profile> profilesByEmailAddress = newConcurrentMap();

    public Profile getById(String id) {
        return profilesById.get(id);
    }

    public Profile getByName(String userName) {
        return profilesByName.get(userName);
    }

    public Profile getByEmailAddress(String emailAddress) {
        return profilesByEmailAddress.get(emailAddress);
    }

    public Collection<Profile> getAllProfiles() {
        return profilesById.values();
    }


    public void put(Profile profile) {
        profilesById.put(profile.getId(), profile);
        profilesByName.put(profile.getUsername(), profile);
        profilesByEmailAddress.put(profile.getEmailAddress(), profile);
    }

    public void remove(Profile profile) {
        profilesById.remove(profile.getId());
        profilesByName.remove(profile.getUsername());
        profilesByEmailAddress.remove(profile.getEmailAddress());
    }
}
