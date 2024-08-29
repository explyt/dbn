package com.dbn.oracleAI.ui;

import com.dbn.common.Availability;
import com.dbn.common.property.PropertyHolderBase;
import com.dbn.common.state.PersistentStateElement;
import com.dbn.connection.ConnectionId;
import com.dbn.oracleAI.AIProfileItem;
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

import static com.dbn.common.options.setting.Settings.*;
import static com.dbn.common.util.Commons.nvln;
import static com.dbn.common.util.Lists.*;

/**
 * ChatBox state holder class
 * This class represents the state of the OracleAIChatBox.
 * It encapsulates the current profiles, selected profile,
 * a history of questions, the AI answers, and the current connection.
 */
@Setter
@Getter
@NoArgsConstructor
public class OracleAIChatBoxState extends PropertyHolderBase.IntStore<ChatBoxStatus> implements PersistentStateElement {

  private ConnectionId connectionId;
  private List<AIProfileItem> profiles = new ArrayList<>();
  private List<ChatMessage> messages = new ArrayList<>();
  private ActionAIType selectedAction = ActionAIType.SHOW_SQL;
  private boolean acknowledged = false;
  private Availability availability = Availability.UNCERTAIN;


  public static final short MAX_CHAR_MESSAGE_COUNT = 100;

  public OracleAIChatBoxState(ConnectionId connectionId) {
    this.connectionId = connectionId;
  }

  @Override
  protected ChatBoxStatus[] properties() {
    return ChatBoxStatus.VALUES;
  }

  public boolean promptingEnabled() {
    return isNot(ChatBoxStatus.INITIALIZING) &&
            isNot(ChatBoxStatus.UNAVAILABLE) &&
            isNot(ChatBoxStatus.QUERYING);
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

  public void addMessages(List<ChatMessage> messages) {
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
    availability= enumAttribute(element, "availability", availability);

    Element profilesElement = element.getChild("profiles");
    List<Element> profileElements = profilesElement.getChildren();
    for (Element profileElement : profileElements) {
      AIProfileItem profile = new AIProfileItem();
      profile.readState(profileElement);
      profiles.add(profile);
    }

    Element messagesElement = element.getChild("messages");
    List<Element> messageElements = messagesElement.getChildren();
    for (Element messageElement : messageElements) {
      ChatMessage message = new ChatMessage();
      message.readState(messageElement);
      messages.add(message);
    }
  }

  @Override
  public void writeState(Element element) {
    setStringAttribute(element, "connection-id", connectionId.id());
    setBooleanAttribute(element, "acknowledged", acknowledged);
    setEnumAttribute(element, "selected-action", selectedAction);
    setEnumAttribute(element, "availability", availability);

    Element profilesElement = newElement(element, "profiles");
    for (AIProfileItem profile : profiles) {
      Element profileElement = newElement(profilesElement, "profile");
      profile.writeState(profileElement);
    }

    Element messagesElement = newElement(element, "messages");
    for (ChatMessage message : messages) {
      Element messageElement = newElement(messagesElement, "message");
      message.writeState(messageElement);
    }
  }
}
