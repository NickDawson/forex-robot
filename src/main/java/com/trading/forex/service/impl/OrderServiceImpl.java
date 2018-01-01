package com.trading.forex.service.impl;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.transaction.StopLossDetails;
import com.oanda.v20.transaction.TakeProfitDetails;
import com.trading.forex.model.Symbol;
import com.trading.forex.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.trading.forex.utils.AlgoUtils.normalize;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    @Autowired
    private Context context;

    @Autowired
    private AccountID accountID;

    @Override
    public OrderCreateResponse requestOrder(Symbol symbol, Long unit, Double stopLoss, Double takeProfit) throws ExecuteException, RequestException {
        OrderCreateRequest orderCreateRequest = new OrderCreateRequest(accountID);
        MarketOrderRequest marketorderrequest = new MarketOrderRequest();
        if (stopLoss != null) {
            final StopLossDetails stopLossDetails = new StopLossDetails();
            final double normalizeStoppLoss = normalize(stopLoss, symbol);
            stopLossDetails.setPrice(normalizeStoppLoss);
            marketorderrequest.setStopLossOnFill(stopLossDetails);
        }
        if (takeProfit != null) {
            final TakeProfitDetails takeProfitDetails = new TakeProfitDetails();
            double normalizeTakeProfit = normalize(takeProfit, symbol);
            takeProfitDetails.setPrice(normalizeTakeProfit);
            marketorderrequest.setTakeProfitOnFill(takeProfitDetails);
        }
        // Populate the body parameter fields
        marketorderrequest.setInstrument(symbol.getBrokerValue());
        marketorderrequest.setUnits(unit);


        // Attach the body parameter to the request
        orderCreateRequest.setOrder(marketorderrequest);
        // Execute the request and obtain the response object
        log.info(" Order  symbol {} unit {} , stop loss {} , takeProfit {}", symbol, unit, marketorderrequest.getStopLossOnFill(), marketorderrequest.getTakeProfitOnFill());
        return context.order.create(orderCreateRequest);
    }
}
