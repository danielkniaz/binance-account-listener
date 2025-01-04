package io.prada.listener.dto;

import io.prada.listener.dto.enums.Action;
import io.prada.listener.dto.enums.OrderType;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Signal {
    private String symbol;
    private Action action;
    private OrderType type;
    private int direction;
    private boolean isIn;
    private boolean isOut;
    private BigDecimal price;
    private BigDecimal relativePrice;
    private BigDecimal risk;
}
