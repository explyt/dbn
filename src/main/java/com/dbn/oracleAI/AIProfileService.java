/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI;

import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.ProfileManagementException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Profile management service
 * Features load, create, update and delete utilities for Database AI-Profiles
 *
 * @author Emmanuel Jannetti (emmanuel.jannetti@oracle.com)
 */
public interface AIProfileService extends ManagedObjectService <Profile> {


    CompletableFuture<Profile>  get(String uuid);

    /**
     * Supplies the AI profile map of the current connection
     *
     * @return a map of profile by profile name. can be empty but not null
     */
    CompletableFuture<List<Profile>> list();
    /**
     * Drops a profile on the remote server  asynchronously
     *
     * @param uuid the identifier of the profile to be deleted
     */
    CompletableFuture<Void> delete(String uuid);

    /**
     * Creates a profile on the remote server  asynchronously
     *
     * @param profile the profile to be created
     */
    CompletionStage<Void> create(Profile profile);

    /**
     * Updates a profile on the remote server  asynchronously
     *
     * @param updatedProfile the updated profile attributes
     */
    CompletionStage<Void> update(Profile updatedProfile);


    class CachedProxy extends ManagedObjectServiceProxy<Profile> implements AIProfileService {
        public CachedProxy(AIProfileService backend) {
            super(backend);
        }
    }

    static AIProfileService getInstance(ConnectionHandler connection) {
        Project project = connection.getProject();
        ConnectionId connectionId = connection.getConnectionId();
        DatabaseOracleAIManager manager = DatabaseOracleAIManager.getInstance(project);
        return manager.getProfileService(connectionId);
    }
}
