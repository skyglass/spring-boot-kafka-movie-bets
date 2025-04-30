package net.skycomposer.betting.market.controller;

import net.skycomposer.betting.common.domain.dto.market.CancelMarketRequest;
import net.skycomposer.betting.common.domain.dto.market.CloseMarketRequest;
import net.skycomposer.betting.common.domain.dto.market.MarketData;
import net.skycomposer.betting.common.domain.dto.market.MarketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.betting.market.service.MarketService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/get-state/{marketId}")
    public MarketData getState(@PathVariable UUID marketId) {
      return marketService.findMarketById(marketId);
    }

    @GetMapping("/all")
    public List<MarketData> getAllMarkets() {
      return marketService.findAll();
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    public MarketResponse open(@RequestBody @Valid MarketData marketData) {
      return marketService.open(marketData);
    }

    @PostMapping("/update")
    public MarketResponse update(@RequestBody @Valid MarketData marketData) {
      return marketService.update(marketData);
    }

    @PostMapping("/close")
    public MarketResponse close(@RequestBody @Valid CloseMarketRequest request) {
        return marketService.close(request);
    }

    @PostMapping("/cancel")
    public MarketResponse cancel(@RequestBody @Valid CancelMarketRequest request) {
        return marketService.cancel(request);
    }
}
