package net.vyhub.VyHubMinecraft.Entity;

public class Property {
    String name;
    boolean granted;
    String value;

    public Property(String name, boolean granted, String value) {
        this.name = name;
        this.granted = granted;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isGranted() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted = granted;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
