package codeu.chat.common;

import codeu.chat.util.Uuid;

public interface ClientController extends BasicController {
  public boolean joinConversation(Uuid conversation, Uuid user);
  public Access getAccess(Uuid conversation, Uuid user);
  public boolean changeAccess(Uuid requestor, String userName, Access access, Uuid conversation);
}
