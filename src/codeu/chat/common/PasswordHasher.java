package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;

public class PasswordHasher {

  public static final Serializer<PasswordHasher> SERIALIZER = new Serializer<PasswordHasher>() {

    @Override
    public void write(OutputStream out, PasswordHasher value) throws IOException {

      Serializers.INTEGER.write(out, value.length);
      out.write(value.hashed);

    }

    @Override
    public PasswordHasher read(InputStream in) throws IOException {
      int length = Serializers.INTEGER.read(in);
      byte[] hashed = new byte[length];
      in.read(hashed);
      return new PasswordHasher(hashed);
    }
  };

  private int length;
  private byte[] hashed;
  public PasswordHasher(char[] password) {
    this.hashed = hashPassword(password);
    this.length = this.hashed.length;
  }

  public PasswordHasher(byte[] hashed) {
    this.hashed = hashed;
    this.length = this.hashed.length;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (o == null || !(o instanceof PasswordHasher))
      return false;
    PasswordHasher other = (PasswordHasher) o;
    return this.length == other.length && Arrays.equals(this.hashed, other.hashed);
  }

  private byte[] hashPassword(char[] password) {
    try {
      byte[] salt = new byte[32];
      for (int i = 0; i < 32; i++) {
        salt[i] = (byte) i;
      }
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
      PBEKeySpec pbspec = new PBEKeySpec(password, salt, 1024, 256);
      byte[] hashed = factory.generateSecret(pbspec).getEncoded();
      return hashed;
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException(e);
    }
  }
}
