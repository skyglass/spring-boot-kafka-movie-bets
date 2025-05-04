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
import net.skycomposer.moviebets.common.dto.customer.WalletData;
import net.skycomposer.moviebets.common.dto.customer.WalletResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerTestHelper {

    private final CustomerClient customerClient;

    @Async
    public CompletableFuture<WalletResponse> asyncAddFunds(String walletId, UUID requestId, int funds) {
        return CompletableFuture.completedFuture(addFunds(walletId, requestId, funds));
    }

    public WalletResponse createWallet(String walletId, UUID requestId, int funds) {
        return addFunds(walletId, requestId, funds);
    }

    @SneakyThrows
    public WalletResponse addFunds(String walletId, UUID requestId, int funds) {
        WalletResponse response = null;
        while (response == null) {
            try {
                response = customerClient.addFunds(walletId, requestId, funds);
            } catch (FeignException.TooManyRequests e) {
                TimeUnit.MILLISECONDS.sleep(Duration.ofSeconds(1).toMillis());
            }
        }
        return response;
    }

    public WalletResponse removeFunds(String walletId, UUID requestId, int funds) {
        return customerClient.removeFunds(walletId, requestId, funds);
    }

    public WalletData findWalletById(String walletId) {
        return customerClient.findWalletById(walletId);
    }



}

