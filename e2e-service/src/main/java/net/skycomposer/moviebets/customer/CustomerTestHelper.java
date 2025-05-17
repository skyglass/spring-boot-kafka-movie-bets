package net.skycomposer.moviebets.customer;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.common.dto.customer.CustomerData;
import net.skycomposer.moviebets.common.dto.customer.CustomerResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerTestHelper {

    private final CustomerClient customerClient;

    @Async
    public CompletableFuture<CustomerResponse> asyncAddFunds(String customerId, UUID requestId, int funds) {
        return CompletableFuture.completedFuture(addFunds(customerId, requestId, funds));
    }

    public CustomerResponse createCustomer(String customerId, UUID requestId, int funds) {
        return addFunds(customerId, requestId, funds);
    }

    @SneakyThrows
    public CustomerResponse addFunds(String customerId, UUID requestId, int funds) {
        CustomerResponse response = null;
        while (response == null) {
            try {
                response = customerClient.addFunds(customerId, requestId, funds);
            } catch (FeignException.TooManyRequests e) {
                TimeUnit.MILLISECONDS.sleep(Duration.ofSeconds(1).toMillis());
            }
        }
        return response;
    }

    public CustomerData findCustomerById(String customerId) {
        return customerClient.findCustomerById(customerId);
    }



}

