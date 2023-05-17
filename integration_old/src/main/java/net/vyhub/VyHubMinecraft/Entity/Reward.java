package net.vyhub.VyHubMinecraft.Entity;

import java.util.Map;

public class Reward {
    private String name;
    private String type;
    private Map<String, String> data;

    private Integer order;
    private Boolean once;

    private Boolean once_from_all;
    private String on_event;
    private String id;

    private Map<String, String> serverbundle;


    public Reward(String name, String type, Map<String, String> data, Integer order, Boolean once, Boolean once_from_all, String on_event, String id, Map<String, String> serverbundle) {
        this.name = name;
        this.type = type;
        this.data = data;
        this.order = order;
        this.once = once;
        this.once_from_all = once_from_all;
        this.on_event = on_event;
        this.id = id;
        this.serverbundle = serverbundle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Boolean getOnce() {
        return once;
    }

    public void setOnce(Boolean once) {
        this.once = once;
    }

    public Boolean getOnce_from_all() {
        return once_from_all;
    }

    public void setOnce_from_all(Boolean once_from_all) {
        this.once_from_all = once_from_all;
    }

    public String getOn_event() {
        return on_event;
    }

    public void setOn_event(String on_event) {
        this.on_event = on_event;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getServerbundle() {
        return serverbundle;
    }

    public void setServerbundle(Map<String, String> serverbundle) {
        this.serverbundle = serverbundle;
    }
}