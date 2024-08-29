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

import com.dbn.connection.ConnectionId;
import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;

/**
 * This is the class that handles processing the prompt from the console and displaying the results to it.
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
public class ShowSqlOnEditor {
  private final Project project;
  private final DatabaseOracleAIManager manager;

  public ShowSqlOnEditor(Project project) {
    this.project = project;
    this.manager = project.getService(DatabaseOracleAIManager.class);
  }

  public void processQuery(ConnectionId connectionId, String comment, Document document, boolean withExplanation) {
    String prompt = comment.substring(3, comment.length() - 1);
    AIProfileItem currProfile = manager.getDefaultProfile(connectionId);
    String action = withExplanation ? ActionAIType.EXPLAIN_SQL.getId() : ActionAIType.SHOW_SQL.getId();
    manager.queryOracleAI(connectionId, prompt, action, currProfile.getName(), currProfile.getModel().getApiName())
        .thenAccept(answer -> appendLine(document, processText(answer, withExplanation), comment))
        .exceptionally((e) -> {
          com.dbn.common.util.Messages.showErrorDialog(project, e.getMessage());
          return null;
        });
  }


  private void appendLine(Document document, String lineToAppend, String afterComment) {
    ApplicationManager.getApplication().invokeLater(() -> {
      WriteCommandAction.runWriteCommandAction(project, () -> {
        String content = document.getText();
        int pos = content.indexOf(afterComment);

        if (pos >= 0) {
          pos = content.indexOf("\n", pos);
          if (pos >= 0) {
            document.insertString(pos + 1, lineToAppend + "\n");
          } else {
            document.insertString(content.length(), "\n" + lineToAppend + "\n");
          }
        } else {
          document.insertString(document.getTextLength(), "\n" + lineToAppend);
        }
      });
    });
  }

  public static String processText(String input, boolean withExplanation) {
    StringBuilder result = new StringBuilder();
    boolean inCodeBlock = false;
    boolean inExplanationBlock = false;
    String[] lines = input.split("\n");
    result.append("\n");

    for (String line : lines) {
      if (line.trim().startsWith("```")) {
        if (!inCodeBlock && inExplanationBlock) {
          result.append("*/\n\n");
          inExplanationBlock = false;
        }
        inCodeBlock = !inCodeBlock;
        continue;
      }

      if (inCodeBlock) {
        result.append(line).append("\n");
      } else {
        if (!inExplanationBlock && withExplanation) {
          result.append("\n/*\n");
          inExplanationBlock = true;
        }
        result.append(line).append("\n");
      }
    }

    if (inExplanationBlock) {
      result.append("*/\n\n");
    }

    return result.toString();
  }

}
