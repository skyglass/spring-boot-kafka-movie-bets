package net.skycomposer.moviebets.market.controller;

import net.skycomposer.moviebets.common.dto.market.CancelMarketRequest;
import net.skycomposer.moviebets.common.dto.market.CloseMarketRequest;
import net.skycomposer.moviebets.common.dto.market.MarketData;
import net.skycomposer.moviebets.common.dto.market.MarketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.market.service.MarketService;
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

    @PostMapping("/close")
    public MarketResponse close(@RequestBody @Valid CloseMarketRequest request) {
        return marketService.close(request);
    }

    @PostMapping("/cancel")
    public MarketResponse cancel(@RequestBody @Valid CancelMarketRequest request) {
        return marketService.cancel(request);
    }
}
