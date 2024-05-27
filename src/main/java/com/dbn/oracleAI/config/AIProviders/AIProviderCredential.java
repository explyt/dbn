package com.dbn.oracleAI.config.AIProviders;

import com.dbn.common.options.PersistentConfiguration;
import com.dbn.common.ui.Presentable;
import com.dbn.common.util.Cloneable;
import com.dbn.common.util.Commons;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

import static com.dbn.common.options.setting.Settings.stringAttribute;
import static com.dbn.common.util.Strings.cachedLowerCase;

@Getter
@Setter
@EqualsAndHashCode
public class AIProviderCredential implements Cloneable<AIProviderCredential>, PersistentConfiguration, Presentable {

  public static final AIProviderCredential DEFAULT = new AIProviderCredential(AIProviderCredentialId.DEFAULT, "", "", "");

  private AIProviderCredentialId id;
  private String credentialName;
  private String username;
  private String key;

  public AIProviderCredential() {
    this(AIProviderCredentialId.create());
  }

  public AIProviderCredential(AIProviderCredentialId id) {
    this.id = id;
  }

  public AIProviderCredential(AIProviderCredentialId id, String credentialName, String username, String key) {
    this.id = id;
    this.credentialName = credentialName;
    this.username = username;
    this.key = key;
  }

  @Override
  @NotNull
  public String getName() {
    return Commons.nvl(credentialName, "");
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }


  @Override
  public AIProviderCredential clone() {
    return new AIProviderCredential(id, credentialName, username, key);
  }

  @Override
  public String toString() {
    return credentialName;
  }


  @Override
  public void readConfiguration(Element element) {
    id = AIProviderCredentialId.get(stringAttribute(element, "id"));
    credentialName = stringAttribute(element, "credential_name");
    username = stringAttribute(element, "username");
    key = stringAttribute(element, "key");

    if (id == null) id = AIProviderCredentialId.get(cachedLowerCase(credentialName));

  }

  @Override
  public void writeConfiguration(Element element) {
    element.setAttribute("id", id.id());
    element.setAttribute("credential_name", Commons.nvl(credentialName, ""));
    element.setAttribute("username", Commons.nvl(username, ""));
    element.setAttribute("key", Commons.nvl(key, ""));
  }
}
