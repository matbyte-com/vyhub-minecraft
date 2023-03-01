package net.vyhub.VyHubMinecraft.Entity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Group {
    String id;
    String name;
    int permission_level;
    String color;
    Map<String, Property> properties;
    boolean is_team;
    List<GroupMapping> mappings;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPermission_level() {
        return permission_level;
    }

    public void setPermission_level(int permission_level) {
        this.permission_level = permission_level;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    public boolean isIs_team() {
        return is_team;
    }

    public void setIs_team(boolean is_team) {
        this.is_team = is_team;
    }

    public List<GroupMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<GroupMapping> mappings) {
        this.mappings = mappings;
    }
}
