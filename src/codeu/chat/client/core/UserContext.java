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

package codeu.chat.client.core;

import java.util.ArrayList;
import java.util.Collection;

import codeu.chat.common.ClientController;
import codeu.chat.common.BasicView;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

public final class UserContext {

  public final User user;
  private final BasicView view;
  private final ClientController controller;

  public UserContext(User user, BasicView view, ClientController controller) {
    this.user = user;
    this.view = view;
    this.controller = controller;
  }

  public boolean joinConversation(ConversationContext conversation) {
    return controller.joinConversation(conversation.conversation.id, user.id);
  }

  public ConversationContext start(String name) {
    final ConversationHeader conversation = controller.newConversation(name, user.id);
    return conversation == null ?
        null :
        new ConversationContext(user, conversation, view, controller);
  }

  public Iterable<ConversationContext> conversations() {

    // Use all the ids to get all the conversations and convert them to
    // Conversation Contexts.
    final Collection<ConversationContext> all = new ArrayList<>();
    for (final ConversationHeader conversation : view.getConversations()) {
      all.add(new ConversationContext(user, conversation, view, controller));
    }

    return all;
  }

  public boolean addConversationInterest(String title) {
    return controller.addConversationInterest(title, user.id);
  }

  public boolean addUserInterest(String name) {
    return controller.addUserInterest(name, user.id);
  }

  public boolean removeUserInterest(String name) {
    return controller.removeUserInterest(name, user.id);
  }
  public boolean removeConversationInterest(String title) {
    return controller.removeConversationInterest(title, user.id);
  }

  public Iterable<String> userStatusUpdate(String name) {
    return view.userStatusUpdate(name, user.id);
  }

  public int conversationStatusUpdate(String title) {
    return view.conversationStatusUpdate(title, user.id);
  }
}
