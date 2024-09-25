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

import java.util.List;

/**
 * This enum is for listing the possible credential providers we have
 * And the associated list of AI module they support
 *
 * @author Ayoub Aarrasse (Oracle)
 * @author Emmanuel Jannetti (Oracle)
 */
@Getter
public enum ProviderType {
  COHERE(ProviderApi.COHERE,
          ProviderModel.COMMAND,
          ProviderModel.COMMAND_NIGHTLY,
          ProviderModel.COMMAND_LIGHT,
          ProviderModel.COMMAND_LIGHT_NIGHTLY),
  GOOGLE(ProviderApi.GOOGLE,
          ProviderModel.GEMINI_1_5_FLASH,
          ProviderModel.GEMINI_1_5_PRO,
          ProviderModel.GEMINI_1_0_PRO),
  OPENAI(ProviderApi.OPENAI,
          ProviderModel.GPT_3_5_TURBO,
          ProviderModel.GPT_3_5_TURBO_0613,
          ProviderModel.GPT_3_5_TURBO_16K,
          ProviderModel.GPT_3_5_TURBO_16K_0613,
          ProviderModel.GPT_4,
          ProviderModel.GPT_4_0613,
          ProviderModel.GPT_4_32K,
          ProviderModel.GPT_4_32K_0613,
          ProviderModel.GPT_4_O,
          ProviderModel.GPT_4_O_MINI),
  OCI(ProviderApi.OCI,
          ProviderModel.COHERE_COMMAND,
          ProviderModel.COHERE_COMMAND_LIGHT,
          ProviderModel.META_LLAMA_2_70B_CHAT,
          ProviderModel.COHERE_EMBED_ENGLISH_LIGHT_V2_0,
          ProviderModel.COHERE_EMBED_ENGLISH_V3_0,
          ProviderModel.COHERE_EMBED_ENGLISH_LIGHT_V3_0,
          ProviderModel.COHERE_EMBED_MULTILINGUAL_V3_0,
          ProviderModel.COHERE_EMBED_MULTILINGUAL_LIGHT_V3_0);

  private final ProviderApi api;
  private final List<ProviderModel> models;

  ProviderType(ProviderApi api, ProviderModel... models) {
    this.api = api;
    this.models = List.of(models);
  }

  public ProviderModel getDefaultModel() {
    return models.get(0);
  }
}
