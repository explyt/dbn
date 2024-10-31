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

package com.dbn.object.common.ui;

import com.dbn.common.ui.util.Listeners;
import com.dbn.object.common.DBObject;
import com.dbn.object.lookup.DBObjectRef;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.List;

public class DBObjectListModel<T extends DBObject> implements ListModel<DBObjectRef<T>> {
    private final List<DBObjectRef<T>> data = new ArrayList<>();
    private final Listeners<ListDataListener> listeners = Listeners.create();

    public DBObjectListModel() {
    }

    public DBObjectListModel(List<T> data) {
        setData(data);
    }

    public static <T extends DBObject> DBObjectListModel<T> create(List<T> data) {
        return new DBObjectListModel<>(data);
    }

    public void setData(List<T> objects) {
        data.clear();
        data.addAll(DBObjectRef.from(objects));
        notifyListeners();
    }

    private void notifyListeners() {
        ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, data.size());
        listeners.notify(l -> l.contentsChanged(event));
    }

    @Override
    public int getSize() {
        return data.size();
    }

    @Override
    public DBObjectRef<T> getElementAt(int i) {
        return data.get(i);
    }

    public List<DBObjectRef<T>> getElements() {
        return data;
    }

    @Override
    public void addListDataListener(ListDataListener listDataListener) {
        listeners.add(listDataListener);
    }

    @Override
    public void removeListDataListener(ListDataListener listDataListener) {
        listeners.remove(listDataListener);
    }
}
