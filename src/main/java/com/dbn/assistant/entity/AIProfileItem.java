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

package com.dbn.assistant.entity;

import com.dbn.assistant.provider.ProviderModel;
import com.dbn.assistant.provider.ProviderType;
import com.dbn.common.state.PersistentStateElement;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdom.Element;

import static com.dbn.common.options.setting.Settings.*;


/**
 * Holder class for profile select items
 *
 * @author Ayoub Aarrasse (Oracle)
 */
@Data
@NoArgsConstructor
public final class AIProfileItem implements PersistentStateElement {
  /**
   * the label of this combo item
   */
  private String name;
  private ProviderType provider;
  private ProviderModel model;
  private boolean enabled = true;
  private boolean selected = false;

  public AIProfileItem(Profile profile) {
    this(profile.getProfileName(), profile.getProvider(), profile.getModel(), profile.isEnabled());
  }

  /**
   * Creates a new combo item
   *
   * @param name the label to be displayed in the combo
   */
  public AIProfileItem(String name, ProviderType provider, ProviderModel model, boolean enabled) {
    this.name = name;
    this.provider = provider;
    this.model = model;
    this.enabled = enabled;
  }

  /**
   * Used to UI fw
   *
   * @return the label
   */
  @Override
  public String toString() {
    return name;
  }

  @Override
  public void readState(Element element) {
    name = stringAttribute(element, "name");
    provider = enumAttribute(element, "provider", ProviderType.class);
    model = enumAttribute(element, "model", ProviderModel.class);
    enabled = booleanAttribute(element, "enabled", enabled);
    selected = booleanAttribute(element, "selected", selected);
  }

  @Override
  public void writeState(Element element) {
    setStringAttribute(element, "name", name);
    setEnumAttribute(element, "provider", provider);
    setEnumAttribute(element, "model", model);
    setBooleanAttribute(element, "enabled", enabled);
    setBooleanAttribute(element, "selected", selected);
  }
}
