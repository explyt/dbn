/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.dbn.oracleAI;

import com.dbn.oracleAI.config.AttributeInput;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Proxy on remote service.
 * The purpose of this proxy si to cache information.
 * As for now there is no auto-invalidation (time based etc...)
 * This proxy fetch remotly a soon as a destruptive operation has been
 * performed
 *
 * @author Emmanuel Jannetti (emmanuel.jannetti@oracle.com)
 * @param <E>
 */
public class ManagedObjectServiceProxy<E extends AttributeInput> implements ManagedObjectService<E> {
  ReentrantLock lock = new ReentrantLock();
  private ManagedObjectService<E> backend;
  private List<E> items = null;

  private final PropertyChangeSupport support;

  public ManagedObjectServiceProxy(ManagedObjectService<E> backend) {
    this.backend = backend;
    this.support = new PropertyChangeSupport(this);
  }

  /**
   * make all items invalid
   */
  public void invalidate() {

  }

  @Override
  public CompletableFuture<List<E>> list() {
    try {
      lock.lock();
      if (this.items == null) {
        return this.backend.list().thenCompose(list -> {
          this.items = list;
          return CompletableFuture.completedFuture(list);
        });
      } else {
        return CompletableFuture.completedFuture(this.items);
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public CompletableFuture<E> get(String uuid) {
    Optional<E> item = this.items.stream().filter(e -> e.getUuid().equalsIgnoreCase(uuid)).findFirst();
    if (item.isPresent()) {
      return CompletableFuture.completedFuture(item.get());
    } else {
      return CompletableFuture.completedFuture(null);
    }
  }

  @Override
  public CompletableFuture<Void> delete(String uuid) {
    return backend.delete(uuid).thenRunAsync(() -> {
      this.items.removeIf(e -> e.getUuid().equalsIgnoreCase(uuid));
      fireUpdatedProfileListEvent();
    });
  }

  @Override
  public CompletionStage<Void> create(E newItem) {
    return backend.create(newItem).thenRunAsync(() -> {
      this.items.add(newItem);
      // TODO : deal with errors
      fireUpdatedProfileListEvent();
    });
  }

  @Override
  public CompletionStage<Void> update(E updatedItem) {
    return backend.update(updatedItem).thenRunAsync(() -> {
      // TODO : dela with the list
      fireUpdatedProfileListEvent();
    });

  }

  private void fireUpdatedProfileListEvent() {
    support.firePropertyChange(this.backend.getClass().getCanonicalName(), null, null);
  }

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    support.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    support.removePropertyChangeListener(pcl);
  }
}
