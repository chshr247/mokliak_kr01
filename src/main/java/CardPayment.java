public class CardPayment implements PaymentMethod {
    @Override
    public void pay(double amount) throws AppException {
        if (amount > 40000) {
            throw new AppException("Maximum is 40.000!!!!");
        }
        System.out.println("Paid " + amount + "with card");
    }
}