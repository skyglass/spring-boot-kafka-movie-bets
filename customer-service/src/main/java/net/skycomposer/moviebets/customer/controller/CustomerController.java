package net.skycomposer.moviebets.customer.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.customer.CustomerData;
import net.skycomposer.moviebets.common.dto.customer.CustomerResponse;
import net.skycomposer.moviebets.customer.service.CustomerApplicationService;
import net.skycomposer.moviebets.customer.service.CustomerService;

@RestController
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;

  private final CustomerApplicationService customerApplicationService;

  @GetMapping("/get-customer/{customerId}")
  public CustomerData findCustomerById(@PathVariable String customerId) {
    return customerService.findCustomerById(customerId);
  }

  @GetMapping("/get-or-create-customer/{customerId}/{requestId}")
  public CustomerData findCustomerById(@PathVariable String customerId, @PathVariable UUID requestId) {
    return customerApplicationService.findOrCreateCustomerById(customerId, requestId);
  }

  @GetMapping("/all")
  public List<CustomerData> findAll() {
    return customerService.findAll();
  }

  @PostMapping("/register/{customerId}/{requestId}")
  public CustomerResponse registerCustomer(@PathVariable String customerId, @PathVariable UUID requestId) {
    return customerApplicationService.registerCustomer(customerId, requestId);
  }

  @PostMapping("/add-funds/{customerId}/{requestId}/{funds}")
  public CustomerResponse addFunds(@PathVariable String customerId, @PathVariable UUID requestId, @PathVariable Integer funds) {
    return customerService.addFundsAsync(customerId, requestId, new BigDecimal(funds));
  }

  @PostMapping("/remove-funds/{customerId}/{requestId}/{funds}")
  public CustomerResponse removeFunds(@PathVariable String customerId, @PathVariable UUID requestId, @PathVariable Integer funds) {
    return customerService.removeFundsAsync(customerId, requestId, new BigDecimal(funds));
  }

}
