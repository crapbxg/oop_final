package util;

/**
 * Interface representing a payment processing system.
 * Used by Account to simulate payments.
 */
public interface PaymentGateway {

    /**
     * Processes a payment request.
     *
     * @param username The owner of the account attempting payment.
     * @param amount   The amount to be charged.
     * @return true if payment succeeds, false otherwise.
     */
    boolean processPayment(String username, double amount);
}
