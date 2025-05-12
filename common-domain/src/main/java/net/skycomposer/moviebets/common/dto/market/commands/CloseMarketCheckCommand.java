package net.skycomposer.moviebets.common.dto.market.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloseMarketCheckCommand {
    private Integer checkId;
}
