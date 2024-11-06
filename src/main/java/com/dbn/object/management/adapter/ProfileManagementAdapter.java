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

package com.dbn.object.management.adapter;

import com.dbn.object.DBAIProfile;
import com.dbn.object.event.ObjectChangeAction;
import com.dbn.object.management.ObjectManagementAdapter;
import com.dbn.object.management.ObjectManagementAdapterFactory;
import com.dbn.object.management.adapter.profile.ProfileCreationAdapter;
import com.dbn.object.management.adapter.profile.ProfileDeleteAdapter;
import com.dbn.object.management.adapter.profile.ProfileDisableAdapter;
import com.dbn.object.management.adapter.profile.ProfileEnableAdapter;
import com.dbn.object.management.adapter.profile.ProfileUpdateAdapter;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link ObjectManagementAdapterFactory} for objects of type {@link DBAIProfile}
 * @author Dan Cioca (Oracle)
 */
public class ProfileManagementAdapter implements ObjectManagementAdapterFactory<DBAIProfile> {

  @Override
  @Nullable
  public ObjectManagementAdapter<DBAIProfile> createAdapter(DBAIProfile profile, ObjectChangeAction action) {
    switch (action) {
      case CREATE: return new ProfileCreationAdapter(profile);
      case UPDATE: return new ProfileUpdateAdapter(profile);
      case DELETE: return new ProfileDeleteAdapter(profile);
      case ENABLE: return new ProfileEnableAdapter(profile);
      case DISABLE: return new ProfileDisableAdapter(profile);
      default: return null;
    }
  }
}
