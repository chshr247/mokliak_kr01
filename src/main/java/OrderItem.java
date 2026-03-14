public class OrderItem {
    private final String productName;
    private final int quantity;
    private final Money price;

    public OrderItem(String productName, int quantity, Money price) {
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public double getTotalPrice() {
        return price.getAmount() * quantity;
    }

    public String getProductName() { return productName; }
}