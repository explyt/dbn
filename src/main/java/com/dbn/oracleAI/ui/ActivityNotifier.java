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

package com.dbn.oracleAI.ui;

import javax.swing.*;

/**
 * A simple subclass of JProgressBar
 * to show endless progression bar
 *
 * @author Emmanuel Jannetti (emmanuel.jannetti@oracle.com)
 */
public class ActivityNotifier extends JProgressBar {

    public ActivityNotifier() {
        super(0, 0);
        stop();
    }

    /**
     * Starts to activity.
     * this will realize the progress bar
     */
    public void start() {
        setIndeterminate(true);
        setVisible(true);
    }
    /**
     * Stops to activity.
     * this will hide the progress bar
     */
    public void stop() {
        setMinimum(0);
        setMaximum(0);
        setIndeterminate(false);
        setVisible(false);
    }
}
