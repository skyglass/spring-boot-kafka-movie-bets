package net.skycomposer.moviebets.customer.service;

import net.skycomposer.moviebets.common.dto.customer.Customer;
import net.skycomposer.moviebets.common.dto.customer.WalletData;
import net.skycomposer.moviebets.common.dto.customer.WalletResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerService {

    List<Customer> findAll();

    Customer findCustomerById(String customerId);

    WalletResponse addFunds(String customerId, String requestId, BigDecimal funds);

    WalletResponse removeFunds(String customerId, String requestId, BigDecimal funds);

    WalletData findWalletById(String customerId);

    List<WalletData> findAllWallets();
}
