package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import codeu.chat.util.Serializers;
import codeu.chat.util.Serializer;
import codeu.chat.util.Uuid;
import codeu.chat.common.Access;

public class ChangeAccessRequest implements Writeable {
  // map from conversation id to Access
  public final Uuid user;
  public final Uuid conversation;
  public final Access access;

  public ChangeAccessRequest(Uuid user, Uuid conversation, Access access) {
    this.user = user;
    this.conversation = conversation;
    this.access = access;
  }

  // get the type of this Writeable as a String
  @Override
  public String getType() {
    return CHANGE_ACCESS_REQUEST_STR;
  }

  // write this Writeable as a User
  @Override
  public void write(OutputStream out, Object value) throws IOException {
    SERIALIZER.write(out, (ChangeAccessRequest)value);
  }

  public static final Serializer<ChangeAccessRequest> SERIALIZER = new Serializer<ChangeAccessRequest>() {
    @Override
    public void write(OutputStream out, ChangeAccessRequest value) throws IOException {
      Uuid.SERIALIZER.write(out, value.user);
      Uuid.SERIALIZER.write(out, value.conversation);
      Access.SERIALIZER.write(out, value.access);
    }

    @Override
    public ChangeAccessRequest read(InputStream in) throws IOException {
      return new ChangeAccessRequest(Uuid.SERIALIZER.read(in), Uuid.SERIALIZER.read(in), Access.SERIALIZER.read(in));
    }
  };

}
