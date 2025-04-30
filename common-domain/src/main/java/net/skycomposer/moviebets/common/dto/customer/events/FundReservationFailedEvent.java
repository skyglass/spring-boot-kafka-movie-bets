package net.skycomposer.moviebets.common.dto.customer.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundReservationFailedEvent {
    private String customerId;
    private BigDecimal funds;
    private BigDecimal currentBalance;
}
