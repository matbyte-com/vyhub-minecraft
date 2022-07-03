package com.minecraft.Entity;

import java.util.Map;

public class AppliedReward {

    Reward reward;
    String id;
    Map<String, String> user;
    String applied_packet_id;

    public AppliedReward(Reward reward, String id, Map<String, String> user, String applied_packet_id) {
        this.reward = reward;
        this.id = id;
        this.user = user;
        this.applied_packet_id = applied_packet_id;
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
