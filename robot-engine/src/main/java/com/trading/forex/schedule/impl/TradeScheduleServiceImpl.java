package com.trading.forex.schedule.impl;

import com.trading.forex.connector.model.BrokerTrade;
import com.trading.forex.connector.model.TradeStatus;
import com.trading.forex.connector.service.TradeService;
import com.trading.forex.entity.TradeHistory;
import com.trading.forex.repository.TradeHistoryRepository;
import com.trading.forex.schedule.TradeScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    private TradeService tradeService;


    @Override
    @Scheduled(fixedDelay = 20000)
    public void runSchedule() {
        List<TradeHistory> tradeHistoryList = tradeHistoryRepository.findByResultIsNull();
        if (!tradeHistoryList.isEmpty()) {
            List<String> tradeIds = tradeHistoryList.stream()
                    .map(tradeHistory -> tradeHistory.getTradeId()).collect(Collectors.toList());
            try {
                final Map<String, BrokerTrade> tradeMap = tradeService.getTradesByIds(tradeIds).stream()
                        .collect(Collectors.toMap(o -> o.getTradeID(), Function.identity()));
                tradeHistoryList.stream().forEach(tradeHistory -> {
                    final BrokerTrade trade = tradeMap.get(tradeHistory.getTradeId());
                    if (trade != null && TradeStatus.CLOSED.equals(trade.getStatus())) {
                        tradeHistory.setResult(trade.getResult());
                        tradeHistory.setEndTime(trade.getEndTime());
                        tradeHistory.setPip(tradeHistory.getWay().getValue() * trade.getPip());
                        tradeHistoryRepository.save(tradeHistory);
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


}
