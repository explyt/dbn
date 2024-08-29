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
import com.dbn.common.util.UUIDs;
import com.dbn.oracleAI.types.ActionAIType;
import com.dbn.oracleAI.types.AuthorType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dbn.common.options.setting.Settings.*;

/**
 * This class is for message elements that will be in the chat
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
@Getter
@Setter
@NoArgsConstructor
public class ChatMessage implements PersistentStateElement {
  private static final Pattern SECTIONS_REGEX_PATTERN = Pattern.compile("(?s)(?:(.*?)```(\\w+)?\\n(.*?)```|(.+?)(?=\\n?```|\\z))");

  /** Unique identifier of the chat message to establish causality relations and chaining of messages */
  private String id = UUIDs.regular();
  private String content;
  private AuthorType author;
  private ChatMessageContext context;
  private transient boolean progress;

  /**
   * Creates a new ChatMessage
   * @param content the message content
   * @param author the author of the message
   * @param context the context in which the chat message was produced
   */
  public ChatMessage(String content, AuthorType author, ChatMessageContext context) {
    this.content = content;
    this.author = author;
    this.context = context;
  }

  /**
   * Breaks message contents into sections, to allow different styling of the content within same response.
   * Background: responses from the AI backends may contain a sequence of text and code sections.
   * Code is typically demarcated by ``` (3 single quotes) followed by code content and closed with again with 3 single quotes
   *
   * @return a list of {@link ChatMessageSection} with the different sections
   */
  public List<ChatMessageSection> getContentSections() {
    if (author == AuthorType.AI && context.getAction() == ActionAIType.SHOW_SQL && !content.contains("```")) {
      // output is already expected to be SQL code based on the action
      // TODO not always true (workaround -> enhance the prompt with "(please use code demarcation with language identifier in the output)")
      return new ChatMessageSection(content.trim(), "sql").asList();
    }

    if (author.isOneOf(AuthorType.USER, AuthorType.ERROR)) {
      // output is already expected to be plain text
      return new ChatMessageSection(content.trim(), null).asList();
    }

    Matcher matcher = SECTIONS_REGEX_PATTERN.matcher(content);

    List<ChatMessageSection> sections = new ArrayList<>();
    while (matcher.find()) {
      String leadingText = matcher.group(1);
      String languageId = matcher.group(2);
      String codeContent = matcher.group(3);
      String tailingText = matcher.group(4);

      createMessageSection(leadingText, null, sections);
      createMessageSection(codeContent, languageId, sections);
      createMessageSection(tailingText, null, sections);
    }

    return sections;
  }

  @Override
  public void readState(Element element) {
    id = stringAttribute(element, "id");
    author = enumAttribute(element, "author", AuthorType.class);

    Element contentElement = element.getChild("content");
    content = readCdata(contentElement);

    Element contextElement = element.getChild("context");
    context = new ChatMessageContext();
    context.readState(contextElement);
  }

  @Override
  public void writeState(Element element) {
    setStringAttribute(element, "id", id);
    setEnumAttribute(element, "author", author);

    Element contentElement = newElement(element,"content");
    writeCdata(contentElement, content);

    Element contextElement = newElement(element,"context");
    context.writeState(contextElement);
  }

  private static void createMessageSection(@Nullable String content, @Nullable String languageId, List<ChatMessageSection> container) {
    if (content == null || content.isBlank()) return;
    container.add(new ChatMessageSection(content.trim(), languageId));
  }
}

