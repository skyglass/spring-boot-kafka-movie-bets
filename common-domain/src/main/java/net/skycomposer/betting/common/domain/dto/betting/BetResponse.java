package net.skycomposer.betting.common.domain.dto.betting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetResponse {

    private UUID betId;
    private String message;
}

