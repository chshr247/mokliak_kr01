public class BankTransferPayment implements PaymentMethod {
    @Override
    public void pay(double amount) throws AppException {
        double commission = amount * 0.03;
        double finalAmount = amount + commission;
        System.out.println("Paid " + finalAmount + " with bank transfer (comission is 3%)");
    }
}