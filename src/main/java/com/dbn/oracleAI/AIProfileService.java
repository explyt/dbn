package com.dbn.oracleAI;

import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * AI Profile service
 */
public interface AIProfileService {
    /**
     * Supplies the AI profile map of the current connection
     *
     * @return a map of profile by profile name. can be empty but not null
     * @throws ProfileManagementException
     */
    public CompletableFuture<List<Profile>> getProfiles();
    /**
     * Drops a profile on the remote server  asynchronously
     *
     * @param profileName the name of the profile to be deleted
     * @throws ProfileManagementException
     */
    public CompletableFuture<Void> deleteProfile(String profileName);

    /**
     * Creates a profile on the remote server  asynchronously
     *
     * @param profile the profile to be created
     * @throws ProfileManagementException
     */
    public CompletionStage<Void> createProfile(Profile profile);

    /**
     * Updates a profile on the remote server  asynchronously
     *
     * @param updatedProfile the updated profile attributes
     * @throws ProfileManagementException
     */
    public CompletionStage<Void> updateProfile(Profile updatedProfile);
}
