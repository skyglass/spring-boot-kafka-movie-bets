package net.skycomposer.betting.market.service;

import jakarta.validation.Valid;
import net.skycomposer.betting.common.domain.dto.customer.Customer;
import net.skycomposer.betting.common.domain.dto.customer.WalletData;
import net.skycomposer.betting.common.domain.dto.customer.WalletResponse;
import net.skycomposer.betting.common.domain.dto.market.CancelMarketRequest;
import net.skycomposer.betting.common.domain.dto.market.CloseMarketRequest;
import net.skycomposer.betting.common.domain.dto.market.MarketData;
import net.skycomposer.betting.common.domain.dto.market.MarketResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface MarketService {

    List<MarketData> findAll();

    MarketData findMarketById(UUID marketId);

    MarketResponse open(MarketData marketData);

    MarketResponse update(MarketData marketData);

    MarketResponse close(CloseMarketRequest request);

    MarketResponse cancel(CancelMarketRequest request);

}
