/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.assistant.credential.local;

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
public class LocalCredential implements Cloneable<LocalCredential>, PersistentConfiguration, Presentable {
  private String name;
  private String user;
  private String key;

  public LocalCredential(String credentialName, String user, String key) {
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
  public LocalCredential clone() {
    return new LocalCredential(name, user, key);
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
