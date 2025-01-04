package io.prada.listener.repository.model;

import io.prada.listener.dto.enums.Action;
import io.prada.listener.dto.enums.OrderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Entity
@Table(name = "signals")
@Getter
@Setter @Accessors(chain = true)
@ToString
@NoArgsConstructor
public class SignalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long created;

    @Column
    private String symbol;

    @Column
    @Enumerated(EnumType.STRING)
    private Action action;

    @Column
    @Enumerated(EnumType.STRING)
    private OrderType type;

    @Column
    private int direction;

    @Column
    private boolean isIn;

    @Column
    private boolean isOut;

    @Column
    private BigDecimal price;

    @Column
    private BigDecimal relativePrice;

    @Column
    private BigDecimal risk;

    @PrePersist
    protected void onCreate() {
        this.created = Instant.now().toEpochMilli();
    }
}