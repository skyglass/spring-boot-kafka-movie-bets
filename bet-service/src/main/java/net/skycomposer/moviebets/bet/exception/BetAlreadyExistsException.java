package net.skycomposer.moviebets.bet.exception;

import java.util.UUID;

public class BetAlreadyExistsException extends RuntimeException {

    public BetAlreadyExistsException(String customerId, UUID marketId) {
        super(String.format("Duplicate bet request for the same customer = %s and marketId = %s", customerId, marketId));
    }
}
