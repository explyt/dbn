package com.dbn.oracleAI.types;

import lombok.Getter;

/**
 * Enumerate openAI models
 */
@Getter
public enum OpenAIModelType {
  GPT4("gpt-4"),
  GPT4_0613("gpt-4-0613"),
  GPT4_32K("gpt-4-32k"),
  GPT4_32K_0613("gpt-4-32k-0613"),
  DEFAULT_GPT("gpt-3.5-turbo"),
  GPT3_5TURBO_0613("gpt-3.5-turbo-0613"),
  GPT3_5TURBO_16K("gpt-3.5-turbo-16k"),
  GPT3_5TURBO_16K_0613("gpt-3.5-turbo-16k-0613");


  private final String action;

  OpenAIModelType(String action) {
    this.action = action;
  }
}
