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

package codeu.chat.client.commandline;

import java.util.Map;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import codeu.chat.common.ServerInfo;
import codeu.chat.client.core.Context;
import codeu.chat.client.core.ConversationContext;
import codeu.chat.client.core.MessageContext;
import codeu.chat.client.core.UserContext;
import codeu.chat.util.Tokenizer;
import codeu.chat.common.Access;

public final class Chat {

  // PANELS
  //
  // We are going to use a stack of panels to track where in the application
  // we are. The command will always be routed to the panel at the top of the
  // stack. When a command wants to go to another panel, it will add a new
  // panel to the top of the stack. When a command wants to go to the previous
  // panel all it needs to do is pop the top panel.
  private final Stack<Panel> panels = new Stack<>();

  public Chat(Context context) {
    this.panels.push(createRootPanel(context));
  }

  // HANDLE COMMAND
  //
  // Take a single line of input and parse a command from it. If the system
  // is willing to take another command, the function will return true. If
  // the system wants to exit, the function will return false.
  //
  public boolean handleCommand(String line) throws IOException {

    final List<String> args = new ArrayList<>();
    final Tokenizer tokenizer = new Tokenizer(line);
    for (String token = tokenizer.next(); token != null; token = tokenizer.next()) {
      args.add(token);
    }

    // check for no input (i.e. no input given)
    if (args.size() == 0) {
      return true;
    }

    final String command = args.get(0);
    args.remove(0);

    // Because "exit" and "back" are applicable to every panel, handle
    // those commands here to avoid having to implement them for each
    // panel.

    if ("exit".equals(command)) {
      // The user does not want to process any more commands
      return false;
    }

    // Do not allow the root panel to be removed.
    if ("back".equals(command) && panels.size() > 1) {
      panels.pop();
      return true;
    }

    if (panels.peek().handleCommand(command, args)) {
      // the command was handled
      return true;
    }

    // If we get to here it means that the command was not correctly handled
    // so we should let the user know. Still return true as we want to continue
    // processing future commands.
    System.out.println("ERROR: Unsupported command");
    return true;
  }
    // Find the first conversation with the given name and return its context.
    // If no conversation has the given name, this will return null.
    private ConversationContext findConvo(final UserContext user, String title) {
    for (final ConversationContext conversation : user.conversations()) {
      if (title.equals(conversation.conversation.title)) {
        return conversation;
      }
    }
      return null;
    }
    // Find the first user with the given name and return a user context
    // for that user. If no user is found, the function will return null.
    private UserContext findUser(final Context context, String name) {
    for (final UserContext user : context.allUsers()) {
      if (user.user.name.equals(name)) {
        return user;
      }
    }
      return null;
    }

