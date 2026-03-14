public interface PaymentMethod {
    void pay(double amount) throws AppException;
}