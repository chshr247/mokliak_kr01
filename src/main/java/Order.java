public class Order {
    private final String id;
    private final String customerEmail;
    private final OrderItem[] items;
    private OrderStatus status;
    private final OrderCategory category;

    public Order(String id, String customerEmail, OrderItem[] items, OrderStatus status, OrderCategory category) {
        this.id = id;
        this.customerEmail = customerEmail;
        if (items != null) this.items = items.clone();
        else this.items = new OrderItem[0];
        this.status = status;
        this.category = category;
    }

    public Order(String id, String customerEmail, OrderItem[] items, OrderCategory category) {
        this(id, customerEmail, items, OrderStatus.NEW, category);
    }

    public String getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public OrderItem[] getItems() {
        return items.clone();
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderCategory getCategory() {
        return category;
    }

    public double getTotalAmount() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }
}