package net.vyhub.Entity;

public class Advert {
    private String id;
    private String title;
    private String content;
    private String color;

    public Advert(String id, String title, String content, String color) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getColor() {
        return color;
    }
}
