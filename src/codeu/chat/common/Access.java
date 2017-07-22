package codeu.chat.common;

public enum Access {
  MEMBER (3), OWNER (2), CREATOR (1);

  private final int access;
  Access(int access) {
    this.access = access;
  }
  int getAccessNum() {
    return this.access;
  }
}
