package net.vyhub.Entity;

import java.util.Map;

public class Server {
    private String name;
    private String serverbundle_id;
    private Map<String, String> extra;
    private String id;

    public Server(String name, String serverbundle_id, Map<String, String> extra, String id) {
        this.name = name;
        this.serverbundle_id = serverbundle_id;
        this.extra = extra;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getServerbundle_id() {
        return serverbundle_id;
    }

    public Map<String, String> getExtra() {
        return extra;
    }

    public String getId() {
        return id;
    }
}
