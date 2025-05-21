package net.skycomposer.moviebets.bet.exception;

public class BetCloseDeniedException extends RuntimeException {

    public BetCloseDeniedException(String authenticatedCustomerId, String betCustomerId) {
        super(String.format("Authenticated customer %s can't cancel bets for other customer %s", authenticatedCustomerId, betCustomerId));
    }
}
