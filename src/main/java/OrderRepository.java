import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    public void save(Order order) {
        orders.add(order);
    }

    public Optional<Order> findById(String id) {
        return orders.stream().filter(order -> order.getId().equals(id)).findFirst();
    }
}