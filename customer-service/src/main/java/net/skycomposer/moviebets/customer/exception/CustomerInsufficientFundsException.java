package net.skycomposer.moviebets.customer.exception;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class CustomerInsufficientFundsException extends Exception {

    private BigDecimal requiredAmount;

    private BigDecimal availableAmount;

    public CustomerInsufficientFundsException(String customerId, BigDecimal requiredAmount, BigDecimal availableAmount) {
        super(String.format(
                "Customer %s has insufficient funds: requiredAmount = %s, availableAmount = %s",
                customerId, requiredAmount, availableAmount));
        this.requiredAmount = requiredAmount;
        this.availableAmount = availableAmount;
    }

}
