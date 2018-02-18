package com.trading.forex.connector.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class BrokerTrade {

    private String tradeID;
    private Double buyPrice;
    private Double takeprofit;
    private Double stopLoss;
    private String orderId;
    private Double result;
    private Double pip;
    private Date endTime;
    private TradeStatus status;
}
