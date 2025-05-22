package net.skycomposer.moviebets.bet.exception;

import java.util.UUID;

public class MarketIsClosedException extends RuntimeException {

    public MarketIsClosedException(String marketName, UUID marketId) {
        super(String.format("Submission Failed: Market '%s' (id = %s) is already closed", marketName, marketId));
    }

}
