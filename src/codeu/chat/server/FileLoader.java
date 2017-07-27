package codeu.chat.server;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Thread;

import codeu.chat.common.Writeable;
import codeu.chat.common.Message;
import codeu.chat.common.ConversationHeader;
import codeu.chat.server.FileWriter;
import codeu.chat.util.Serializers;
import codeu.chat.common.User;
import codeu.chat.common.Access;
import codeu.chat.common.ChangeAccessRequest;

public class FileLoader {

  private Controller controller;

  public FileLoader(Controller controller) {
    this.controller = controller;
  }

  public void loadState() {
    File file = new File(FileWriter.TRANSACTION_FILE);

    // check if can't load transaction state from the log file
    if(!file.exists())
      return;
    FileInputStream fin = null;
    try {
      fin = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      System.err.println("couldn't find transaction log file");
    } catch (SecurityException e) {
      System.err.println("can't access read transaction log file");
    }

    // each count has a type first then data right after
    String type;
    Object value;
    try {

      // each object is in the format: 0x00 then type then data
      while(fin.read() != -1) {

          // read the type of the data
          type = Serializers.STRING.read(fin);

          // check cases for user message or conversationheader types
          switch(type) {
            case Writeable.USER_STR:
              value = User.SERIALIZER.read(fin);
              User user = (User)value;

              // add new user to restore state
              this.controller.newUser(user.id, user.name, user.creation);
              break;
            case Writeable.MESSAGE_STR:
              value = Message.SERIALIZER.read(fin);
              Message message = (Message)value;

              // add new message to restore state
              this.controller.newMessage(message.id, message.author, message.conversationName, message.content, message.creation);
              break;
            case Writeable.CONVERSATION_STR:
              value = ConversationHeader.SERIALIZER.read(fin);
              ConversationHeader conversationheader = (ConversationHeader)value;

              // add new conversation to restore state
              this.controller.newConversation(conversationheader.id, conversationheader.title, conversationheader.owner, conversationheader.creation);
              break;
            case Writeable.CHANGE_ACCESS_REQUEST_STR:
              value = ChangeAccessRequest.SERIALIZER.read(fin);
              ChangeAccessRequest request = (ChangeAccessRequest)value;

              // load access for the user
              this.controller.loadChangeAccess(request.user, request.conversation, request.access);
              break;
            }
      }
    } catch (IOException e) {
        System.err.println("error reading transaction log");
      }

    try {
      fin.close();
    } catch (IOException e) {
    }
  }

}
