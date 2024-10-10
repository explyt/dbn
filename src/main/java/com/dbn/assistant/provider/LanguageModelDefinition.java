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

package com.dbn.assistant.provider;

import com.dbn.common.util.XmlContents;
import lombok.SneakyThrows;
import org.jdom.Element;

import java.util.List;

import static com.dbn.common.options.setting.Settings.booleanAttribute;
import static com.dbn.common.options.setting.Settings.stringAttribute;
import static com.dbn.common.util.Lists.convert;
import static java.util.Collections.unmodifiableList;

public class LanguageModelDefinition {
    private static final LanguageModelDefinition INSTANCE = new LanguageModelDefinition();
    private final List<ProviderType> providers;

    @SneakyThrows
    private LanguageModelDefinition() {
        Element element = XmlContents.fileToElement(getClass(), "language-model-providers.xml");
        List<Element> providerElements = element.getChildren("provider");

        providers = unmodifiableList(convert(providerElements, e -> createProvider(e)));
    }

    private static ProviderModel createModel(ProviderType provider, Element element) {
        String modelId = stringAttribute(element, "id");
        String modelApiName = stringAttribute(element, "api-name");
        return new ProviderModel(provider, modelId, modelApiName);
    }

    private static ProviderType createProvider(Element element) {
        String id = stringAttribute(element, "id");
        String name = stringAttribute(element, "name");
        String host = stringAttribute(element, "host");
        boolean main = booleanAttribute(element, "default", false);
        boolean experimental = booleanAttribute(element, "experimental", false);
        ProviderType provider = new ProviderType(id, name, host, main, experimental);

        List<Element> modelElements = element.getChild("models").getChildren();
        List<ProviderModel> models = unmodifiableList(convert(modelElements, e -> createModel(provider, e)));
        provider.setModels(models);

        return provider;
    }


    public static List<ProviderType> providers() {
        return INSTANCE.providers;
    }
}
