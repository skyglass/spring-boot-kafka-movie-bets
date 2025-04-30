package net.skycomposer.betting.customer.exception;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class CustomerInsufficientFundsException extends RuntimeException {

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
