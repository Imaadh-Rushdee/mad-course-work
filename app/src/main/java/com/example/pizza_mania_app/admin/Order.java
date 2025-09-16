package com.example.pizza_mania_app.admin;

public class Order {
    private long id;
    private String name;
    private String status;

    public Order(long id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }

    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
}
