package net.skycomposer.moviebets.common.dto.bet.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectBetCommand {
    private UUID betId;
    private String customerId;
    private String reason;
}
