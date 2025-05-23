package net.skycomposer.moviebets.customer.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.customer.CustomerData;
import net.skycomposer.moviebets.common.dto.customer.CustomerResponse;
import net.skycomposer.moviebets.customer.exception.CustomerNotFoundException;

@Component
@RequiredArgsConstructor
public class CustomerApplicationService {

    private final CustomerService customerService;

    public CustomerData findOrCreateCustomerById(String customerId, UUID requestId) {
        try {
            return customerService.findCustomerById(customerId);
        } catch (CustomerNotFoundException e) {
            registerCustomer(customerId, requestId);
            return customerService.findCustomerById(customerId);
        }
    }

    public CustomerResponse registerCustomer(String customerId, UUID requestId) {
        return customerService.addFunds(customerId, requestId, new BigDecimal(CustomerService.DEFAULT_REGISTERED_CUSTOMER_BALANCE));
    }
}
