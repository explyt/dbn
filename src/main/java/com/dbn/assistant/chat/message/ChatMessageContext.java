/*
 * Copyright 2024 Oracle and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dbn.assistant.chat.message;

import com.dbn.assistant.chat.window.PromptAction;
import com.dbn.assistant.provider.AIModel;
import com.dbn.common.state.PersistentStateElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdom.Element;

import java.util.Map;

import static com.dbn.common.options.setting.Settings.enumAttribute;
import static com.dbn.common.options.setting.Settings.setEnumAttribute;
import static com.dbn.common.options.setting.Settings.setStringAttribute;
import static com.dbn.common.options.setting.Settings.stringAttribute;

/**
 * Chat message context - preserving profile, model and action selection against an AI response message
 *
 * @author Dan Cioca (Oracle)
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatMessageContext implements PersistentStateElement {
    private static final Gson GSON = new GsonBuilder().create();

    private String profile;
    private AIModel model;
    private PromptAction action;

    public ChatMessageContext(String profile, AIModel model, PromptAction action) {
        this.profile = profile;
        this.model = model;
        this.action = action;
    }

    public String getAttributes() {
        Map<String, String> attributes = Map.of("model", model.getApiName());
        return GSON.toJson(attributes);
    }

    @Override
    public void readState(Element element) {
        profile = stringAttribute(element, "profile");
          model = AIModel.forId(stringAttribute(element, "model"));
        action = enumAttribute(element, "action", PromptAction.class);
    }

    @Override
    public void writeState(Element element) {
        setStringAttribute(element, "profile", profile);
        setStringAttribute(element, "model", model.getId());
        setEnumAttribute(element, "action", action);
    }
}
