package com.dbn.oracleAI.config.ui;

import com.dbn.oracleAI.config.AIProviders.AIProviderType;

public interface ProvidersSelectionCallback {
  void onProviderSelected(AIProviderType aiProviderType);
}
