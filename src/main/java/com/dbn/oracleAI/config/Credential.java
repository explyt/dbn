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

package com.dbn.oracleAI.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * This class can store credentials for basic
 * authentication with a username and password, as well as for more complex
 * authentications like those required for OCI services.
 * This class implements the {@link AttributeInput} interface, which requires validation
 * and formatting methods suitable for use in preparing data for PL/SQL calls.
 */

@Getter
@ToString
@EqualsAndHashCode
/**
 * AI provider credential class
 */
public class Credential implements AttributeInput {
  /**
   * name of that credential
   */
  protected String credentialName;
  /**
   * username used in that credential
   */
  protected String username;
  /**
   * Is that credential enabled, a credential can be defined on DB side but been disabled
   */
  protected boolean isEnabled;
  /**
   * comment used in that credential
   */
  protected String comments;

  public Credential(String credentialName, String userName, boolean isEnabled, String comments) {
    this.credentialName = credentialName;
    this.username = userName;
    this.isEnabled = isEnabled;
    this.comments = comments;
  }

  public List<String> toUpdatingAttributeList() {
    return null;
  }

  ;

  protected String toAttributeFormat(String attributeName, String attributeValue) {
    return String.format(
        "credential_name => '%s', \n" +
            "attribute => '%s', \n" +
            "value => '%s'",
        credentialName,
        attributeName,
        attributeValue
    );
  }

  @Override
  public void validate() {

  }

  @Override
  public String toAttributeMap() {
    return null;
  }

  @Override
  public String getUuid() {
    return this.credentialName;
  }
}
