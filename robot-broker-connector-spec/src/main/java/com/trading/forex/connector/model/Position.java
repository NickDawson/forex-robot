package com.trading.forex.connector.model;

import com.trading.forex.common.model.Symbol;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Position {

    private Symbol symbol;
    private Double pl;
    private Double unrealizedPL;
    private Double resettablePL;
    private Double commission;
    private Double longValue;
    private Double shortValue;
}
