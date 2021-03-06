package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import codeu.chat.common.User;
import codeu.chat.common.Message;
import codeu.chat.common.ConversationHeader;
import java.lang.String;

// Writeable is a class for Objects that can be written through an OutputStream
// This is used for writing different Objects to the transaction log file
public interface Writeable {
  public static final String USER_STR = "user";
  public static final String MESSAGE_STR = "message";
  public static final String CONVERSATION_STR = "conversationheader";
  public static final String CHANGE_ACCESS_REQUEST_STR = "changeaccessrequest";
  public void write(OutputStream out, Object value) throws IOException;
  public String getType();
}
