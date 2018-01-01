package com.trading.forex.service;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.order.OrderCreateResponse;
import com.trading.forex.model.Symbol;

public interface OrderService {


    OrderCreateResponse requestOrder(Symbol symbol, Long unit, Double stopLoss, Double takeProfit) throws ExecuteException, RequestException;
}