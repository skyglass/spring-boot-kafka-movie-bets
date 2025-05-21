package net.skycomposer.moviebets.bet.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.common.dto.bet.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BetController {

    private final BetService betService;

    @GetMapping("/get-market-status/{marketId}")
    public MarketStatusData getMarketStatus(@PathVariable UUID marketId, Authentication authentication) {
        String customerId = authentication.getName();
        return betService.getMarketStatus(marketId, customerId);
    }

    @GetMapping("/get-state/{betId}")
    public BetData getState(@PathVariable UUID betId) {
      return betService.getState(betId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-bets-by-market/{marketId}")
    public SumStakesData getBetsByMarket(@PathVariable UUID marketId) {
      return betService.getBetsByMarket(marketId);
    }

    @GetMapping("/get-bets-for-market/{marketId}")
    public BetDataList getBetsForMarket(@PathVariable UUID marketId) {
      return betService.getBetsForMarket(marketId, false);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-bets-for-market-for-admin/{marketId}")
    public BetDataList getBetsForMarketForAdmin(@PathVariable UUID marketId) {
        return betService.getBetsForMarket(marketId, true);
    }

    @GetMapping("/get-bets-for-player/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.name")
    public BetDataList getBetsForPlayer(@PathVariable String customerId) {
      return betService.getBetsForPlayer(customerId);
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    public BetResponse open(@RequestBody @Valid BetData betData, Authentication authentication) {
        String authenticatedCustomerId = authentication.getName();
        return betService.open(betData, authenticatedCustomerId);
    }

    @PostMapping("/cancel")
    public BetResponse close(@RequestBody @Valid CancelBetRequest request, Authentication authentication) {
        String authenticatedCustomerId = authentication.getName();
        return betService.close(request, authenticatedCustomerId);
    }
}
