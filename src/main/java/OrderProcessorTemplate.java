import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public abstract class OrderProcessorTemplate {
    private static final Logger log = LoggerFactory.getLogger(OrderProcessorTemplate.class);

    private final PaymentMethod paymentMethod;
    private final OrderRepository repository;

    protected OrderProcessorTemplate(PaymentMethod paymentMethod, OrderRepository repository) {
        this.paymentMethod = paymentMethod;
        this.repository = repository;
    }

    public final void process(String orderId) {
        log.info("Processing order started. ID: {}", orderId);

        try {
            Order order = repository.findById(orderId).orElseThrow(() -> {
                log.warn("Order not found in repository: {}", orderId);
                return new AppException("Order not found");
            });

            validate(order);
            double finalAmount = calculate(order);

            try {
                pay(order, finalAmount);
                log.info("Payment processed successfully for order: {}", orderId);
            } catch (Exception e) {
                InfrastructureException infra = new InfrastructureException("Payment gateway failure", e);
                log.error("Infrastructure failure during payment for order: {}", orderId, infra);
                throw new AppException("Payment system error", infra);
            }

            ship(order);
            deliver(order);
            archiveAfterDelivery(order);

            log.info("Order {} processed and archived successfully", orderId);

        } catch (ArchiveOperationException e) {
            log.warn("Archive failed: {}", e.getMessage());
            throw e;
        } catch (AppException e) {
            log.warn("Process stopped due to business rule: {}", e.getMessage());
            throw e;
        }
    }

    public Optional<Order> findOrderById(String id) {
        return repository.findById(id);
    }

    protected void validate(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getTotalPrice() > 50000) {
                throw new AppException("Price " + item.getTotalPrice() + " is more than 50.000");
            }
        }
    }

    protected double calculate(Order order) {
        double amount = order.getTotalAmount();
        if (order.getCategory() == OrderCategory.CLEARANCE) {
            amount = amount * 0.8;
        }
        return amount;
    }

    protected void pay(Order order, double amount) {
        paymentMethod.pay(amount);
        order.setStatus(OrderStatus.PAID);
    }

    protected abstract void ship(Order order);
    protected abstract void deliver(Order order);

    protected void archiveAfterDelivery(Order order) {
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new ArchiveOperationException("Cannot archive. Expected DELIVERED, but was " + order.getStatus());
        }
        order.setStatus(OrderStatus.ARCHIVED);
    }
}