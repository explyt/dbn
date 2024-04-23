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
public class AIProviderType implements Cloneable<AIProviderType>, PersistentConfiguration, Presentable {

  public static final AIProviderType DEFAULT = new AIProviderType(AIProviderTypeId.DEFAULT, "", "", "");

  private AIProviderTypeId id;
  private String hostname;
  private String username;
  private String key;

  public AIProviderType() {
    this(AIProviderTypeId.create());
  }

  public AIProviderType(AIProviderTypeId id) {
    this.id = id;
  }

  public AIProviderType(AIProviderTypeId id, String hostname, String username, String key) {
    this.id = id;
    this.hostname = hostname;
    this.username = username;
    this.key = key;
  }

  @Override
  @NotNull
  public String getName() {
    return Commons.nvl(hostname, "");
  }

  @Nullable
  @Override
  public Icon getIcon() {
    return null;
  }


  @Override
  public AIProviderType clone() {
    return new AIProviderType(id, hostname, username, key);
  }

  @Override
  public String toString() {
    return hostname;
  }


  @Override
  public void readConfiguration(Element element) {
    id = AIProviderTypeId.get(stringAttribute(element, "id"));
    hostname = stringAttribute(element, "hostname");
    username = stringAttribute(element, "username");
    key = stringAttribute(element, "key");

    if (id == null) id = AIProviderTypeId.get(cachedLowerCase(hostname));

  }

  @Override
  public void writeConfiguration(Element element) {
    element.setAttribute("id", id.id());
    element.setAttribute("hostname", Commons.nvl(hostname, ""));
    element.setAttribute("username", Commons.nvl(username, ""));
    element.setAttribute("key", Commons.nvl(key, ""));
  }
}
