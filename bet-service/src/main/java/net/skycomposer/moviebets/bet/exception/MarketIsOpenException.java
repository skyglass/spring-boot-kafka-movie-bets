package net.skycomposer.moviebets.bet.exception;

import java.util.UUID;

public class MarketIsOpenException extends RuntimeException {

    public MarketIsOpenException(UUID marketId) {
        super(String.format("Bet List is not available: Market %s is not closed yet", marketId));
    }

}