package net.vyhub.entity;

public class Warn {
    private String reason;
    private String id;

    public Warn(String reason, String id) {
        this.reason = reason;
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public String getId() {
        return id;
    }
}
