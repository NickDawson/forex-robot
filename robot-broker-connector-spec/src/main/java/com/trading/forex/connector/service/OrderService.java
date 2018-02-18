package com.trading.forex.connector.service;


import com.trading.forex.common.model.Symbol;
import com.trading.forex.connector.model.OrderCreateResponse;


public interface OrderService {


    OrderCreateResponse requestOrder(Symbol symbol, Long unit, Double stopLoss, Double takeProfit);
}