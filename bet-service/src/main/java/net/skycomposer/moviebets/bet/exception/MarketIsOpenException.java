package net.skycomposer.moviebets.bet.exception;

import java.util.UUID;

public class MarketIsOpenException extends RuntimeException {

    public MarketIsOpenException(UUID marketId) {
        super(String.format("Market %s is not closed yet: Permission Denied", marketId));
    }

}