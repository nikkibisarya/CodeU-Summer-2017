// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.server;

import java.util.Collection;
import java.lang.NullPointerException;
import java.lang.String;

import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.common.Writeable;
import codeu.chat.common.Access;
import codeu.chat.common.ChangeAccessRequest;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final Model model;
  private final Uuid.Generator uuidGenerator;

  private FileWriter fileWriter;
  private boolean loading;

  public Controller(Uuid serverId, Model model, FileWriter fileWriter) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());

    // store the FileWriter to user
    this.fileWriter = fileWriter;
    this.loading = true;
    startUp();
    this.loading = false;
  }

  public void startUp() {
    FileLoader fileLoader = new FileLoader(this);
    fileLoader.loadState();
    new Thread(fileWriter).start();
  }

  // save the state from this Writeable to transaction log file
  private void save(Writeable x) {
    if(fileWriter == null)
      return;
    try {
      if(!loading)
        fileWriter.insert(x);
    } catch (InterruptedException e) {
      System.err.println("fail to insert to queue");
    }
  }

  public boolean changeAccess(Uuid requestor, String userName, Access access, Uuid conversation) {

    User requestorUser = model.userById().first(requestor);
    User user = model.userByText().first(userName);

    if(requestorUser == null || user == null) {
      return false;
    }

    Access requestorAccess = requestorUser.getConversationAccess(conversation);

    if (access == Access.CREATOR) {
      return false;
    }

    if (requestorAccess != Access.CREATOR && requestorAccess != Access.OWNER) {
      return false;
    }

    if (requestor.equals(user.id)) {
      // trying to change self
      return false;
    }

    // can't change a CREATOR
    if (user.getConversationAccess(conversation) == Access.CREATOR) {
      return false;
    }

    if (access == Access.NO_ACCESS) {
      if (user.containsConversationAccess(conversation)) {
        save(new ChangeAccessRequest(user.id, conversation, Access.NO_ACCESS));
      }
      user.removeConversationAccess(conversation);
    } else {
      if (user.getConversationAccess(conversation) != access) {
        save(new ChangeAccessRequest(user.id, conversation, access));
      }
      user.addConversationAccess(conversation, access);
    }
    return true;
  }

  public void loadChangeAccess(Uuid userid, Uuid conversationid, Access access) {
    User user = model.userById().first(userid);
    ConversationHeader conversation = model.conversationById().first(conversationid);

    if (access == Access.NO_ACCESS) {
      user.removeConversationAccess(conversationid);
    } else {
      user.addConversationAccess(conversationid, access);
    }
    LOG.info("loadChangeAccess success: (user=%s, conversation=%s, access=%s)", user.name, conversation.title, access);
  }

  public Access getAccess(Uuid conversation, Uuid user) {
    Access access = model.userById().first(user).getConversationAccess(conversation);
    return access;
  }

  public boolean joinConversation(Uuid conversation, Uuid user) {
    User getUser = model.userById().first(user);
    return getUser.containsConversationAccess(conversation);
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name) {
    return newUser(createId(), name, Time.now());
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner) {
    return newConversation(createId(), title, owner, Time.now());
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final ConversationPayload foundConversation = model.conversationPayloadById().first(conversation);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body, conversation);
      model.add(message);

      // save this current Message object to log file
      save(message);

      LOG.info("Message added: %s", message.id);

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {
        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
          Uuid.equals(foundConversation.firstMessage, Uuid.NULL) ?
          message.id :
          foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.

      foundConversation.lastMessage = message.id;
    }

    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime) {

    User user = null;

    if (isUsernameInUse(name)) {
      LOG.info(
          "newUser fail - name in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    } else if (isIdFree(id)) {
      user = new User(id, name, creationTime);
      model.add(user);

      // save this current User object to log file
      save(user);

      LOG.info(
          "newUser success (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    } else {
      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }
    return user;
  }

  @Override
  public ConversationHeader newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    final User foundOwner = model.userById().first(owner);

    ConversationHeader conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new ConversationHeader(id, owner, creationTime, title);
      model.add(conversation);

      // add creator access to owner
      foundOwner.addConversationAccess(id, Access.CREATOR);

      // save this current Conversationheader object to log file
      save(conversation);

      LOG.info("Conversation added: " + id);
    }

    return conversation;
  }

  @Override
  public boolean addUserInterest(String name, Uuid owner) {

    final User foundOwner = model.userById().first(owner);
    final User foundUser = model.userByText().first(name);

    if(foundOwner.UserUpdateMap.containsKey(foundUser.id)) {
      LOG.info("ERROR: User already in interests.");
      return false;
    } else {
      foundOwner.UserUpdateMap.put(foundUser.id, Time.now());
      LOG.info("User Interest added: " + foundUser.id);
      return true;
    }
  }

  @Override
  public boolean removeUserInterest(String name, Uuid owner) {

    final User foundOwner = model.userById().first(owner);
    final User foundUser = model.userByText().first(name);

    if(foundOwner.UserUpdateMap.containsKey(foundUser.id)) {
      //foundOwner.UserSet.remove(foundUser.id);
      foundOwner.UserUpdateMap.remove(foundUser.id);
      LOG.info("User Interest removed: " + foundUser.id);
      return true;
    } else {
      LOG.info("ERROR: User not found in interests.");
      return false;
    }
  }

  @Override
  public boolean addConversationInterest(String title, Uuid owner) {

    final User foundOwner = model.userById().first(owner);
    final ConversationHeader foundConversation = model.conversationByText().first(title);

    if(foundOwner.ConvoUpdateMap.containsKey(foundConversation.id)) {
      LOG.info("ERROR: Conversation already in interests.");
      return false;
    } else {
      //foundOwner.ConvoSet.add(foundConversation.id);
      foundOwner.ConvoUpdateMap.put(foundConversation.id, Time.now());
      LOG.info("Conversation Interest added: " + foundConversation.id);
      return true;
    }
  }

  @Override
  public boolean removeConversationInterest(String title, Uuid owner) {

    final User foundOwner = model.userById().first(owner);
    final ConversationHeader foundConversation = model.conversationByText().first(title);

    if(foundOwner.ConvoUpdateMap.containsKey(foundConversation.id)) {
      //foundOwner.ConvoSet.remove(foundConversation.id);
      foundOwner.ConvoUpdateMap.remove(foundConversation.id);
      LOG.info("Conversation Interest removed: " + foundConversation.id);
      return true;
    } else {
      LOG.info("ERROR: Conversation not found in interests.");
      return false;
    }
  }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

     // Assuming that "randomUuid" is actually well implemented, this
     // loop should never be needed, but just incase make sure that the
     // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null ||
           model.conversationById().first(id) != null ||
           model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) { return !isIdInUse(id); }

  private boolean isUsernameInUse(String name) {
    return model.userByText().first(name) != null;
  }

}
