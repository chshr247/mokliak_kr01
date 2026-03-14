public class DefaultOrderProcessor extends OrderProcessorTemplate {

    public DefaultOrderProcessor(PaymentMethod paymentMethod, OrderRepository repository) {
        super(paymentMethod, repository);
    }

    @Override
    protected void ship(Order order) {
        order.setStatus(OrderStatus.SHIPPED);
    }

    @Override
    protected void deliver(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
    }
}