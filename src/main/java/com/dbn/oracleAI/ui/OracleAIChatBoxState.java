package com.dbn.oracleAI.ui;

import com.dbn.oracleAI.AIProfileItem;
import com.dbn.oracleAI.types.AuthorType;
import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.types.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class represents the state of the OracleAIChatBox.
 * It encapsulates the current profiles, selected profile, the current question text,
 * a history of questions, the AI answers, and the current connection.
 */
@Builder
@Setter
@Getter
@AllArgsConstructor
/**
 * Chatbox state holder class
 */
public class OracleAIChatBoxState {
  private List<AIProfileItem> profiles;
  private AIProfileItem selectedProfile;
  private String currentQuestionText;
  private List<String> questionHistory;
  private List<ChatMessage> aiAnswers;
  private String currConnection;

  public static final short MAX_CHAR_MESSAGE_COUNT = 3;

  /**
   * Converts the state of the OracleAIChatBox into an XML Element.
   *
   * @return Element representing the state of the OracleAIChatBox.
   */
  public Element toElement() {
    Element stateElement = new Element("OracleAIChatBoxState");
    stateElement.addContent(new Element("currConnection").setText(currConnection));
    stateElement.addContent(new Element("selectedProfile").addContent(selectedProfile != null ? createProfileElement(selectedProfile) : null));
    stateElement.addContent(new Element("currentQuestionText").setText(currentQuestionText));

    Element profilesElement = new Element("profiles");
    profiles.forEach(profile -> {
      profilesElement.addContent(createProfileElement(profile));
    });
    stateElement.addContent(profilesElement);


    Element messagesElement = new Element("chatMessages");
    aiAnswers.forEach(chatMessage -> {
      Element messageElement = new Element("chatMessage");
      messageElement.setAttribute("message", chatMessage.getMessage());
      messageElement.setAttribute("author", chatMessage.getAuthor().toString());
      messagesElement.addContent(messageElement);
    });
    stateElement.addContent(messagesElement);

    return stateElement;
  }

  private Element createProfileElement(AIProfileItem profile) {
    Element profileElement = new Element("profile");
    profileElement.setAttribute("label", profile.getLabel());
    profileElement.setAttribute("effective", String.valueOf(profile.isEffective()));
    profileElement.setAttribute("provider", profile.getProvider().toString());
    profileElement.setAttribute("model", profile.getModel().toString());
    return profileElement;
  }

  /**
   * Reconstructs the OracleAIChatBoxState from an XML Element.
   *
   * @param stateElement XML element representing the state.
   * @return OracleAIChatBoxState reconstructed from the XML.
   */
  public static OracleAIChatBoxState fromElement(Element stateElement) {
    String currConnection = stateElement.getChildText("currConnection");

    // Retrieve the selectedProfile element and handle possible null
    Element selectedProfileElement = stateElement.getChild("selectedProfile");
    AIProfileItem selectedProfileLabel = null;
    if (selectedProfileElement != null) {
      selectedProfileLabel = createAIProfileItem(selectedProfileElement.getChildren().get(0));
    }

    String currentQuestionText = stateElement.getChildText("currentQuestionText");

    List<AIProfileItem> profiles = stateElement.getChild("profiles").getChildren("profile").stream()
        .map(OracleAIChatBoxState::createAIProfileItem)
        .collect(Collectors.toList());

    List<ChatMessage> chatMessages = stateElement.getChild("chatMessages").getChildren("chatMessage").stream()
        .map(messageElement -> new ChatMessage(
            messageElement.getAttributeValue("message"),
            AuthorType.valueOf(messageElement.getAttributeValue("author"))))
        .collect(Collectors.toList());

    return OracleAIChatBoxState.builder()
        .currConnection(currConnection)
        .selectedProfile(selectedProfileLabel)
        .currentQuestionText(currentQuestionText)
        .profiles(profiles)
        .aiAnswers(chatMessages)
        .build();
  }

  private static AIProfileItem createAIProfileItem(Element profileElement) {
    if (profileElement.getAttributeValue("label") == null) return null;
    String label = profileElement.getAttributeValue("label");
    boolean effective = Boolean.parseBoolean(profileElement.getAttributeValue("effective"));
    ProviderType provider = ProviderType.valueOf(profileElement.getAttributeValue("provider").toUpperCase());
    ProviderModel model = ProviderModel.valueOf(profileElement.getAttributeValue("model"));
    return new AIProfileItem(label, provider, model, effective);

  }
}
