package net.skycomposer.moviebets.bet.exception;

import java.util.UUID;

public class BetNotFoundException extends RuntimeException {

    public BetNotFoundException(UUID betId) {
        super(String.format("Couldn't find bet %s", betId));
    }

}


