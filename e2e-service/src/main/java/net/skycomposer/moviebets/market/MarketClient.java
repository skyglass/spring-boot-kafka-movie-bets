package net.skycomposer.moviebets.market;

import jakarta.validation.Valid;
import net.skycomposer.moviebets.common.dto.market.CancelMarketRequest;
import net.skycomposer.moviebets.common.dto.market.CloseMarketRequest;
import net.skycomposer.moviebets.common.dto.market.MarketData;
import net.skycomposer.moviebets.common.dto.market.MarketResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "market")
public interface MarketClient {

    @GetMapping("/get-state/{marketId}")
    MarketData getState(@PathVariable("marketId") String marketId);

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    MarketResponse open(@RequestBody @Valid MarketData marketData);

    @PostMapping("/update")
    MarketResponse update(@RequestBody @Valid MarketData marketData);

    @PostMapping("/close")
    MarketResponse close(@RequestBody @Valid CloseMarketRequest request);

    @PostMapping("/cancel")
    MarketResponse cancel(@RequestBody @Valid CancelMarketRequest request);
}
