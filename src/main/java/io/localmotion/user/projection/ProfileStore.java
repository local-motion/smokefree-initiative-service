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
        Profile prevProfile = profilesById.get(profile.getId());

        if (prevProfile != null) {
            if (prevProfile.getUsername() != null)
                profilesByName.remove(prevProfile.getUsername());
            if (prevProfile.getEmailAddress() != null)
                profilesByEmailAddress.remove(prevProfile.getEmailAddress());
        }
        else {
            // Debug
            if (profilesById.values().stream().anyMatch(p -> p.getEmailAddress() != null && p.getEmailAddress().equals(profile.getEmailAddress())))
                System.out.println(("ERROOOOOOR"));
        }

        profilesById.put(profile.getId(), profile);
        if (profile.getUsername() != null)
            profilesByName.put(profile.getUsername(), profile);
        if (profile.getEmailAddress() != null)
            profilesByEmailAddress.put(profile.getEmailAddress(), profile);
    }

    public void remove(String userId) {
        Profile profile = profilesById.get(userId);
        profilesById.remove(userId);

        if (profile != null) {
            if (profile.getUsername() != null)
                profilesByName.remove(profile.getUsername());
            if (profile.getEmailAddress() != null)
                profilesByEmailAddress.remove(profile.getEmailAddress());
        }
    }
}
