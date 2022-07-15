package net.vyhub.VyHubMinecraft.Entity;

import java.util.List;
import java.util.Map;

public class AppliedReward {

    private String id;
    private String active;
    private Reward reward;
    private Map<String, String> user;
    private String applied_packet_id;

    private String status;
    private List<String> executed_on;

    public AppliedReward(String id, String active, Reward reward, Map<String, String> user, String applied_packet_id, String status, List<String> executed_on) {
        this.id = id;
        this.active = active;
        this.reward = reward;
        this.user = user;
        this.applied_packet_id = applied_packet_id;
        this.status = status;
        this.executed_on = executed_on;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public Map<String, String> getUser() {
        return user;
    }

    public void setUser(Map<String, String> user) {
        this.user = user;
    }

    public String getApplied_packet_id() {
        return applied_packet_id;
    }

    public void setApplied_packet_id(String applied_packet_id) {
        this.applied_packet_id = applied_packet_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getExecuted_on() {
        return executed_on;
    }

    public void setExecuted_on(List<String> executed_on) {
        this.executed_on = executed_on;
    }
}
