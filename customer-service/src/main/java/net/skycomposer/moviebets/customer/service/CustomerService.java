package net.skycomposer.moviebets.customer.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import net.skycomposer.moviebets.common.dto.customer.CustomerData;
import net.skycomposer.moviebets.common.dto.customer.CustomerResponse;
import net.skycomposer.moviebets.customer.exception.CustomerInsufficientFundsException;

public interface CustomerService {

    public static final Integer DEFAULT_REGISTERED_CUSTOMER_BALANCE = 100;

    CustomerData findCustomerById(String customerId);

    CustomerResponse addFunds(String customerId, UUID requestId, BigDecimal funds);

    CustomerResponse addFundsAsync(String customerId, UUID requestId, BigDecimal funds);

    CustomerResponse removeFundsAsync(String customerId, UUID requestId, BigDecimal funds);

    CustomerResponse removeFunds(String customerId, UUID requestId, BigDecimal funds) throws CustomerInsufficientFundsException;

    List<CustomerData> findAll();
}
