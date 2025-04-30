package net.skycomposer.moviebets.customer.exception;

import lombok.Getter;

@Getter
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerId) {
        super(String.format("Couldn't find customer %s", customerId));
    }

}


