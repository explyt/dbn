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

package com.dbn.oracleAI.editor.intention;

import com.dbn.oracleAI.types.ActionAIType;
import com.intellij.codeInsight.intention.HighPriorityAction;
import org.jetbrains.annotations.NotNull;

/**
 * Editor intention action for invoking AI-Assistant module from within the editor
 *
 * @author Ayoub Aarrasse (ayoub.aarrasse@oracle.com)
 */
public class AssistantExplainIntentionAction extends AssistantBaseIntentionAction implements HighPriorityAction {


  @Override
  @NotNull
  public String getText() {
    return "Database assistant Explain SQL";
  }

  @Override
  protected ActionAIType getAction() {
    return ActionAIType.EXPLAIN_SQL;
  }
}
