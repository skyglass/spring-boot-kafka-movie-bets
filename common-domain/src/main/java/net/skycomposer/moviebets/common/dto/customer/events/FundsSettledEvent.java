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
public class FundsSettledEvent {
    private UUID betId;
    private String customerId;
    private UUID marketId;
    private BigDecimal funds;
    private BigDecimal currentBalance;
}