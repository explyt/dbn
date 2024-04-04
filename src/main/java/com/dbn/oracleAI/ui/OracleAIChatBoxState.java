package com.dbn.oracleAI.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Setter
@Getter
@AllArgsConstructor
public class OracleAIChatBoxState {
  private List<OracleAIChatBox.AIProfileItem> profiles;
  private OracleAIChatBox.AIProfileItem selectedProfile;
  private String currentQuestionText;
  private List<String> questionHistory;
  private String aiAnswers;
  private String currConnection;

  public OracleAIChatBoxState() {
  }


  public Element toElement() {
    Element stateElement = new Element("OracleAIChatBoxState");

    stateElement.addContent(new Element("currConnection").setText(currConnection));
    stateElement.addContent(new Element("selectedProfile").setText(selectedProfile != null ? selectedProfile.getLabel() : ""));
    stateElement.addContent(new Element("currentQuestionText").setText(currentQuestionText));
//    Element questionsElement = new Element("questionHistory");
//    for (String question : questionHistory) {
//      questionsElement.addContent(new Element("question").setText(question));
//    }
//    stateElement.addContent(questionsElement);
    stateElement.addContent(new Element("aiAnswers").setText(aiAnswers));

    Element profilesElement = new Element("profiles");
    for (OracleAIChatBox.AIProfileItem profile : profiles) {
      Element profileElement = new Element("profile");
      profileElement.setAttribute("label", profile.getLabel());
      profileElement.setAttribute("effective", String.valueOf(profile.isEffective()));
      profilesElement.addContent(profileElement);
    }
    stateElement.addContent(profilesElement);

    return stateElement;
  }

  public static OracleAIChatBoxState fromElement(Element stateElement) {
    OracleAIChatBoxState state = new OracleAIChatBoxState();

    state.setCurrConnection(stateElement.getChildText("currConnection"));

    String selectedProfileLabel = stateElement.getChildText("selectedProfile");

    state.setSelectedProfile(new OracleAIChatBox.AIProfileItem(selectedProfileLabel, true));

    state.setCurrentQuestionText(stateElement.getChildText("currentQuestionText"));

//    List<Element> questionElements = stateElement.getChild("questionHistory").getChildren("question");
//    List<String> questions = questionElements.stream().map(Element::getText).collect(Collectors.toList());
//    state.setQuestionHistory(questions);

    state.setAiAnswers(stateElement.getChildText("aiAnswers"));

    List<Element> profileElements = stateElement.getChild("profiles").getChildren("profile");
    List<OracleAIChatBox.AIProfileItem> profiles = profileElements.stream()
        .map(profileElement -> new OracleAIChatBox.AIProfileItem(
            profileElement.getAttributeValue("label"),
            Boolean.parseBoolean(profileElement.getAttributeValue("effective"))))
        .collect(Collectors.toList());
    state.setProfiles(profiles);

    return state;
  }



  // Assuming getters and setters are implemented
}
