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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread;

import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.common.ClientController;
import codeu.chat.common.PasswordHasher;
import codeu.chat.common.Access;

final class Controller implements ClientController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final ConnectionSource source;

  public Controller(ConnectionSource source) {
    this.source = source;
  }

  public boolean changeAccess(Uuid requestor, String userName, Access access, Uuid conversation) {
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.CHANGE_ACCESS_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), requestor);
      Serializers.STRING.write(connection.out(), userName);
      Access.SERIALIZER.write(connection.out(), access);
      Uuid.SERIALIZER.write(connection.out(), conversation);

      int response = Serializers.INTEGER.read(connection.in());
      if (response == NetworkCode.CHANGE_ACCESS_RESPONSE_OK) {
        LOG.info("Changed access success");
        return true;
      } else if (response == NetworkCode.CHANGE_ACCESS_RESPONSE_FAIL) {
        LOG.info("Unable to change access");
        return false;
      } else {
        LOG.error("Response from server failed.");
        return false;
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
      return false;
    }
  }

  public Access getAccess(Uuid conversation, Uuid user) {
    Access response = null;
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_ACCESS_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Uuid.SERIALIZER.write(connection.out(), user);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_ACCESS_RESPONSE) {
        response = Serializers.nullable(Access.SERIALIZER).read(connection.in());
        LOG.info("Got access success");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }
    return response;
  }

  public boolean joinConversation(Uuid conversation, Uuid user) {
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.JOIN_CONVERSATION_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Uuid.SERIALIZER.write(connection.out(), user);

      int response = Serializers.INTEGER.read(connection.in());
      if (response == NetworkCode.JOIN_CONVERSATION_RESPONSE_OK) {
        LOG.info("User joined success");
        return true;
      } else if (response == NetworkCode.JOIN_CONVERSATION_RESPONSE_NO_ACCESS) {
        LOG.info("User has insufficient access to join");
        return false;
      } else {
        System.out.println("Response from server failed.");
        LOG.error("Response from server failed.");
        return false;
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
      return false;
    }
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {

    Message response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), author);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Serializers.STRING.write(connection.out(), body);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = Serializers.nullable(Message.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public User newUser(String name, PasswordHasher hasher) {

    User response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      PasswordHasher.SERIALIZER.write(connection.out(), hasher);
      Serializers.STRING.write(connection.out(), name);
      LOG.info("newUser: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner)  {

    ConversationHeader response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(ConversationHeader.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }
  @Override
  public boolean addUserInterest(String name, Uuid owner) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_INTEREST_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_INTEREST_RESPONSE) {
        response = Serializers.BOOLEAN.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean removeUserInterest(String name, Uuid owner) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.REMOVE_USER_INTEREST_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.REMOVE_USER_INTEREST_RESPONSE) {
        response = Serializers.BOOLEAN.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean addConversationInterest(String title, Uuid owner) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_INTEREST_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_INTEREST_RESPONSE) {
            response = Serializers.BOOLEAN.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public boolean removeConversationInterest(String title, Uuid owner) {

    boolean response = false;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.REMOVE_CONVERSATION_INTEREST_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.REMOVE_CONVERSATION_INTEREST_RESPONSE) {
            response = Serializers.BOOLEAN.read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }
}
