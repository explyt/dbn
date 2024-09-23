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

package com.dbn.oracleAI.model;

import com.dbn.common.state.PersistentStateElement;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.types.ProviderModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdom.Element;

import static com.dbn.common.options.setting.Settings.*;

/**
 * Chat message context - preserving profile, model and action selection against an AI response message
 *
 * @author Dan Cioca (Oracle)
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatMessageContext implements PersistentStateElement {
    private String profile;
    private ProviderModel model;
    private ActionAIType action;

    public ChatMessageContext(String profile, ProviderModel model, ActionAIType action) {
        this.profile = profile;
        this.model = model;
        this.action = action;
    }

    @Override
    public void readState(Element element) {
        profile = stringAttribute(element, "profile");
        model = enumAttribute(element, "model", ProviderModel.class);
        action = enumAttribute(element, "action", ActionAIType.class);
    }

    @Override
    public void writeState(Element element) {
        setStringAttribute(element, "profile", profile);
        setEnumAttribute(element, "model", model);
        setEnumAttribute(element, "action", action);
    }
}
