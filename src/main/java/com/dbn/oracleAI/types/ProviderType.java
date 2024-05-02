package com.dbn.oracleAI.types;

import java.util.Collections;
import java.util.Set;

/**
 * This enum is for listing the possible credential providers we have
 */
public enum ProviderType {
  COHERE(ProviderModel.COMMAND, ProviderModel.COMMAND,
         ProviderModel.COMMAND_NIGHTLY,
         ProviderModel.COMMAND_LIGHT,
         ProviderModel.COMMAND_LIGHT_NIGHTLY),
  OPENAI(ProviderModel.GPT_3_5_TURBO, ProviderModel.GPT_3_5_TURBO,
          ProviderModel.GPT_3_5_TURBO_0613,
          ProviderModel.GPT_3_5_TURBO_16K,
          ProviderModel.GPT_3_5_TURBO_16K_0613,
          ProviderModel.GPT_4,
          ProviderModel.GPT_4_0613,
          ProviderModel.GPT_4_32K,
          ProviderModel.GPT_4_32K_0613),
  OCI(ProviderModel.COHERE_COMMAND, ProviderModel.COHERE_COMMAND,
          ProviderModel.COHERE_COMMAND_LIGHT,
          ProviderModel.META_LLAMA_2_70B_CHAT,
          ProviderModel.COHERE_EMBED_ENGLISH_LIGHT_V2_0,
          ProviderModel.COHERE_EMBED_ENGLISH_V3_0,
          ProviderModel.COHERE_EMBED_ENGLISH_LIGHT_V3_0,
          ProviderModel.COHERE_EMBED_MULTILINGUAL_V3_0,
          ProviderModel.COHERE_EMBED_MULTILINGUAL_LIGHT_V3_0);

  private Set models = Collections.emptySet();
  private ProviderModel defaultModel;
  ProviderType(ProviderModel defaultModel, ProviderModel ... models) {
   this.models = Set.of(models);
   this.defaultModel = defaultModel;
  }

 public Set<ProviderModel> getModels() {
  return models;
 }
 public ProviderModel getDefaultModel() {
  return defaultModel;
 }

}
