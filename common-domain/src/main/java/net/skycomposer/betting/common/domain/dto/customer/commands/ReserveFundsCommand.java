package net.skycomposer.betting.common.domain.dto.customer.commands;

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
public class ReserveFundsCommand {
    private String customerId;
    private String requestId;
    private BigDecimal funds;
}
