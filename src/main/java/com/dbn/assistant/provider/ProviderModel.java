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

/**
 * AI models
 *
 * @author Emmanuel Jannetti (Oracle)
 */
public enum ProviderModel {
  GPT_4("gpt-4"),
  GPT_4_0613("gpt-4-0613"),
  GPT_4_32K("gpt-4-32k"),
  GPT_4_32K_0613("gpt-4-32k-0613"),
  GPT_3_5_TURBO("gpt-3.5-turbo"),
  GPT_3_5_TURBO_0613("gpt-3.5-turbo-0613"),
  GPT_3_5_TURBO_16K("gpt-3.5-turbo-16k"),
  GPT_3_5_TURBO_16K_0613("gpt-3.5-turbo-16k-0613"),
  COMMAND("command"),
  COMMAND_NIGHTLY("command-nightly"),
  COMMAND_LIGHT("command-light"),
  COMMAND_LIGHT_NIGHTLY("command-light-nightly"),
  COHERE_COMMAND("cohere.command"),
  COHERE_COMMAND_LIGHT("cohere.command-light"),
  META_LLAMA_2_70B_CHAT("meta.llama-2-70b-chat"),
  COHERE_EMBED_ENGLISH_V3_0("cohere.embed-english-v3.0"),
  COHERE_EMBED_MULTILINGUAL_V3_0("cohere.embed-multilingual-v3.0"),
  COHERE_EMBED_ENGLISH_LIGHT_V3_0("cohere.embed-english-light-v3.0"),
  COHERE_EMBED_MULTILINGUAL_LIGHT_V3_0("cohere.embed-multilingual-light-v3.0"),
  COHERE_EMBED_ENGLISH_LIGHT_V2_0("cohere.embed-english-light-v2.0");

  public String getApiName() {
    return apiName;
  }

  //How this is named in profile API
  private final String apiName;

  ProviderModel(String apiName) {
    this.apiName = apiName;
  }

  public static ProviderModel getByName(String modelApiName) {
    for (ProviderModel type : values()) {
      if (type.getApiName().equals(modelApiName)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid model name: " + modelApiName);
  }
}
