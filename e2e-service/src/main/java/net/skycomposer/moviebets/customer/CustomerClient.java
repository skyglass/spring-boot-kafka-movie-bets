package net.skycomposer.moviebets.customer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import net.skycomposer.moviebets.common.dto.customer.CustomerData;
import net.skycomposer.moviebets.common.dto.customer.CustomerResponse;

import java.util.UUID;

@FeignClient(name = "customer")
public interface CustomerClient {

    @GetMapping("/get-customer/{customerId}")
    CustomerData findCustomerById(@PathVariable("customerId") String customerId);

    @PostMapping("/add-funds/{customerId}/{requestId}/{funds}")
    public CustomerResponse addFunds(@PathVariable String customerId, @PathVariable UUID requestId, @PathVariable int funds);

    @PostMapping("/remove-funds/{customerId}/{requestId}/{funds}")
    public CustomerResponse removeFunds(@PathVariable String customerId, @PathVariable UUID requestId, @PathVariable int funds);
}
