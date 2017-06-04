package codeu.chat.common;

/**
 * Created by Nikki on 5/22/17.
 */
import codeu.chat.util.Uuid;

import java.io.IOException;

public final class ServerInfo {
    private final static String SERVER_VERSION = "1.0.0";

    private Uuid version;
    public ServerInfo() {
        try {
            this.version = Uuid.parse(SERVER_VERSION);
        }
        catch(IOException e) {
            //this.version = Uuid.NULL;
        }
    }
    public ServerInfo(Uuid version) {
        this.version = version;
    }

    public Uuid getVersion() {
        return (this.version == null) ? Uuid.NULL : version;
    }

    @Override
    public String toString() {
        return getVersion().toString();
    }
}
