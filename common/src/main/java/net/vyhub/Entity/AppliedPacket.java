package net.vyhub.Entity;

public class AppliedPacket {
    private String id;
    private Purchase purchase;

    private Packet packet;

    public AppliedPacket(String id, Purchase purchase, Packet packet) {
        this.id = id;
        this.purchase = purchase;
        this.packet = packet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
