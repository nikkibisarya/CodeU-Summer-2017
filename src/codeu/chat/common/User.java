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

package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

public final class User implements Writeable {

  public HashMap<Uuid, Time> UserUpdateMap = new HashMap<Uuid, Time>();
  public HashMap<Uuid, Time> ConvoUpdateMap = new HashMap<Uuid, Time>();
  public final Uuid id;
  public final String name;
  public final Time creation;
  // map from conversation id to Access
  private Map<Uuid, Access> conversationAccessMap;

  public User(Uuid id, String name, Time creation) {
    this.id = id;
    this.name = name;
    this.creation = creation;
    this.conversationAccessMap = new HashMap<>();
  }

  // get the type of this Writeable as a String
  @Override
  public String getType() {
    return USER_STR;
  }

  // write this Writeable as a User
  @Override
  public void write(OutputStream out, Object value) throws IOException {
    SERIALIZER.write(out, (User)value);
  }

  public static final Serializer<User> SERIALIZER = new Serializer<User>() {

    @Override
    public void write(OutputStream out, User value) throws IOException {
      Uuid.SERIALIZER.write(out, value.id);
      Serializers.STRING.write(out, value.name);
      Time.SERIALIZER.write(out, value.creation);

    }

    @Override
    public User read(InputStream in) throws IOException {
      return new User(
          Uuid.SERIALIZER.read(in),
          Serializers.STRING.read(in),
          Time.SERIALIZER.read(in)
      );
    }
  };

  public void addConversationAccess(Uuid conversation, Access access) {
      conversationAccessMap.put(conversation, access);
  }

  public void removeConversationAccess(Uuid conversation) {
    conversationAccessMap.remove(conversation);
  }

  public boolean containsConversationAccess(Uuid conversation) {
    return conversationAccessMap.containsKey(conversation);
  }

  public Access getConversationAccess(Uuid conversation) {
    return conversationAccessMap.get(conversation);
  }
}
