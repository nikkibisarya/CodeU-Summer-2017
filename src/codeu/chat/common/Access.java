package codeu.chat.common;

public enum Access {
  MEMBER (3), OWNER (2), CREATOR (1), NO_ACCESS (0);
  // NO_ACCESS is used for ChangeAccessRequest

  private final int access;
  Access(int access) {
    this.access = access;
  }
  int getAccessNum() {
    return this.access;
  }
  static Access get(int i) {
    switch (i) {
      case 1: return Access.CREATOR;
      case 2: return Access.OWNER;
      case 3: return Access.MEMBER;
      default: return Access.NO_ACCESS;
    }
  }
}
