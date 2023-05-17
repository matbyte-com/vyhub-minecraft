package net.vyhub.Entity;

public class Purchase {
    private String id;
    private String amount_text;

    public Purchase(String id, String amount_text) {
        this.id = id;
        this.amount_text = amount_text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount_text() {
        return amount_text;
    }

    public void setAmount_text(String amount_text) {
        this.amount_text = amount_text;
    }
}
