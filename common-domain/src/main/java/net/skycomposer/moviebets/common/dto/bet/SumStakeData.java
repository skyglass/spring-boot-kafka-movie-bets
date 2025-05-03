package net.skycomposer.moviebets.common.dto.bet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SumStakeData {
    private Long total;
    private Long votes;
    private MarketResult result;
}
