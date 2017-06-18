package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import codeu.chat.common.User;
import codeu.chat.common.Message;
import codeu.chat.common.ConversationHeader;
import java.lang.String;

public class Writeable {
  public static final String USER_STR = "user";
  public static final String MESSAGE_STR = "message";
  public static final String CONVERSATION_STR = "conversationheader";
  public void write(OutputStream out, Object value) throws IOException {
    return;
  }
  public String getType() {
    return null;
  }
  // public User readUser(InputStream in) throws IOException;
  // public Message readMessage(InputStream in) throws IOException;
  // public ConversationHeader readConversationHeader(InputStream in) throws IOException;
}
