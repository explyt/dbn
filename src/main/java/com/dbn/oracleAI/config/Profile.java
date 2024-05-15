package com.dbn.oracleAI.config;

import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.types.ProviderType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.diagnostic.Logger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
@EqualsAndHashCode(exclude = {"isEnabled", "comments"}) // Exclude as per your needs
public class Profile implements AttributeInput {

  private static final Logger LOGGER = Logger.getInstance("com.dbn.oracleAI");

  private final String PROFILE_NAME_ATTR_NAME = "name";
  private final String PROFILE_OWNER_ATTR_NAME = "owner";

  private String profileName;

  private String description;

  @Expose
  private ProviderType provider;
  @SerializedName("credential_name")
  @Expose
  private String credentialName;
  @Builder.Default
  @SerializedName("object_list")
  @Expose
  private List<ProfileDBObjectItem> objectList = Collections.emptyList();
  private Integer maxTokens;
  @Builder.Default
  private List<String> stopTokens = Collections.emptyList();
  @Expose
  @JsonAdapter(ProviderModelSerializer.class)
  private ProviderModel model;
  @Builder.Default
  @Expose
  private Double temperature = 0.0;
  private boolean isEnabled;
  private Boolean comments;


  /**
   * Deep Copy Constructor for Profile.
   * This constructor will create a deep copy of the given Profile instance.
   *
   * @param other The Profile instance to copy from.
   */
  public Profile(Profile other) {
    this.profileName = other.profileName;
    this.description = other.description;
    this.provider = other.provider;
    this.credentialName = other.credentialName;
    if (other.objectList != null) {
      this.objectList = new ArrayList<>(other.objectList);
    }
    this.maxTokens = other.maxTokens;
    if (other.stopTokens != null) {
      this.stopTokens = new ArrayList<>(other.stopTokens);
    }
    this.model = other.model;
    this.temperature = other.temperature;
    this.isEnabled = other.isEnabled;
    this.comments = other.comments;
  }

  @Override
  public void validate() {
    // TODO implement this
  }

  @Override
  public String toAttributeMap(boolean isNew) throws IllegalArgumentException {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(ProviderModel.class, new ProviderModelSerializer())
        .create();

    String attributesJson = gson.toJson(this).replace("'", "''");
    if (isNew) {
      return String.format(
          "profile_name => '%s',\n" +
              "attributes => '%s',\n" +
              "description => '%s'\n",
          profileName,
          attributesJson,
          description);
    } else {
      return String.format(
          "profile_name => '%s',\n" +
              "attributes => '%s'\n",
          profileName,
          attributesJson);
    }
  }

  public static Object clobToObject(String attributeName, Clob clob) throws SQLException, IOException {
    if ("object_list".equals(attributeName)) {
      GsonBuilder builder = new GsonBuilder();
      Gson gson = builder.create();

      try (Reader reader = clob.getCharacterStream();
           BufferedReader br = new BufferedReader(reader)) {
        Type listType = new TypeToken<List<ProfileDBObjectItem>>() {
        }.getType();
        return gson.fromJson(br, listType);
      }
    } else {
      StringBuilder sb = new StringBuilder();
      try (Reader reader = clob.getCharacterStream();
           BufferedReader br = new BufferedReader(reader)) {
        int b;
        while (-1 != (b = br.read())) {
          sb.append((char) b);
        }
      }
      return sb.toString();
    }
  }

  // Inner class to handle the JSON serialization
  private static class ProviderModelSerializer implements JsonSerializer<ProviderModel> {
    @Override
    public JsonElement serialize(ProviderModel src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getApiName());
    }
  }

  /**
  * When setting the provider we set the default model if it's still null.
  * This is because there could be a chance that the model is not specified in the database server side
  **/
  public void setProvider(ProviderType type) {
    this.provider = type;
    if (this.model == null) this.model = provider.getDefaultModel();
  }
}
