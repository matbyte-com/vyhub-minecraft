package net.vyhub.entity;

/*
[
  {
    "uuid": "7b50eee0-7ae5-48de-b41d-9a6b3be3faca",
    "name": "ibot3",
    "created": "2022-07-18 13:40:41 +0200",
    "source": "Server",
    "expires": "forever",
    "reason": "test"
  }
]
 */

public class MinecraftBan {
    private String uuid;
    private String name;
    private String created;
    private String source;
    private String expires;
    private String reason;

    public MinecraftBan(String uuid, String name, String created, String source, String expires, String reason) {
        this.uuid = uuid;
        this.name = name;
        this.created = created;
        this.source = source;
        this.expires = expires;
        this.reason = reason;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getCreated() {
        return created;
    }

    public String getSource() {
        return source;
    }

    public String getExpires() {
        return expires;
    }

    public String getReason() {
        return reason;
    }
}
