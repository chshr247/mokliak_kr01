public class PayPalPayment implements PaymentMethod {
    @Override
    public void pay(double amount) throws AppException {
        if (amount < 500) {
            throw new AppException("Minimum 500!!!");
        }
        System.out.println("Paid " + amount + " with PayPal");
    }
}