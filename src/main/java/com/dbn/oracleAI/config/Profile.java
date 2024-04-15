package com.dbn.oracleAI.config;

import com.dbn.oracleAI.types.ProviderType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Clob;
import java.sql.SQLException;
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

  @Expose private ProviderType provider;
  @Expose private String credentialName;
  @Expose private List<ObjectListItem> objectList;
  private Integer maxTokens;
  private List<String> stopTokens;
  @Expose private String model;
  private Double temperature;
  private Boolean comments;



  @Override
  public void validate() {
    //TODO
  }

  @Override
  public String toAttributeMap() throws IllegalArgumentException {
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
