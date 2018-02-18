package com.trading.forex.connector.service;

import com.trading.forex.connector.model.BrokerTrade;

import java.util.List;

public interface TradeService {

    List<BrokerTrade> getTradesByIds(List<String> tradeIds);
}
