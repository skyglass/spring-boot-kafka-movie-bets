package net.skycomposer.moviebets.market.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Table(name = "market_close_check")
@Entity
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketCloseCheckEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    public static final Integer MARKET_CLOSE_CHECK_ID = 1;

    @Column(name = "last_check_at", nullable = false)
    private Instant lastCheckAt;

    @Column(name = "check_id", nullable = false)
    private Integer checkId;

    @PrePersist
    public void prePersist() {
        this.checkId = MARKET_CLOSE_CHECK_ID;
        this.lastCheckAt = Instant.now();
    }
}
