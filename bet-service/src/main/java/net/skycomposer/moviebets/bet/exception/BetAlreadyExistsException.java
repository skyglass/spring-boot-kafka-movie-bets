package net.skycomposer.moviebets.bet.exception;

public class BetAlreadyExistsException extends RuntimeException {

    public BetAlreadyExistsException(String customerId, String marketName) {
        super(String.format("Duplicate Bet Request 13: Only one bet is allowed for customer '%s' and event '%s'", customerId, marketName));
    }
}
