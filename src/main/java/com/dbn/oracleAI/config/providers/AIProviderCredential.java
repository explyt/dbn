package com.dbn.oracleAI.config.providers;

import com.dbn.common.options.PersistentConfiguration;
import com.dbn.common.ui.Presentable;
import com.dbn.common.util.Cloneable;
import com.dbn.common.util.Commons;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.options.setting.Settings.stringAttribute;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class AIProviderCredential implements Cloneable<AIProviderCredential>, PersistentConfiguration, Presentable {
  private String name;
  private String user;
  private String key;

  public AIProviderCredential(String credentialName, String user, String key) {
    this.name = credentialName;
    this.user = user;
    this.key = key;
  }

  @Override
  @NotNull
  public String getName() {
    return Commons.nvl(name, "");
  }

  @Override
  public AIProviderCredential clone() {
    return new AIProviderCredential(name, user, key);
  }

  @Override
  public String toString() {
    return name;
  }


  @Override
  public void readConfiguration(Element element) {
    name = stringAttribute(element, "name");
    user = stringAttribute(element, "user");
    key = stringAttribute(element, "key");
  }

  @Override
  public void writeConfiguration(Element element) {
    element.setAttribute("name", Commons.nvl(name, ""));
    element.setAttribute("user", Commons.nvl(user, ""));
    element.setAttribute("key", Commons.nvl(key, ""));
  }
}
