package com.example.pizza_mania_app.helperClasses;

public class deliveryOrders {
    private int id;
    private String customer;
    private String address;
    private String orderMethod;

    public deliveryOrders(int id, String customer, String address, String orderMethod) {
        this.id = id;
        this.customer = customer;
        this.address = address;
        this.orderMethod = orderMethod;
    }

    // Getters
    public int getId() { return id; }

    public String getCustomer() { return customer; }
    public String getAddress() { return address; }
    public String getOrderMethod() { return orderMethod; }
}
