package com.minecraft.Entity;

import java.util.Map;

public class Reward {

    Map<String, String> data;
    String name;
    Boolean once;
    String on_event;
    String id;
    String type;

    public Reward(Map<String, String> data, String name, Boolean once, String on_event, String id, String type) {
        this.data = data;
        this.name = name;
        this.once = once;
        this.on_event = on_event;
        this.id = id;
        this.type = type;
    }

    public Map<String, String> getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public Boolean getOnce() {
        return once;
    }

    public String getOn_event() {
        return on_event;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}

