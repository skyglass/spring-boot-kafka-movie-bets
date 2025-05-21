package net.skycomposer.moviebets.bet.exception;

import java.util.UUID;

public class MarketIsClosedException extends RuntimeException {

    public MarketIsClosedException(UUID marketId) {
        super(String.format("Market %s is already closed: Permission Denied", marketId));
    }

}
