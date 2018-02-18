package com.trading.forex.schedule.impl;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.trade.Trade;
import com.oanda.v20.trade.TradeListRequest;
import com.oanda.v20.trade.TradeState;
import com.oanda.v20.trade.TradeStateFilter;
import com.trading.forex.entity.TradeHistory;
import com.trading.forex.repository.TradeHistoryRepository;
import com.trading.forex.schedule.TradeScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by hsouidi on 11/09/2017.
 */
@Service
@Slf4j
public class TradeScheduleServiceImpl implements TradeScheduleService {


    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    @Autowired
    private Context context;

    @Autowired
    private AccountID accountID;

    private static String FORMAT = "yyyy-MM-dd'T'hh:mm:ss";

    @Override
    @Scheduled(fixedDelay = 20000)
    public void runSchedule() {
        List<TradeHistory> tradeHistoryList = tradeHistoryRepository.findByResultIsNull();
        if (!tradeHistoryList.isEmpty()) {
            List<String> tradeIds = tradeHistoryList.stream()
                    .map(tradeHistory -> tradeHistory.getTradeId()).collect(Collectors.toList());
            TradeListRequest tradeListRequest = new TradeListRequest(accountID);
            tradeListRequest.setIds(tradeIds);
            tradeListRequest.setState(TradeStateFilter.ALL);
            try {
                final double marginRate = context.account.get(accountID).getAccount().getMarginRate().doubleValue();
                Map<String, Trade> tradeMap = context.trade.list(tradeListRequest).getTrades().stream()
                        .collect(Collectors.toMap(o -> o.getId().toString(), Function.identity()));
                tradeHistoryList.stream().forEach(tradeHistory -> {
                    final Trade trade = tradeMap.get(tradeHistory.getTradeId());
                    if (trade!=null&&TradeState.CLOSED.equals(trade.getState())) {
                        tradeHistory.setResult(trade.getRealizedPL().doubleValue());
                        tradeHistory.setEndTime(toDate(trade.getCloseTime()));
                        tradeHistory.setPip(tradeHistory.getWay().getValue()*
                                ((trade.getAverageClosePrice().doubleValue()-trade.getPrice().doubleValue()) * Math.pow(10, tradeHistory.getSymbol().getDecimal()+1)
                                        * marginRate));
                        tradeHistoryRepository.save(tradeHistory);
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
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
