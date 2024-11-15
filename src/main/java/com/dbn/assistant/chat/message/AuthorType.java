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

import com.dbn.common.constant.Constant;

/**
 * This is for the possible authors that can send a message in the chat
 *
 * @author Ayoub Aarrasse (Oracle)
 */
public enum AuthorType implements Constant<AuthorType> {
  USER,   // user prompt
  AGENT,  // assistant backend responses
  SYSTEM; // error responses or information blocks
}