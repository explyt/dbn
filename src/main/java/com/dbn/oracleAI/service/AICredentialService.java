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

package com.dbn.oracleAI.service;


import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.oracleAI.DatabaseAssistantManager;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.exceptions.CredentialManagementException;
import com.intellij.openapi.project.Project;

import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * AI profile credential service
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
public interface AICredentialService {
  /**
   * Asynchronously creates a new credential.
   *
   * @throws CredentialManagementException underlying service failed
   */
  CompletableFuture<Void> createCredential(Credential credential);

  /**
   * Asynchronously updates an attributes of existing credential.
   *
   * @throws CredentialManagementException
   */
  CompletableFuture<Void> updateCredential(Credential editedCredential);

  /**
   * Asynchronously lists detailed credential information from the database.
   *
   * @throws CredentialManagementException
   */
  CompletableFuture<List<Credential>> getCredentials();

  /**
   * Asynchronously deletes a specific credential information from the database.
   *
   * @throws CredentialManagementException
   */
  CompletableFuture<Void> deleteCredential(String credentialName);

  /**
   * Asynchronously updates the status (enabled/disabled) of the credential from the database.
   */
  void updateStatus(String credentialName, Boolean isEnabled);

  static AICredentialService getInstance(ConnectionHandler connection) {
    Project project = connection.getProject();
    ConnectionId connectionId = connection.getConnectionId();
    DatabaseAssistantManager manager = DatabaseAssistantManager.getInstance(project);
    return manager.getCredentialService(connectionId);
  }
}
