package net.skycomposer.moviebets.bet;

import jakarta.validation.Valid;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.BetResponse;
import net.skycomposer.moviebets.common.dto.bet.CancelBetRequest;
import net.skycomposer.moviebets.common.dto.bet.SumStakesData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "bet")
public interface BetClient {

    @GetMapping("/get-state/{betId}")
    BetData getState(@PathVariable("betId") String betId);

    @GetMapping("/get-bets-by-market/{marketId}")
    SumStakesData getBetsByMarket(@PathVariable("marketId") String marketId);

    @PostMapping("/place-for-admin")
    @ResponseStatus(HttpStatus.CREATED)
    BetResponse place(@RequestBody @Valid BetData betData);

    @PostMapping("/cancel-for-admin")
    BetResponse cancel(@RequestBody @Valid CancelBetRequest request);
}
