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

package com.dbn.oracleAI.config.AIProviders;

import com.dbn.common.util.Cloneable;
import com.dbn.common.util.CollectionUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
public class AIProviderCredentialBundle implements Iterable<AIProviderCredential>, Cloneable {
  private final List<AIProviderCredential> credentials = new ArrayList<>();
  public static final AIProviderCredentialBundle DEFAULT = new AIProviderCredentialBundle();

  private AIProviderCredentialBundle() {
    List<AIProviderCredential> environmentTypes = List.of();
    setElements(environmentTypes);
  }

  public AIProviderCredentialBundle(AIProviderCredentialBundle source) {
    setElements(source.credentials);
  }

  private void setElements(List<AIProviderCredential> environmentTypes) {
    this.credentials.clear();
    CollectionUtil.cloneElements(environmentTypes, this.credentials);
  }

  @NotNull
  public AIProviderCredential getEnvironmentType(AIProviderCredentialId id) {
    for (AIProviderCredential environmentType : this) {
      if (environmentType.getId() == id) {
        return environmentType;
      }
    }
    return AIProviderCredential.DEFAULT;
  }

  @Override
  public Iterator<AIProviderCredential> iterator() {
    return credentials.iterator();
  }

  public void clear() {
    credentials.clear();
  }

  public void add(AIProviderCredential environmentType) {
    credentials.add(environmentType);
  }

  public void add(int index, AIProviderCredential environmentType) {
    credentials.add(index, environmentType);
  }


  public int size() {
    return credentials.size();
  }

  public AIProviderCredential get(int index) {
    return credentials.get(index);
  }

  public AIProviderCredential remove(int index) {
    return credentials.remove(index);
  }

  @Override
  public AIProviderCredentialBundle clone() {
    return new AIProviderCredentialBundle(this);
  }
}
