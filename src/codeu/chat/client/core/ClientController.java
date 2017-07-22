package codeu.chat.common;

import codeu.chat.util.Uuid;

public interface ClientController extends BasicController {
  public void joinConversation(Uuid conversation, Uuid user);
  public String getAccess(Uuid conversation, Uuid user);
}
