package codeu.chat.common;

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public enum Access {
  MEMBER {
    @Override
    public String toString() {
      return "member";
    }
  },
  OWNER {
    @Override
    public String toString() {
      return "owner";
    }
  },
  CREATOR {
    @Override
    public String toString() {
      return "creator";
    }
  },
  NO_ACCESS {
    @Override
    public String toString() {
      return "remove";
    }
  };

  public static Access getAccess(String str) {
    str = str.toLowerCase();
    switch (str) {
      case "member": return Access.MEMBER;
      case "owner": return Access.OWNER;
      case "creator": return Access.CREATOR;
      case "remove": return Access.NO_ACCESS;
      default: return null;
    }
  }

  public static final Serializer<Access> SERIALIZER = new Serializer<Access>() {

    @Override
    public void write(OutputStream out, Access value) throws IOException {
      Serializers.STRING.write(out, value.toString());
    }

    @Override
    public Access read(InputStream in) throws IOException {
      return getAccess(Serializers.STRING.read(in));
    }
  };
}
