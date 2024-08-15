package com.dbn.oracleAI.config;

import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.types.ProviderType;
import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI profile POJO class
 */
@Slf4j
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(exclude = {"isEnabled", "comments"}) // Exclude as per your needs
public class Profile implements AttributeInput {

  private final String PROFILE_NAME_ATTR_NAME = "name";
  private final String PROFILE_OWNER_ATTR_NAME = "owner";

  @NotNull
  protected String profileName;

  protected String description;

  @Expose
  protected ProviderType provider;
  @SerializedName("credential_name")
  @Expose
  protected String credentialName;
  @Builder.Default
  @SerializedName("object_list")
  @Expose
  protected List<ProfileDBObjectItem> objectList = Collections.emptyList();
  protected Integer maxTokens;
  @Builder.Default
  protected List<String> stopTokens = Collections.emptyList();
  @Expose
  @JsonAdapter(ProviderModelSerializer.class)
  protected ProviderModel model;
  @Builder.Default
  @Expose
  protected Float temperature = 0.0F;
  protected boolean isEnabled;
  protected Boolean comments;


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
                    "attributes => '%s',\n" +
                    "description => '%s'\n",
            profileName,
            attributesJson,
            description);

  }

  @Override
  public String getUuid() {
    return this.profileName;
  }

  public static Object clobToObject(String attributeName, Clob clob) throws SQLException, IOException, JsonParseException {

    try (Reader reader = clob.getCharacterStream();
         BufferedReader br = new BufferedReader(reader)) {
      if ("object_list".equals(attributeName)) {
        return ProfileDBObjectItem.fromStream(br);
      } else {
        StringBuilder sb = new StringBuilder();
        int b;
        while (-1 != (b = br.read())) {
          sb.append((char) b);
        }
        return sb.toString();
      }
    }
  }


  // Inner class to handle the JSON serialization
  public static class ProviderModelSerializer implements JsonSerializer<ProviderModel> {
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
