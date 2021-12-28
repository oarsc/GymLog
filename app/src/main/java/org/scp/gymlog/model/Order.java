package org.scp.gymlog.model;

public enum Order {
    ALPHABETICALLY("alphabetically"),
    LAST_USED("last_used");

    public String name;
    Order(String name) {
        this.name = name;
    }

    public static Order getByName(String name) {
        for (Order order: Order.values()) {
            if (order.name.equals(name)){
                return order;
            }
        }
        throw new IllegalArgumentException("No order found for name \""+name+"\"");
    }
}
