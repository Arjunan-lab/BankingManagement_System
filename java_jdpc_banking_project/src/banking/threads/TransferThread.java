package banking.threads;

import banking.exceptions.InsufficientFundsException;
import banking.services.BankService;

public class TransferThread extends Thread {
    private BankService service;
    private int from;
    private int to;
    private double amount;

    public TransferThread(BankService service, int from, int to, double amount) {
        this.service = service;
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public void run() {
        try {
            service.transfer(from, to, amount);
            System.out.println("Transfer successful: " + amount + " from " + from + " to " + to);
        } catch (InsufficientFundsException e) {
            System.out.println("Transfer failed (insufficient funds): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }
}
