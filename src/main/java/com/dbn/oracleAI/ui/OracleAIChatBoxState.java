package com.dbn.oracleAI.ui;

import com.dbn.oracleAI.types.AuthorType;
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
public class OracleAIChatBoxState {
  private List<OracleAIChatBox.AIProfileItem> profiles;
  private OracleAIChatBox.AIProfileItem selectedProfile;
  private String currentQuestionText;
  private List<String> questionHistory;
  private List<ChatMessage> aiAnswers;
  private String currConnection;

  /**
   * Converts the state of the OracleAIChatBox into an XML Element.
   *
   * @return Element representing the state of the OracleAIChatBox.
   */
  public Element toElement() {
    Element stateElement = new Element("OracleAIChatBoxState");
    stateElement.addContent(new Element("currConnection").setText(currConnection));
    stateElement.addContent(new Element("selectedProfile").setText(selectedProfile != null ? selectedProfile.getLabel() : ""));
    stateElement.addContent(new Element("currentQuestionText").setText(currentQuestionText));

    Element profilesElement = new Element("profiles");
    profiles.forEach(profile -> {
      Element profileElement = new Element("profile");
      profileElement.setAttribute("label", profile.getLabel());
      profileElement.setAttribute("effective", String.valueOf(profile.isEffective()));
      profilesElement.addContent(profileElement);
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

  /**
   * Reconstructs the OracleAIChatBoxState from an XML Element.
   *
   * @param stateElement XML element representing the state.
   * @return OracleAIChatBoxState reconstructed from the XML.
   */
  public static OracleAIChatBoxState fromElement(Element stateElement) {
    String currConnection = stateElement.getChildText("currConnection");
    String selectedProfileLabel = stateElement.getChildText("selectedProfile");
    String currentQuestionText = stateElement.getChildText("currentQuestionText");

    List<OracleAIChatBox.AIProfileItem> profiles = stateElement.getChild("profiles").getChildren("profile").stream()
        .map(profileElement -> new OracleAIChatBox.AIProfileItem(
            profileElement.getAttributeValue("label"),
            Boolean.parseBoolean(profileElement.getAttributeValue("effective"))))
        .collect(Collectors.toList());

    List<ChatMessage> chatMessages = stateElement.getChild("chatMessages").getChildren("chatMessage").stream()
        .map(messageElement -> new ChatMessage(
            messageElement.getAttributeValue("message"),
            AuthorType.valueOf(messageElement.getAttributeValue("author"))))
        .collect(Collectors.toList());

    return OracleAIChatBoxState.builder()
        .currConnection(currConnection)
        .selectedProfile(new OracleAIChatBox.AIProfileItem(selectedProfileLabel, true))
        .currentQuestionText(currentQuestionText)
        .profiles(profiles)
        .aiAnswers(chatMessages)
        .build();
  }
}
