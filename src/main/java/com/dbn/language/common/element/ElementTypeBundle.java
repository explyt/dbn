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

package com.dbn.language.common.element;

import com.dbn.common.index.IndexRegistry;
import com.dbn.common.thread.Background;
import com.dbn.common.util.Measured;
import com.dbn.common.util.Unsafe;
import com.dbn.language.common.DBLanguage;
import com.dbn.language.common.DBLanguageDialect;
import com.dbn.language.common.TokenTypeBundle;
import com.dbn.language.common.element.impl.BasicElementType;
import com.dbn.language.common.element.impl.BlockElementType;
import com.dbn.language.common.element.impl.ElementTypeBase;
import com.dbn.language.common.element.impl.ExecVariableElementType;
import com.dbn.language.common.element.impl.IdentifierElementType;
import com.dbn.language.common.element.impl.IterationElementType;
import com.dbn.language.common.element.impl.LeafElementType;
import com.dbn.language.common.element.impl.NamedElementType;
import com.dbn.language.common.element.impl.OneOfElementType;
import com.dbn.language.common.element.impl.QualifiedIdentifierElementType;
import com.dbn.language.common.element.impl.SequenceElementType;
import com.dbn.language.common.element.impl.TokenElementType;
import com.dbn.language.common.element.impl.UnknownElementType;
import com.dbn.language.common.element.impl.WrapperElementType;
import com.dbn.language.common.element.util.ElementTypeAttribute;
import com.dbn.language.common.element.util.ElementTypeDefinition;
import com.dbn.language.common.element.util.ElementTypeDefinitionException;
import com.dbn.object.type.DBObjectType;
import com.intellij.openapi.ide.CopyPasteManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dbn.common.options.setting.Settings.stringAttribute;
import static com.dbn.diagnostics.Diagnostics.conditionallyLog;

@Slf4j
@Getter
public class ElementTypeBundle {
    private final AtomicInteger leafIndexer = new AtomicInteger();
    private final IndexRegistry<LeafElementType> leafRegistry = new IndexRegistry<>();

    private final TokenTypeBundle tokenTypeBundle;
    private BasicElementType unknownElementType;
    private NamedElementType rootElementType;

    private final DBLanguageDialect languageDialect;
    private final AtomicInteger idCursor = new AtomicInteger();

    private transient Builder builder = new Builder();
    private final Map<String, NamedElementType> namedElementTypes = new ConcurrentHashMap<>();


    private static class Builder {
        private final Set<LeafElementType> leafElementTypes = new HashSet<>();
        private final Set<ElementTypeBase> allElementTypes = new HashSet<>();
        private boolean rewriteIds;
    }

    public void registerElement(LeafElementType tokenType) {
        leafRegistry.add(tokenType);
    }

    public LeafElementType getElement(int index) {
        return leafRegistry.get(index);
    }


    public ElementTypeBundle(DBLanguageDialect languageDialect, TokenTypeBundle tokenTypeBundle, Document document) {
        this.languageDialect = languageDialect;
        this.tokenTypeBundle = tokenTypeBundle;
        Measured.run("building element-type bundle for " + languageDialect.getID(), () -> build(document));
    }

    private void build(Document document) {
        try {
            Element root = document.getRootElement();
            for (Element child : root.getChildren()) {
                createNamedElementType(child);
            }

            NamedElementType unknown = namedElementTypes.get("custom_undefined");
            for(NamedElementType namedElementType : namedElementTypes.values()){
                if (!namedElementType.isDefinitionLoaded()) {
                    namedElementType.update(unknown);
                    log.warn("DBN - [{}] undefined element type: {}", this.languageDialect.getID(), namedElementType.getId());
                }
            }

            for (LeafElementType leafElementType: builder.leafElementTypes) {
                leafElementType.registerLeaf();
            }

            if (builder.rewriteIds) {
                Unsafe.warned(() -> {
/*
                    ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
                    JDOMUtil.write(document, stringWriter);

                    String data = stringWriter.toString();
*/
                    StringWriter stringWriter = new StringWriter();
                    new XMLOutputter().output(document, stringWriter);

                    String data = stringWriter.getBuffer().toString();
                    log.info("LANGUAGE_DEFINITION\n" +
                            "====================================={}\n" +
                            "=====================================", data);

                    CopyPasteManager copyPasteManager = CopyPasteManager.getInstance();
                    copyPasteManager.setContents(new StringSelection(data));
                });
            }

            Set<ElementTypeBase> allElementTypes = builder.allElementTypes;
            builder = null;
            Background.run(() -> Measured.run(
                    "initializing element-type lookup cache for " + this.languageDialect.getID(),
                    () -> {
                        for (ElementTypeBase elementType : allElementTypes) {
                            elementType.cache.initialize();
                        }
                    }));

            //warnAmbiguousBranches();
        } catch (Exception e) {
            conditionallyLog(e);
            log.error("[DBN] Failed to build element-type bundle for {}", languageDialect.getID(), e);
        }
    }

