package com.dbn.oracleAI.config.ui;

import com.dbn.oracleAI.config.AIProviders.AIProviderCredential;

public interface ProvidersSelectionCallback {
  void onProviderSelected(AIProviderCredential aiProviderCredential);
}
