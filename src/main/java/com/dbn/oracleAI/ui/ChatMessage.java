package com.dbn.oracleAI.ui;

import com.dbn.oracleAI.types.AuthorType;
import lombok.Getter;

/**
 * This class is for message elements that will be in the chat
 */
@Getter
public class ChatMessage {
  String message;
  AuthorType author;

  /**
   * Creates a new ChatMessage
   * @param msg the message content
   * @param author the author of the message
   */
  public ChatMessage(String msg, AuthorType author) {
    this.message = msg;
    this.author = author;
  }
}
