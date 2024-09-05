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

package com.dbn.oracleAI.ui;

import com.dbn.common.Availability;
import com.dbn.common.property.PropertyHolderBase;
import com.dbn.common.state.PersistentStateElement;
import com.dbn.connection.ConnectionId;
import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.model.PersistentChatMessage;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.types.ChatBoxStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dbn.common.options.setting.Settings.*;
import static com.dbn.common.util.Commons.coalesce;
import static com.dbn.common.util.Commons.nvln;
import static com.dbn.common.util.Lists.*;

/**
 * ChatBox state holder class
 * This class represents the state of the OracleAIChatBox.
 * It encapsulates the current profiles, selected profile,
 * a history of questions, the AI answers, and the current connection.
 *
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 * @author Emmanuel Jannetti (emmanuel.jannetti@oracle.com)
 */
@Setter
@Getter
@NoArgsConstructor
public class ChatBoxState extends PropertyHolderBase.IntStore<ChatBoxStatus> implements PersistentStateElement {

  private ConnectionId connectionId;
  private List<AIProfileItem> profiles = new ArrayList<>();
  private List<PersistentChatMessage> messages = new ArrayList<>();
  private ActionAIType selectedAction = ActionAIType.SHOW_SQL;
  private Availability availability = Availability.UNCERTAIN;
  private String defaultProfileName;
  private boolean acknowledged = false;

  public static final short MAX_CHAR_MESSAGE_COUNT = 100;

  public ChatBoxState(ConnectionId connectionId) {
    this.connectionId = connectionId;
  }

  @Override
  protected ChatBoxStatus[] properties() {
    return ChatBoxStatus.VALUES;
  }

  /**
   * State utility indicating the feature is initialized and ready to use
   * @return true if the chat box is properly initialized and can be interacted with
   */
  public boolean available() {
    return isNot(ChatBoxStatus.INITIALIZING) &&
            isNot(ChatBoxStatus.UNAVAILABLE) &&
            isNot(ChatBoxStatus.QUERYING);
  }

  /**
   * State utility indicating the prompting is available.
   * It internally checks if the feature is ready to use by calling {@link #available()} but also checks if a valid profile is selected
   * @return true if prompting is allowed
   */
  public boolean promptingAvailable() {
    if (!available()) return false;

    AIProfileItem profile = getSelectedProfile();
    if (profile == null) return false;
    if (!profile.isEnabled()) return false;

    return true;
  }

  @Nullable
  public AIProfileItem getSelectedProfile() {
    return first(profiles, p -> p.isSelected());
  }

  public void setSelectedProfile(@Nullable AIProfileItem profile) {
    String profileName = profile == null ? null : profile.getName();
    forEach(profiles, p -> p.setSelected(Objects.equals(p.getName(), profileName)));
  }

  /**
   * Replaces the list of profiles by preserving the profile and model selection (as far as possible)
   * @param profiles
   */
  public void setProfiles(List<AIProfileItem> profiles) {
    AIProfileItem selectedProfile = nvln(getSelectedProfile(), firstElement(profiles));
    this.profiles = profiles;
    setSelectedProfile(selectedProfile);
  }

  public Set<String> getProfileNames() {
    return profiles.stream().map(p -> p.getName()).collect(Collectors.toSet());
  }

  @Nullable
  public AIProfileItem getDefaultProfile() {
    // resolve default profile by doing ever less qualified lookup inside the list of profiles
    return coalesce(
            () -> first(profiles, p -> p.isEnabled() && p.getName().equalsIgnoreCase(defaultProfileName)),
            () -> first(profiles, p -> p.isEnabled() && p.isSelected()),
            () -> first(profiles, p -> p.isEnabled()));
  }

  public void setDefaultProfile(@Nullable AIProfileItem profile) {
    defaultProfileName = profile == null? null : profile.getName();
  }

  /**
   * Verifies if the given profile name represents a valid and enabled profile for use as default
   * @param profile the profile to be verified
   * @return true is the profile for the given name exists and is enabled
   */
  public boolean isUsableProfile(AIProfileItem profile) {
    if (profile == null) return false;
    return profiles
            .stream()
            .filter(p -> p.isEnabled())
            .anyMatch(p -> p.getName().equalsIgnoreCase(profile.getName()));
  }

  public void addMessages(List<PersistentChatMessage> messages) {
    this.messages.addAll(messages);
  }


  public void clearMessages() {
    messages.clear();
  }

  @Override
  public void readState(Element element) {
    connectionId = connectionIdAttribute(element, "connection-id");
    acknowledged = booleanAttribute(element, "acknowledged", acknowledged);
    selectedAction = enumAttribute(element, "selected-action", selectedAction);
    availability = enumAttribute(element, "availability", availability);
    defaultProfileName = stringAttribute(element, "default-profile-name");

    List<AIProfileItem> profiles = new ArrayList<>();
    Element profilesElement = element.getChild("profiles");
    List<Element> profileElements = profilesElement.getChildren();
    for (Element profileElement : profileElements) {
      AIProfileItem profile = new AIProfileItem();
      profile.readState(profileElement);
      profiles.add(profile);
    }
    setProfiles(profiles);

    Element messagesElement = element.getChild("messages");
    List<Element> messageElements = messagesElement.getChildren();
    for (Element messageElement : messageElements) {
      PersistentChatMessage message = new PersistentChatMessage();
      message.readState(messageElement);
      messages.add(message);
    }
  }

  @Override
  public void writeState(Element element) {
    setStringAttribute(element, "connection-id", connectionId.id());
    setStringAttribute(element, "default-profile-name", defaultProfileName);
    setBooleanAttribute(element, "acknowledged", acknowledged);
    setEnumAttribute(element, "selected-action", selectedAction);
    setEnumAttribute(element, "availability", availability);

    Element profilesElement = newElement(element, "profiles");
    for (AIProfileItem profile : profiles) {
      Element profileElement = newElement(profilesElement, "profile");
      profile.writeState(profileElement);
    }

    Element messagesElement = newElement(element, "messages");
    for (PersistentChatMessage message : messages) {
      Element messageElement = newElement(messagesElement, "message");
      message.writeState(messageElement);
    }
  }
}
