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

import com.dbn.oracleAI.types.ProviderType;

/**
 * placeholder class for provider configuration
 */
public class ProviderConfiguration {
  private static final String OPENAI_ACCESS_POINT = "api.openai.com";
  private static final String COHERE_ACCESS_POINT = "api.cohere.ai";
  private static final String OCI_ACCESS_POINT = "api.oci.com";

  /**
   * Gets access point (hostname address) of a provider
   * @param providerType the provider
   * @return the access point
   */
  public static String getAccessPoint(ProviderType providerType) {
    String accessPoint = "";
    switch (providerType) {
      case OPENAI:
        accessPoint = OPENAI_ACCESS_POINT;
        break;
      case COHERE:
        accessPoint = COHERE_ACCESS_POINT;
        break;
      case OCI:
        accessPoint = OCI_ACCESS_POINT;
    }
    return accessPoint;
  }
}
