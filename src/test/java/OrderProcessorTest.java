import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OrderProcessorTest {

    private OrderRepository repository;
    private DefaultOrderProcessor processor;

    @BeforeEach
    void setUp() {
        repository = new OrderRepository();
        processor = new DefaultOrderProcessor(new CardPayment(), repository);
    }

    // Positive tests
    @Test
    void process_happyPath_orderBecomesArchived() {
        Order order = new Order("1", "user@test.com",
                new OrderItem[]{new OrderItem("Laptop", 1, new Money(1000))},
                OrderCategory.REGULAR);
        repository.save(order);

        processor.process("1");

        assertEquals(OrderStatus.ARCHIVED, order.getStatus());
    }

    @Test
    void calculate_clearanceOrder_applies20PercentDiscount() {
        Order order = new Order("2", "user@test.com",
                new OrderItem[]{new OrderItem("Phone", 1, new Money(1000))},
                OrderCategory.CLEARANCE);
        repository.save(order);
        DefaultOrderProcessor proc = new DefaultOrderProcessor(new BankTransferPayment(), repository);

        proc.process("2");

        assertEquals(OrderStatus.ARCHIVED, order.getStatus());
    }

    @Test
    void process_withPayPalPayment_succeeds() {
        Order order = new Order("3", "user@test.com",
                new OrderItem[]{new OrderItem("Tablet", 1, new Money(600))},
                OrderCategory.REGULAR);
        repository.save(order);
        DefaultOrderProcessor proc = new DefaultOrderProcessor(new PayPalPayment(), repository);

        assertDoesNotThrow(() -> proc.process("3"));
    }

    @Test
    void findOrderById_existingOrder_returnsPresent() {
        Order order = new Order("4", "user@test.com",
                new OrderItem[]{new OrderItem("Book", 2, new Money(50))},
                OrderCategory.REGULAR);
        repository.save(order);

        assertTrue(processor.findOrderById("4").isPresent());
    }

    @Test
    void findOrderById_unknownId_returnsEmpty() {
        assertTrue(processor.findOrderById("nonexistent").isEmpty());
    }

    @Test
    void process_bankTransferPayment_addsCommissionAndSucceeds() {
        Order order = new Order("5", "user@test.com",
                new OrderItem[]{new OrderItem("Monitor", 1, new Money(500))},
                OrderCategory.REGULAR);
        repository.save(order);
        DefaultOrderProcessor proc = new DefaultOrderProcessor(new BankTransferPayment(), repository);

        assertDoesNotThrow(() -> proc.process("5"));
        assertEquals(OrderStatus.ARCHIVED, order.getStatus());
    }


    // Negative tests
    @Test
    void process_orderNotFound_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> processor.process("ghost"));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void cardPayment_amountOver40000_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> new CardPayment().pay(40001));
        assertTrue(ex.getMessage().contains("40.000"));
    }

    @Test
    void payPalPayment_amountUnder500_throwsAppException() {
        AppException ex = assertThrows(AppException.class, () -> new PayPalPayment().pay(499));
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    void archiveAfterDelivery_wrongStatus_throwsArchiveOperationException() {
        Order order = new Order("6", "user@test.com",
                new OrderItem[]{new OrderItem("Pen", 1, new Money(10))},
                OrderStatus.SHIPPED,
                OrderCategory.REGULAR);

        assertThrows(ArchiveOperationException.class,
                () -> processor.archiveAfterDelivery(order));
    }

    // Negative parametrized test
    @ParameterizedTest
    @ValueSource(doubles = {50001, 75000, 100000})
    void validate_itemPriceExceeds50000_throwsAppException(double price) {
        Order order = new Order("7", "user@test.com",
                new OrderItem[]{new OrderItem("Expensive", 1, new Money(price))},
                OrderCategory.REGULAR);
        repository.save(order);

        AppException ex = assertThrows(AppException.class, () -> processor.process("7"));
        assertTrue(ex.getMessage().contains("50.000"));

        repository = new OrderRepository();
        processor = new DefaultOrderProcessor(new CardPayment(), repository);
    }
}