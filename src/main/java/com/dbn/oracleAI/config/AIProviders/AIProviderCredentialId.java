package com.dbn.oracleAI.config.AIProviders;

import com.dbn.common.constant.PseudoConstant;

import java.util.UUID;

public final class AIProviderCredentialId extends PseudoConstant<AIProviderCredentialId> {

  public static final AIProviderCredentialId DEFAULT = get("default");

  public AIProviderCredentialId(String id) {
    super(id);
  }

  public static AIProviderCredentialId get(String id) {
    return PseudoConstant.get(AIProviderCredentialId.class, id);
  }

  public static AIProviderCredentialId create() {
    return AIProviderCredentialId.get(UUID.randomUUID().toString());
  }

}
