package com.dbn.oracleAI.config.ui;

import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.types.ProviderModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class is made for when we want to update a profile and implements a different version of toAttributeMap
 */
public class ProfileUpdate extends Profile {

  public ProfileUpdate(Profile other) {
    super(other);
  }

  @Override
  public String toAttributeMap() throws IllegalArgumentException {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(ProviderModel.class, new ProviderModelSerializer())
        .create();

    String attributesJson = gson.toJson(this).replace("'", "''");
    return String.format(
        "profile_name => '%s',\n" +
            "attributes => '%s'\n"
        ,
        profileName,
        attributesJson);

  }
}
