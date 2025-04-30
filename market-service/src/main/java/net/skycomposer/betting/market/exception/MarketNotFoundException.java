package net.skycomposer.betting.market.exception;

import lombok.Getter;

import java.util.UUID;

@Getter
public class MarketNotFoundException extends RuntimeException {

    public MarketNotFoundException(UUID marketId) {
        super(String.format("Couldn't find market %s", marketId));
    }

}


