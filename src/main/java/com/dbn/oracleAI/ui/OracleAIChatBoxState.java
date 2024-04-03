package com.dbn.oracleAI.ui;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jdom.Element;

import java.util.List;

@Setter @Getter @Builder @ToString
public class OracleAIChatBoxState extends Element {
  //TODO : why we are also an Element ? if we have to why we need OracleAIChatBoxState subclass ?
  private List<OracleAIChatBox.AIProfileItem> profiles;
  private OracleAIChatBox.AIProfileItem selectedProfile;
  private String currentQuestionText;
  private List<String> questionHistory;
  private String aiAnswers;
}
