package net.skycomposer.moviebets.bet.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
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

    @GetMapping("/get-state/{betId}")
    public BetData getState(@PathVariable UUID betId) {
      return betService.getState(betId);
    }

    @GetMapping("/get-bets-by-market/{marketId}")
    public SumStakesData getBetsByMarket(@PathVariable UUID marketId) {
      return betService.getBetsByMarket(marketId);
    }

    @GetMapping("/get-bets-for-market/{marketId}")
    public BetDataList getBetsForMarket(@PathVariable UUID marketId) {
      return betService.getBetsForMarket(marketId);
    }

    @GetMapping("/get-bets-for-player/{customerId}")
    public BetDataList getBetsForPlayer(@PathVariable String customerId) {
      return betService.getBetsForPlayer(customerId);
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    public BetResponse open(@RequestBody @Valid BetData betData) {
        return betService.open(betData);
    }

    @PostMapping("/cancel")
    public BetResponse close(@RequestBody @Valid CancelBetRequest request) {
        return betService.close(request);
    }
}
