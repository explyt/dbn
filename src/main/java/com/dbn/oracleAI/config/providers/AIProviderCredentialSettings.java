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

package com.dbn.oracleAI.config.providers;

import com.dbn.common.options.BasicProjectConfiguration;
import com.dbn.oracleAI.config.providers.ui.AIProviderCredentialsSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.options.setting.Settings.newElement;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class AIProviderCredentialSettings
    extends BasicProjectConfiguration<AIProviderSettings, AIProviderCredentialsSettingsForm> {

  private AIProviderCredentialBundle credentials = new AIProviderCredentialBundle();

  AIProviderCredentialSettings(AIProviderSettings parent) {
    super(parent);
  }

  public void setCredentials(AIProviderCredentialBundle credentials) {
    this.credentials = new AIProviderCredentialBundle(credentials);
  }

  @NotNull
  @Override
  public AIProviderCredentialsSettingsForm createConfigurationEditor() {
    return new AIProviderCredentialsSettingsForm(this);
  }

  @Override
  public String getConfigElementName() {
    return "credential-settings";
  }

  @Override
  public void readConfiguration(Element element) {
    Element credentialsElement = element.getChild("credentials");
    if (credentialsElement != null) {
      credentials.clear();
      for (Element credentialElement : credentialsElement.getChildren()) {
        AIProviderCredential credential = new AIProviderCredential();
        credential.readConfiguration(credentialElement);
        credentials.add(credential);
      }
    }
  }

  @Override
  public void writeConfiguration(Element element) {
    Element credentialsElement = newElement(element, "credentials");
    for (AIProviderCredential credential : credentials) {
      Element credentialElement = newElement(credentialsElement, "credential");
      credential.writeConfiguration(credentialElement);
    }
  }
}
