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
