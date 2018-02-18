package com.trading.forex.oanda.service;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.trade.TradeListRequest;
import com.oanda.v20.trade.TradeState;
import com.oanda.v20.trade.TradeStateFilter;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.connector.exceptions.ConnectorTechnicalException;
import com.trading.forex.connector.model.BrokerTrade;
import com.trading.forex.connector.model.TradeStatus;
import com.trading.forex.connector.service.TradeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;


@Slf4j
@Service
public class OandaTradeServiceImpl implements TradeService {

    private static String FORMAT = "yyyy-MM-dd'T'hh:mm:ss";

    @Autowired
    public Context context;

    @Autowired
    public AccountID accountID;

    @Override
    public List<BrokerTrade> getTradesByIds(List<String> tradeIds) {

        TradeListRequest tradeListRequest = new TradeListRequest(accountID);
        tradeListRequest.setIds(tradeIds);
        tradeListRequest.setState(TradeStateFilter.ALL);
        try {
            final double marginRate = context.account.get(accountID).getAccount().getMarginRate().doubleValue();
            return context.trade.list(tradeListRequest).getTrades().stream()
                    .map(trade -> BrokerTrade.builder()
                            .tradeID(trade.getId().toString())
                            .result(trade.getRealizedPL().doubleValue())
                            .endTime(trade.getCloseTime()!=null?toDate(trade.getCloseTime()):null)
                            .pip(trade.getAverageClosePrice()!=null?(trade.getAverageClosePrice().doubleValue() - trade.getPrice().doubleValue()) * Math.pow(10, Symbol.fromBrokerValue(trade.getInstrument().toString()).getDecimal() + 1)
                                    * marginRate:null)
                            .status(TradeState.CLOSED.equals(trade.getState())? TradeStatus.CLOSED:TradeStatus.OPEN)
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ConnectorTechnicalException(e);
        }
    }


    private Date toDate(DateTime dateTime) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return simpleDateFormat.parse(dateTime.toString());
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
