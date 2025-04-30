package net.skycomposer.moviebets.customer.controller;

import net.skycomposer.moviebets.customer.service.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.customer.WalletData;
import net.skycomposer.moviebets.common.dto.customer.WalletResponse;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

  private static final Integer DEFAULT_REGISTERED_CUSTOMER_AMOUNT = 100;

  private final CustomerService customerService;

  @GetMapping("/get-wallet/{walletId}")
  public WalletData findWalletById(@PathVariable String walletId) {
    return customerService.findWalletById(walletId);
  }

  @GetMapping("/all")
  public List<WalletData> findAll() {
    return customerService.findAllWallets();
  }

  @PostMapping("/register/{walletId}/{requestId}")
  public WalletResponse addFunds(@PathVariable String walletId, @PathVariable String requestId) {
    return customerService.addFunds(walletId, requestId, new BigDecimal(DEFAULT_REGISTERED_CUSTOMER_AMOUNT));
  }

  @PostMapping("/add-funds/{walletId}/{requestId}/{funds}")
  public WalletResponse addFunds(@PathVariable String walletId, @PathVariable String requestId, @PathVariable Integer funds) {
    return customerService.addFunds(walletId, requestId, new BigDecimal(funds));
  }

  @PostMapping("/remove-funds/{walletId}/{requestId}/{funds}")
  public WalletResponse removeFunds(@PathVariable String walletId, @PathVariable String requestId, @PathVariable Integer funds) {
    return customerService.removeFunds(walletId, requestId, new BigDecimal(funds));
  }

}
