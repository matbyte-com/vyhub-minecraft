package net.vyhub.Entity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VyHubUser {

    private String id;
    private String type;
    private String identifier;
    private String registered_on;
    private String username;
    private String avatar;
    private boolean admin;
    private String credit_account_id;
    private Map<String, String> attributes;
    private String email;
    private boolean email_notification;
    private List<VyHubUser> linked_users;

    public VyHubUser(String id, String type, String identifier, String registered_on, String username, String avatar, boolean admin, String credit_account_id, Map<String, String> attributes, String email, boolean email_notification, List<VyHubUser> linked_users) {
        this.id = id;
        this.type = type;
        this.identifier = identifier;
        this.registered_on = registered_on;
        this.username = username;
        this.avatar = avatar;
        this.admin = admin;
        this.credit_account_id = credit_account_id;
        this.attributes = attributes;
        this.email = email;
        this.email_notification = email_notification;
        this.linked_users = linked_users;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getRegistered_on() {
        return registered_on;
    }

    public void setRegistered_on(String registered_on) {
        this.registered_on = registered_on;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getCredit_account_id() {
        return credit_account_id;
    }

    public void setCredit_account_id(String credit_account_id) {
        this.credit_account_id = credit_account_id;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmail_notification() {
        return email_notification;
    }

    public void setEmail_notification(boolean email_notification) {
        this.email_notification = email_notification;
    }

    public List<VyHubUser> getLinked_users() {
        return linked_users;
    }

    public void setLinked_users(List<VyHubUser> linked_users) {
        this.linked_users = linked_users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VyHubUser that = (VyHubUser) o;
        return identifier.equals(that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
}
