package com.dbn.oracleAI;

import com.dbn.oracleAI.config.AttributeInput;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface ManagedObjectService<E extends AttributeInput> {
    public CompletableFuture<List<E>> list();
    public CompletableFuture<E>       get(String uuid);
    public CompletableFuture<Void>    delete(String uuid);
    public CompletionStage<Void>      create(E newItem);
    public CompletionStage<Void>      update(E updatedItem);
}
