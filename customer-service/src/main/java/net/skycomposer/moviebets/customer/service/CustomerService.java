package net.skycomposer.moviebets.customer.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import net.skycomposer.moviebets.common.dto.customer.Customer;
import net.skycomposer.moviebets.common.dto.customer.WalletData;
import net.skycomposer.moviebets.common.dto.customer.WalletResponse;
import net.skycomposer.moviebets.customer.exception.CustomerInsufficientFundsException;

public interface CustomerService {

    List<Customer> findAll();

    Customer findCustomerById(String customerId);

    WalletResponse addFunds(String customerId, UUID requestId, BigDecimal funds);

    WalletResponse addFundsAsync(String customerId, UUID requestId, BigDecimal funds);

    WalletResponse removeFundsAsync(String customerId, UUID requestId, BigDecimal funds);

    WalletResponse removeFunds(String customerId, UUID requestId, BigDecimal funds) throws CustomerInsufficientFundsException;

    WalletData findWalletById(String customerId);

    List<WalletData> findAllWallets();
}
