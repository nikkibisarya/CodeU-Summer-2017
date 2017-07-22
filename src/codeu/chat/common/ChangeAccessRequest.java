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

  // map from conversation id to Access
  private Map<Uuid, Access> map;

  public ChangeAccessRequest(Map<Uuid, Access> map) {
    this.map = map;
  }

  public static final Serializer<ChangeAccessRequest> SERIALIZER = new Serializer<ChangeAccessRequest>() {

    @Override
    public void write(OutputStream out, ChangeAccessRequest value) throws IOException {

      // TODO: add conversationid, access, conversationid, access, ...
      // Uuid.SERIALIZER.write(out, value.id);
      // Serializers.STRING.write(out, value.name);
      // Time.SERIALIZER.write(out, value.creation);
      Serializers.INTEGER.write(out, value.map.size());
      for (Uuid id : value.map.keySet()) {
        Uuid.SERIALIZER.write(out, id);
        Serializers.INTEGER.write(out, value.map.get(id).getAccessNum());
      }

    }

    @Override
    public ChangeAccessRequest read(InputStream in) throws IOException {

      return new ChangeAccessRequest(new HashMap<Uuid, Access>());
      // TODO: add reading convoid, access, convoid, access, ...
      // add to map
      // return new ChangeAccessRequest(map)

      // return new User(
      //     Uuid.SERIALIZER.read(in),
      //     Serializers.STRING.read(in),
      //     Time.SERIALIZER.read(in)
      // );
    }
  };

}