    public short nextIndex() {
        int index = leafIndexer.incrementAndGet();
        return (short) index;
    }

    public void markIdsDirty() {
        builder.rewriteIds = true;
    }

    private void createNamedElementType(Element def) throws ElementTypeDefinitionException {
        String id = determineMandatoryAttribute(def, "id", "Invalid definition of named element type.");
        String languageId = stringAttribute(def, "language");
        NamedElementType elementType = getNamedElementType(id, null);
        elementType.loadDefinition(def);
        if (elementType.is(ElementTypeAttribute.ROOT)) {
            DBLanguage language = DBLanguage.getLanguage(languageId);
            if (language == languageDialect.getBaseLanguage()) {
                if (rootElementType == null) {
                    rootElementType = elementType;
                } else {
                    throw new ElementTypeDefinitionException("Duplicate root definition");
                }
            }
        }
    }


    public static String determineMandatoryAttribute(Element def, @NonNls String attribute, String message) throws ElementTypeDefinitionException {
        String value = stringAttribute(def, attribute);
        if (value == null) {
            throw new ElementTypeDefinitionException(message + "Missing '" + attribute + "' attribute.");
        }
        return value;
    }

    public ElementTypeBase resolveElementDefinition(Element def, @NonNls String type, ElementTypeBase parent) throws ElementTypeDefinitionException {
        ElementTypeBase result;
        if (ElementTypeDefinition.SEQUENCE.is(type)){
            result = new SequenceElementType(this, parent, createId(), def);

        } else if (ElementTypeDefinition.BLOCK.is(type)) {
            result = new BlockElementType(this, parent, createId(), def);

        } else if (ElementTypeDefinition.ITERATION.is(type)) {
            result = new IterationElementType(this, parent, createId(), def);

        } else if (ElementTypeDefinition.ONE_OF.is(type)) {
            result = new OneOfElementType(this, parent, createId(), def);

        } else if (ElementTypeDefinition.QUALIFIED_IDENTIFIER.is(type)) {
            result =  new QualifiedIdentifierElementType(this, parent, createId(), def);

        } else if (ElementTypeDefinition.WRAPPER.is(type)) {
            result = new WrapperElementType(this, parent, createId(), def);

        } else if (ElementTypeDefinition.ELEMENT.is(type)) {
            String id = determineMandatoryAttribute(def, "ref-id", "Invalid reference to element.");
            result = getNamedElementType(id, parent);

        } else if (ElementTypeDefinition.TOKEN.is(type)) {
            result = new TokenElementType(this, parent, createId(), def);

        } else if (
                ElementTypeDefinition.OBJECT_DEF.is(type) ||
                ElementTypeDefinition.OBJECT_REF.is(type) ||
                ElementTypeDefinition.ALIAS_DEF.is(type) ||
                ElementTypeDefinition.ALIAS_REF.is(type) ||
                ElementTypeDefinition.VARIABLE_DEF.is(type) ||
                ElementTypeDefinition.VARIABLE_REF.is(type)) {
            result = new IdentifierElementType(this, parent, createId(), def);

        } else if (ElementTypeDefinition.EXEC_VARIABLE.is(type)) {
            result = new ExecVariableElementType(this, parent, createId(), def);

        }  else {
            throw new ElementTypeDefinitionException("Could not resolve element definition '" + type + '\'');
        }

        result.collectLeafElements(builder.leafElementTypes);
        builder.allElementTypes.add(result);
        return result;
    }


    @NotNull
    public static DBObjectType resolveObjectType(String name) throws ElementTypeDefinitionException {
        DBObjectType objectType = DBObjectType.get(name);
        if (objectType == null)
            throw new ElementTypeDefinitionException("Invalid object type '" + name + "'");
        return objectType;
    }


    /*protected synchronized TokenElementType getTokenElementType(String id) {
        TokenElementType elementType = tokenElementTypes.get(id);
        if (elementType == null) {
            elementType = new TokenElementType(this, id);
            tokenElementTypes.put(id, elementType);
            log.info("Created token element objectType '" + id + "'");
        }
        return elementType;
    }*/

    private NamedElementType getNamedElementType(String id, ElementTypeBase parent) {
        NamedElementType elementType = namedElementTypes.computeIfAbsent(id, i -> {
            NamedElementType namedElementType = new NamedElementType(this, i);
            builder.allElementTypes.add(namedElementType);
            return namedElementType;
        });

        if (parent != null) elementType.addParent(parent);
        return elementType;
    }

    public NamedElementType getNamedElementType(String id) {
        return namedElementTypes.get(id);
    }

    public BasicElementType getUnknownElementType() {
        if (unknownElementType == null) {
            unknownElementType = new UnknownElementType(this);
        }
        return unknownElementType;
    }

    private String createId() {
        String id = Integer.toString(idCursor.getAndIncrement());
        StringBuilder buffer = new StringBuilder();
        while (buffer.length() + id.length() < 5) {
            buffer.append('0');
        }
        buffer.append(id);
        return buffer.toString();
    }
}