  // CREATE ROOT PANEL
  //
  // Create a panel for the root of the application. Root in this context means
  // the first panel and the only panel that should always be at the bottom of
  // the panels stack.
  //
  // The root panel is for commands that require no specific contextual information.
  // This is before a user has signed in. Most commands handled by the root panel
  // will be user selection focused.
  //
  private Panel createRootPanel(final Context context) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command to print a list of all commands and their description when
    // the user for "help" while on the root panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("ROOT MODE");
        System.out.println("  version");
        System.out.println("    Check the version of server.");
        System.out.println("  uptime");
        System.out.println("    The uptime of chat server");
        System.out.println("  u-list");
        System.out.println("    List all users.");
        System.out.println("  u-add <name>");
        System.out.println("    Add a new user with the given name.");
        System.out.println("  u-sign-in <name>");
        System.out.println("    Sign in as the user with the given name.");
        System.out.println("  exit");
        System.out.println("    Exit the program.");
      }
    });

    panel.register("version", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        final ServerInfo info = context.getInfo();
        if (info == null) {
          System.out.println("Failed to get version number.");
        } else {
          String version = info.getVersionStr();
          System.out.println(version == null ? "no version!" : version);
        }
      }
    });

    // U-LIST (user list)
    //
    // Add a command to print all users registered on the server when the user
    // enters "u-list" while on the root panel.
    //
    panel.register("u-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for (final UserContext user : context.allUsers()) {
          System.out.format(
              "USER %s (UUID:%s)\n",
              user.user.name,
              user.user.id);
        }
      }
    });

    // U-ADD (add user)
    //
    // Add a command to add and sign-in as a new user when the user enters
    // "u-add" while on the root panel.
    //
    panel.register("u-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        // this only takes first token of the name (surround with quotes for inputs with whitespaces)
        final String name = args.size() > 0 ? args.get(0).trim() : "";
        if (name.length() > 0) {
          if (context.create(name) == null) {
            System.out.println("ERROR: Failed to create new user");
          }
        } else {
          System.out.println("ERROR: Missing <username>");
        }
      }
    });

    // U-SIGN-IN (sign in user)
    //
    // Add a command to sign-in as a user when the user enters "u-sign-in"
    // while on the root panel.
    //
    panel.register("u-sign-in", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        // this only takes first token of the name (surround with quotes for inputs with whitespaces)
        final String name = args.size() > 0 ? args.get(0).trim() : "";
        if (name.length() > 0) {
          final UserContext user = findUser(context, name);
          if (user == null) {
            System.out.format("ERROR: Failed to sign in as '%s'\n", name);
          } else {
            panels.push(createUserPanel(context, user));
          }
        } else {
          System.out.println("ERROR: Missing <username>");
        }
      }
      // Now that the panel has all its commands registered, return the panel
      // so that it can be used.
    });

    // INFO (Updated context)
    //
    // Add a command to use the updated context
    // Prints the server info to the user.
    //  if info is null, the server did not send us a valid info object.
    //
    panel.register("uptime", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        final ServerInfo info = context.getInfo();
        if (info == null) {
          // Communicate error to user - the server did not send us a valid
          // info object.
          System.out.println("ERROR: The server send us an invalid info object. Failed to retrieve Up Time of server.");
        } else {
          String uptime = info.getTimeStr();
          // Print the server info (uptime) to the user in a pretty way
          System.out.println("Up-time: " + (uptime == null ? "no up time" : uptime));
        }
      }
    });

    return panel;
  }

   private Panel createUserPanel(final Context context, final UserContext user) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command that will print a list of all commands and their
    // descriptions when the user enters "help" while on the user panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("USER MODE");
        System.out.println("  c-list");
        System.out.println("    List all conversations that the current user can interact with.");
        System.out.println("  c-add <title>");
        System.out.println("    Add a new conversation with the given title and join it as the current user.");
        System.out.println("  c-join <title>");
        System.out.println("    Join the conversation as the current user.");
        System.out.println("  u-int-add <name>");
        System.out.println("    Add a user to the current user's interests.");
        System.out.println("  u-int-remove <name>");
        System.out.println("    Remove a user from the current user's interests.");
        System.out.println("  c-int-add <title>");
        System.out.println("    Add a conversation to the current user's interests.");
        System.out.println("  c-int-remove <title>");
        System.out.println("    Remove a conversation from the current user's interests.");
        System.out.println("  status-update-u <name>");
        System.out.println("    Call a status update on the specified user interest.");
        System.out.println("  status-update-c <title>");
        System.out.println("    Call a status update on the specified conversation interest.");
        System.out.println("  info");
        System.out.println("    Display all info for the current user");
        System.out.println("  back");
        System.out.println("    Go back to ROOT MODE.");
        System.out.println("  exit");
        System.out.println("    Exit the program.");
      }
    });

    // C-LIST (list conversations)
    //
    // Add a command that will print all conversations when the user enters
    // "c-list" while on the user panel.
    //
    panel.register("c-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for (final ConversationContext conversation : user.conversations()) {
          System.out.format(
              "CONVERSATION %s (UUID:%s)\n",
              conversation.conversation.title,
              conversation.conversation.id);
        }
      }
    });

    // C-ADD (add conversation)
    //
    // Add a command that will create and join a new conversation when the user
    // enters "c-add" while on the user panel.
    //
    panel.register("c-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        // this only takes first token of the conversation name (surround with quotes for inputs with whitespaces)
        final String name = args.size() > 0 ? args.get(0).trim() : "";
        if (name.length() > 0) {
          final ConversationContext conversation = user.start(name);
          if (conversation == null) {
            System.out.println("ERROR: Failed to create new conversation");
          } else {
            panels.push(createConversationPanel(conversation));
          }
        } else {
          System.out.println("ERROR: Missing <title>");
        }
      }
    });

    // C-JOIN (join conversation)
    //
    // Add a command that will joing a conversation when the user enters
    // "c-join" while on the user panel.
    //
    panel.register("c-join", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        // this only takes first token of the conversation name (surround with quotes for inputs with whitespaces)
        final String name = args.size() > 0 ? args.get(0).trim() : "";
        if (name.length() > 0) {
          final ConversationContext conversation = findConvo(user, name);
          if (conversation == null) {
            System.out.format("ERROR: No conversation with name '%s'\n", name);
          } else {
            boolean access = user.joinConversation(conversation);
            if (access) {
              System.out.format("User joined conversation '%s' successfully\n", name);
              panels.push(createConversationPanel(conversation));
            } else {
              System.out.format("ERROR: No access to join conversation '%s'\n", name);
            }
          }
        } else {
          System.out.println("ERROR: Missing <title>");
        }
      }
    });

    // U-INT-ADD (add user interest)
    //
    // Add a command that will add a user to the current user's
    // interests when the user enters "u-int-add" while on the user panel
    //
    panel.register("u-int-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for(String token : args){
          final String name = token;
          if (name.length() > 0) {
            final UserContext foundUser = findUser(context, name);
            if (foundUser == null) {
              System.out.format("ERROR: No user with name '%s'\n", name);
            } else if (user.addUserInterest(name) == true) {
                System.out.println("User \"" + name + "\" added to interests");
            } else {
                System.out.println("ERROR: User \"" + name + "\" already in interests");
            }
          } else {
            System.out.println("ERROR: Missing <username>");
          }
        }
      }
    });

    // C-INT-ADD (add conversation interest)
    //
    // A command that will add a conversation to the current user's
    // interests map when the user enters "c-int-add"" while on the user panel
    //
    panel.register("c-int-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for(String token : args){
          final String title = token;
          if (title.length() > 0) {
            final ConversationContext conversation = findConvo(user, title);
            if (conversation == null) {
              System.out.format("ERROR: No conversation with name '%s'\n", title);
            } else if (user.addConversationInterest(title) == true) {
                System.out.println("Conversation \"" + title + "\" is added to interests");
            } else {
                System.out.println("ERROR: Conversation \"" + title + "\" is already in interests");
            }
          } else {
            System.out.println("ERROR: Missing <title>");
          }
        }
      }
    });

    // U-INT-REMOVE (Remove user interest)
    //
    // Add a command that will remove a user to the current user's
    // interests when the user enters "u-int-remove" while on the user panel
    //
    panel.register("u-int-remove", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for(String token : args){
          final String name = token;
          if (name.length() > 0) {
            final UserContext foundUser = findUser(context, name);
            if (foundUser == null) {
              System.out.format("ERROR: No user with name '%s'\n", name);
            } else if (user.removeUserInterest(name) == true) {
                System.out.println("User \"" + name + "\" removed from interests");
            } else {
                System.out.println("ERROR: User \"" + name + "\" not in interests");
            }
          } else {
            System.out.println("ERROR: Missing <username>");
          }
        }
      }
    });

    // C-INT-REMOVE (remove conversation interest)
    //
    // Add a command that will remove a conversation to the current user's
    // interests when the user enters "c-int-remove" while on the user panel
    //
    panel.register("c-int-remove", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for(String token : args){
          final String title = token;
          if (title.length() > 0) {
            final ConversationContext conversation = findConvo(user, title);
            if (conversation == null) {
              System.out.format("ERROR: No conversation with name '%s'\n", title);
            } else if (user.removeConversationInterest(title) == true) {
                System.out.println("Conversation \"" + title + "\" is removed from interests");
            } else {
                System.out.println("ERROR: Conversation \"" + title + "\" is not in interests");
            }
          } else {
            System.out.println("ERROR: Missing <title>");
          }
        }
      }
    });

    // STATUS-UPDATE-C (conversation status update)
    //
    // Add a command that will check for new messages in one of the user's conversation
    // interests when the user enters "status-update-c" while on the user panel
    //
    panel.register("status-update-c", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for(String token : args){
          final String title = token;
          if (title.length() > 0) {
            final ConversationContext conversation = findConvo(user, title);
            if (conversation == null) {
              System.out.format("ERROR: No conversation with name '%s'\n", title);
            } else {
              final int newMessages = user.conversationStatusUpdate(title);
              if (newMessages == -1) {
                System.out.println("ERROR: Conversation \"" + title + "\" is not in interests");
              }
              else if (newMessages == 0) {
                System.out.println("No new messages in conversation \"" + title +"\"");
              }
              else {
                System.out.println(newMessages + " new messages in conversation \"" + title + "\"");
              }
            }
          } else {
            System.out.println("ERROR: Missing <title>");
          }
        }
      }
    });

    // STATUS-UPDATE-U (user status update)
    //
    // Add a command that will add check new conversations created by and
    //  contributed to by one of the current user's user interests when the
    //  user enters "status-update-u" while on the user panel
    //
    panel.register("status-update-u", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        for(String token : args){
          final String name = token;
          if (name.length() > 0) {
            final UserContext foundUser = findUser(context, name);
            if (foundUser == null) {
              System.out.format("ERROR: No user with name '%s'\n", name);
            } else {
              System.out.println("User \"" + name + "\" has contributed to:");
              for(final String contribution : user.userStatusUpdate(name)) {
                System.out.println(contribution);
              }
            }
          } else {
            System.out.println("ERROR: Missing <username>");
          }
        }
      }
    });


    // INFO
    //
    // Add a command that will print info about the current context when the
    // user enters "info" while on the user panel.
    //
    panel.register("info", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("User Info:");
        System.out.format("  Name : %s\n", user.user.name);
        System.out.format("  Id   : UUID:%s\n", user.user.id);
      }
    });

    // Now that the panel has all its commands registered, return the panel
    // so that it can be used.
    return panel;
  }

  private Panel createConversationPanel(final ConversationContext conversation) {

    final Panel panel = new Panel();

    // HELP
    //
    // Add a command that will print all the commands and their descriptions
    // when the user enters "help" while on the conversation panel.
    //
    panel.register("help", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("USER MODE");
        System.out.println("  m-list");
        System.out.println("    List all messages in the current conversation.");
        System.out.println("  m-add <message>");
        System.out.println("    Add a new message to the current conversation as the current user.");
        System.out.println("  info");
        System.out.println("    Display all info about the current conversation.");
        System.out.println("  get-access");
        System.out.println("    Get access mode of current user.");
        System.out.println("  change-access");
        System.out.println("    Change access of other user.");
        System.out.println("  back");
        System.out.println("    Go back to USER MODE.");
        System.out.println("  exit");
        System.out.println("    Exit the program.");
      }
    });

    // M-LIST (list messages)
    //
    // Add a command to print all messages in the current conversation when the
    // user enters "m-list" while on the conversation panel.
    //
    panel.register("m-list", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("--- start of conversation ---");
        for (MessageContext message = conversation.firstMessage();
                            message != null;
                            message = message.next()) {
          System.out.println();
          System.out.format("USER : %s\n", message.message.author);
          System.out.format("SENT : %s\n", message.message.creation);
          System.out.println();
          System.out.println(message.message.content);
          System.out.println();
        }
        System.out.println("---  end of conversation  ---");
      }
    });

    // M-ADD (add message)
    //
    // Add a command to add a new message to the current conversation when the
    // user enters "m-add" while on the conversation panel.
    //
    panel.register("m-add", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        // this only takes first token of the msg (surround with quotes for inputs with whitespaces)
        final String message = args.size() > 0 ? args.get(0).trim() : "";
        if (message.length() > 0) {
          conversation.add(message);
        } else {
          System.out.println("ERROR: Messages must contain text");
        }
      }
    });

    // INFO
    //
    // Add a command to print info about the current conversation when the user
    // enters "info" while on the conversation panel.
    //
    panel.register("info", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("Conversation Info:");
        System.out.format("  Title : %s\n", conversation.conversation.title);
        System.out.format("  Id    : UUID:%s\n", conversation.conversation.id);
        System.out.format("  Owner : %s\n", conversation.conversation.owner);
      }
    });

    panel.register("get-access", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        System.out.println("Current user has access mode: " + conversation.getAccess());
      }
    });

    panel.register("change-access", new Panel.Command() {
      @Override
      public void invoke(List<String> args) {
        if (args.size() >= 2) {
          final String userName = args.get(0).trim();
          final String access = args.get(1).trim().toLowerCase();
          // access is "member", "owner", "remove"
          final Access accessData = Access.getAccess(access);
          if (accessData != null && conversation.changeAccess(conversation.user.id, userName, accessData, conversation.conversation.id)) {
            System.out.format("Current user changed access of '%s' to '%s'\n", userName, access);
          } else {
            System.out.format("ERROR: Unable change access of '%s' to '%s'\n", userName, access);
          }

        } else {
          System.out.println("ERROR: expecting 2 arguments: mod-access <user_name> <access>");
        }
      }
    });

    // Now that the panel has all its commands registered, return the panel
    // so that it can be used.
    return panel;
  }
}
