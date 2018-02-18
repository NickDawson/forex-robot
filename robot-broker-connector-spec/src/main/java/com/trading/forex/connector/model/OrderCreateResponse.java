package com.trading.forex.connector.model;

import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.model.Way;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderCreateResponse {

    private String tradeID;
    private Symbol symbol;
    private Double price;
    private Double takeProfit;
    private Double stopLoss;
    private Way way;

}


