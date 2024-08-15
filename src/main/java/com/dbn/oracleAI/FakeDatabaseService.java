package com.dbn.oracleAI;

import com.dbn.oracleAI.config.DBObjectItem;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Mockup database service.
 * This service use data from JSON dump files.
 * Default location are
 *     /var/tmp/schemas.json
 *     /var/tmp/dbitems.json
 * Location can be overided by following system properties
 *  fake.services.schema.dump
 *  fake.services.dbitems.dump
 */
@Slf4j
public class FakeDatabaseService implements DatabaseService {

    Type SCHEMA_TYPE = new TypeToken<List<String>>() {
    }.getType();
    Type DBOJB_TYPE = new TypeToken<List<DBObjectItem>>() {
    }.getType();
    String schemasRepoFilename = System.getProperty("fake.services.schema.dump", "/var/tmp/schemas.json");
    String dbobjRepoFilename = System.getProperty("fake.services.dbitems.dump", "/var/tmp/dbitems.json");

    List<String> schemas = null;
    Map<String, List<DBObjectItem>> objs = null;

    @Override
    public CompletableFuture<List<String>> getSchemaNames() {
        if (schemas == null) {
            try {
                this.schemas = new Gson().fromJson(new FileReader(schemasRepoFilename), SCHEMA_TYPE);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("cannot read schemas list " + e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(this.schemas);
    }

    @Override
    public CompletableFuture<List<DBObjectItem>> getObjectItemsForSchema(String schema) {
        if (objs == null) {
            this.objs = new HashMap<>();
            Gson g = new GsonBuilder().setLenient().create();
            // expected file format
            // <schema name>:<json array><CR>
            try {

                BufferedReader fr = new BufferedReader(new FileReader(dbobjRepoFilename));
                StringBuffer schemaName = new StringBuffer();
                boolean eofReached = false;
                boolean markerReached = false;
                // read schema
                while (!eofReached) {
                    int c = 0;
                    markerReached = false;
                    c = fr.read();
                    switch (c) {
                        case (int)':':
                            markerReached=true;
                            break;
                        case -1:
                            eofReached=true;
                            break;
                        default:
                            schemaName.append((char) c);
                    }
                    if (eofReached) {
                        break;
                    }
                    if (markerReached) {
                        log.debug("new schema :" + schemaName);
                        String jl = fr.readLine();
                        List<DBObjectItem> l = g.fromJson(jl, DBOJB_TYPE);
                        log.debug("new obj :" + l);
                        this.objs.put(schemaName.toString(), l);
                        schemaName.delete(0, schemaName.length());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("cannot read db obj list " + e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(this.objs.get(schema));
    }
}
