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
public class AIProviderCredentialBundle implements Iterable<AIProviderCredential>, Cloneable {
  private final List<AIProviderCredential> environmentTypes = new ArrayList<>();
  public static final AIProviderCredentialBundle DEFAULT = new AIProviderCredentialBundle();

  private AIProviderCredentialBundle() {
    List<AIProviderCredential> environmentTypes = List.of();
    setElements(environmentTypes);
  }

  public AIProviderCredentialBundle(AIProviderCredentialBundle source) {
    setElements(source.environmentTypes);
  }

  private void setElements(List<AIProviderCredential> environmentTypes) {
    this.environmentTypes.clear();
    CollectionUtil.cloneElements(environmentTypes, this.environmentTypes);
  }

  @NotNull
  public AIProviderCredential getEnvironmentType(AIProviderCredentialId id) {
    for (AIProviderCredential environmentType : this) {
      if (environmentType.getId() == id) {
        return environmentType;
      }
    }
    return AIProviderCredential.DEFAULT;
  }

  @Override
  public Iterator<AIProviderCredential> iterator() {
    return environmentTypes.iterator();
  }

  public void clear() {
    environmentTypes.clear();
  }

  public void add(AIProviderCredential environmentType) {
    environmentTypes.add(environmentType);
  }

  public void add(int index, AIProviderCredential environmentType) {
    environmentTypes.add(index, environmentType);
  }


  public int size() {
    return environmentTypes.size();
  }

  public AIProviderCredential get(int index) {
    return environmentTypes.get(index);
  }

  public AIProviderCredential remove(int index) {
    return environmentTypes.remove(index);
  }

  public List<AIProviderCredential> getEnvironmentTypes() {
    return environmentTypes;
  }

  @Override
  public AIProviderCredentialBundle clone() {
    return new AIProviderCredentialBundle(this);
  }
}
