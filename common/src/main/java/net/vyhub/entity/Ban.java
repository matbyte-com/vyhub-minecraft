package net.vyhub.entity;

public class Ban {
    private String id;
    private String reason;
    private Integer length;
    private String ends_on;
    private String status;
    private Boolean active;

    public Ban(String id, String reason, Integer length, String ends_on, String status, Boolean active) {
        this.id = id;
        this.reason = reason;
        this.length = length;
        this.ends_on = ends_on;
        this.status = status;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getReason() {
        return reason;
    }

    public Integer getLength() {
        return length;
    }

    public String getEnds_on() {
        return ends_on;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getActive() {
        return active;
    }
}
