package net.skycomposer.moviebets.market.dao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.common.dto.market.MarketStatus;

import java.time.Instant;
import java.util.UUID;

@Table(name = "market")
@Entity
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "item1", nullable = false)
    private String item1;

    @Column(name = "item2", nullable = false)
    private String item2;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MarketStatus status;

    @Column(name = "open", nullable = false)
    private Boolean open;

    @Convert(converter = MarketResultConverter.class)
    @Column(name = "result")
    private MarketResult result;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closes_at", nullable = false)
    private Instant closesAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.open = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

}
