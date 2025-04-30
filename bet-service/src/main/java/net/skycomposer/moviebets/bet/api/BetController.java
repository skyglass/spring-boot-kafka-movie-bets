package net.skycomposer.moviebets.bet.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.common.dto.bet.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BetController {

    @GetMapping("/get-state/{betId}")
    public BetData getState(@PathVariable String betId) {
      //TODO
      return null;
    }

    @GetMapping("/get-bets-by-market/{marketId}")
    public SumStakesData getBetsByMarket(@PathVariable String marketId) {
      //TODO
      return null;
    }

    @GetMapping("/get-bets-for-market/{marketId}")
    public BetDataList getBetsForMarket(@PathVariable String marketId) {
      //TODO
      return null;
    }

    @GetMapping("/get-bets-for-player/{walletId}")
    public BetDataList getBetsForPlayer(@PathVariable String walletId) {
      //TODO
      return null;
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    public BetResponse open(@RequestBody @Valid BetData betData) {
      log.info("Open new bet {}", betData);
      //TODO
      return null;
    }

    @PostMapping("/cancel")
    public BetResponse close(@RequestBody @Valid CancelBetRequest request) {
      log.info("Cancel bet {}", request);
      //TODO
      return null;
    }
}
