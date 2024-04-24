package com.dbn.oracleAI.config.AIProviders;

import com.dbn.common.constant.PseudoConstant;

import java.util.UUID;

public final class AIProviderTypeId extends PseudoConstant<AIProviderTypeId> {

  public static final AIProviderTypeId DEFAULT = get("default");

  public AIProviderTypeId(String id) {
    super(id);
  }

  public static AIProviderTypeId get(String id) {
    return PseudoConstant.get(AIProviderTypeId.class, id);
  }

  public static AIProviderTypeId create() {
    return AIProviderTypeId.get(UUID.randomUUID().toString());
  }

}
