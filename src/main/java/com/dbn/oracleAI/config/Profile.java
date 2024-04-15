package com.dbn.oracleAI.config;

import com.dbn.oracleAI.types.ProviderType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
public class Profile implements AttributeInput {

  private static final Gson gson = new GsonBuilder()
      .excludeFieldsWithoutExposeAnnotation()
      .create();

  private String profileName;
  private String description;

  @NonNull @Expose private ProviderType provider;
  @NonNull @Expose private String credentialName;
  @Builder.Default
  @Expose private List<ObjectListItem> objectList = Collections.emptyList();
  private Integer maxTokens;
  @Builder.Default
  private List<String> stopTokens = Collections.emptyList();
  @NonNull @Expose private String model;
  private Double temperature;
  private Boolean comments;

  @Setter
  @Getter
  public static class ObjectListItem {
    private String owner;
    private String name;

    public ObjectListItem(String owner, String name) {
      this.owner = owner;
      this.name = name;
    }
  }

  @Override
  public boolean isValid() {
    return provider != null && credentialName != null;
  }

  @Override
  public String format() throws IllegalArgumentException {
    if (!isValid()) {
      throw new IllegalArgumentException("Invalid profile attributes.");
    }

    String attributesJson = gson.toJson(this).replace("'", "''");

    return String.format(
        "profile_name => '%s',\n" +
            "attributes => '%s',\n" +
            "description => '%s'",
        profileName,
        attributesJson,
        description
    );
  }

  public static Object clobToObject(String attributeName, Clob clob) throws SQLException, IOException {
    if ("object_list".equals(attributeName)) {
      Gson gson = new Gson();
      try (Reader reader = clob.getCharacterStream();
           BufferedReader br = new BufferedReader(reader)) {
        Type listType = new TypeToken<List<ObjectListItem>>(){}.getType();

        return gson.fromJson(br, listType);
      }
    } else {
      StringBuilder sb = new StringBuilder();
      try (Reader reader = clob.getCharacterStream();
           BufferedReader br = new BufferedReader(reader)) {
        int b;
        while (-1 != (b = br.read())) {
          sb.append((char)b);
        }
      }
      return sb.toString();
    }
  }
}
