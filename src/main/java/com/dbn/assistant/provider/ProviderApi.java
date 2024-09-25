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

package com.dbn.assistant.provider;

import lombok.Getter;

/**
 * Provider api information
 * (containing the host / access-point of the LLM api)
 * TODO this could move to the definition of ProviderType unless it becomes more complex than a plain host name
 */
@Getter
public enum ProviderApi {
    OPENAI("api.openai.com"),
    COHERE("api.cohere.ai"),
    GOOGLE("generativelanguage.googleapis.com"),
    OCI("api.oci.com");

  private final String host;
  ProviderApi(String host) {
    this.host = host;
  }

  /**
   * Gets access point (hostname address) of a provider
   * @param providerType the provider
   * @return the access point
   */
  public static String getAccessPoint(ProviderType providerType) {
    if (providerType == null) return "";
    return providerType.getApi().getHost();
  }
}
