package net.skycomposer.betting.market.api;

import net.skycomposer.betting.common.domain.dto.market.CancelMarketRequest;
import net.skycomposer.betting.common.domain.dto.market.CloseMarketRequest;
import net.skycomposer.betting.common.domain.dto.market.MarketData;
import net.skycomposer.betting.common.domain.dto.market.MarketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MarketController {

    @GetMapping("/get-state/{marketId}")
    public MarketData getState(@PathVariable String marketId) {
      //TODO
      return null;
    }

    @GetMapping("/all")
    public List<MarketData> getAllMarkets() {
      log.info("Fetching all markets");
      //TODO
      return null;
    }

    @PostMapping("/open")
    @ResponseStatus(HttpStatus.CREATED)
    public MarketResponse open(@RequestBody @Valid MarketData marketData) {
      log.info("Open new market {}", marketData);
      //TODO
      return null;
    }

    @PostMapping("/update")
    public MarketResponse update(@RequestBody @Valid MarketData marketData) {
      log.info("Update market {}", marketData);
      //TODO
      return null;
    }

    @PostMapping("/close")
    public MarketResponse close(@RequestBody @Valid CloseMarketRequest request) {
      log.info("Close market {}", request);
      //TODO
      return null;
    }

    @PostMapping("/cancel")
    public MarketResponse cancel(@RequestBody @Valid CancelMarketRequest request) {
      log.info("Cancel market {}", request);
      //TODO
      return null;
    }
}
