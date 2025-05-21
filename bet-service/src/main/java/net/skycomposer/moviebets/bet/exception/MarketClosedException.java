package net.skycomposer.moviebets.bet.exception;

import java.util.UUID;

public class MarketClosedException extends RuntimeException {

    public MarketClosedException(UUID marketId) {
        super(String.format("Market %s is already closed", marketId));
    }

}