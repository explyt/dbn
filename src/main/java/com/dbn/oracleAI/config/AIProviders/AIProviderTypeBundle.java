package com.dbn.oracleAI.config.AIProviders;

import com.dbn.common.util.Cloneable;
import com.dbn.common.util.CollectionUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class AIProviderTypeBundle implements Iterable<AIProviderType>, Cloneable {
  private final List<AIProviderType> environmentTypes = new ArrayList<>();
  public static final AIProviderTypeBundle DEFAULT = new AIProviderTypeBundle();

  private AIProviderTypeBundle() {
    List<AIProviderType> environmentTypes = List.of();
    setElements(environmentTypes);
  }

  public AIProviderTypeBundle(AIProviderTypeBundle source) {
    setElements(source.environmentTypes);
  }

  private void setElements(List<AIProviderType> environmentTypes) {
    this.environmentTypes.clear();
    CollectionUtil.cloneElements(environmentTypes, this.environmentTypes);
  }

  @NotNull
  public AIProviderType getEnvironmentType(AIProviderTypeId id) {
    for (AIProviderType environmentType : this) {
      if (environmentType.getId() == id) {
        return environmentType;
      }
    }
    return AIProviderType.DEFAULT;
  }

  @Override
  public Iterator<AIProviderType> iterator() {
    return environmentTypes.iterator();
  }

  public void clear() {
    environmentTypes.clear();
  }

  public void add(AIProviderType environmentType) {
    environmentTypes.add(environmentType);
  }

  public void add(int index, AIProviderType environmentType) {
    environmentTypes.add(index, environmentType);
  }


  public int size() {
    return environmentTypes.size();
  }

  public AIProviderType get(int index) {
    return environmentTypes.get(index);
  }

  public AIProviderType remove(int index) {
    return environmentTypes.remove(index);
  }

  public List<AIProviderType> getEnvironmentTypes() {
    return environmentTypes;
  }

  @Override
  public AIProviderTypeBundle clone() {
    return new AIProviderTypeBundle(this);
  }
}
