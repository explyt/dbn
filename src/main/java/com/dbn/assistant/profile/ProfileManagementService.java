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

package com.dbn.assistant.profile;

import com.dbn.DatabaseNavigator;
import com.dbn.assistant.profile.adapter.ProfileCreationAdapter;
import com.dbn.assistant.profile.adapter.ProfileDeleteAdapter;
import com.dbn.assistant.profile.adapter.ProfileDisableAdapter;
import com.dbn.assistant.profile.adapter.ProfileEnableAdapter;
import com.dbn.assistant.profile.adapter.ProfileUpdateAdapter;
import com.dbn.common.component.PersistentState;
import com.dbn.object.DBAIProfile;
import com.dbn.object.event.ObjectChangeAction;
import com.dbn.object.management.ObjectManagementAdapterBase;
import com.dbn.object.management.ObjectManagementServiceBase;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dbn.common.component.Components.projectService;

/**
 * Database Assistant profile management component
 * Exposes CRUD-like actions for the {@link com.dbn.object.DBAIProfile} entities
 * Internally instantiates the specialized {@link com.dbn.object.management.ObjectManagementAdapter} component
 * and invokes the MODAL option of the adapter
 *
 * @author Dan Cioca (Oracle)
 */
@Slf4j
@State(
    name = ProfileManagementService.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE))

public class ProfileManagementService extends ObjectManagementServiceBase<DBAIProfile> implements PersistentState {
  public static final String COMPONENT_NAME = "DBNavigator.Project.ProfileManagementService";

  private ProfileManagementService(Project project) {
    super(project, COMPONENT_NAME);
  }

  public static ProfileManagementService getInstance(@NotNull Project project) {
    return projectService(project, ProfileManagementService.class);
  }

  @Nullable
  protected ObjectManagementAdapterBase<DBAIProfile> createAdapter(DBAIProfile profile, ObjectChangeAction action) {
    switch (action) {
      case CREATE: return new ProfileCreationAdapter(profile);
      case UPDATE: return new ProfileUpdateAdapter(profile);
      case DELETE: return new ProfileDeleteAdapter(profile);
      case ENABLE: return new ProfileEnableAdapter(profile);
      case DISABLE: return new ProfileDisableAdapter(profile);
      default: return null;
    }
  }

  /*********************************************
   *            PersistentStateComponent       *
   *********************************************/

  @Override
  public Element getComponentState() {
    return null;
  }

  @Override
  public void loadComponentState(@NotNull Element element) {
  }
}
