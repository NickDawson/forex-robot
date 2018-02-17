package com.trading.forex.schedule.impl;

import com.trading.forex.common.model.CandlestickGranularity;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.model.Way;
import com.trading.forex.common.utils.CustomList;
import com.trading.forex.connector.model.OrderCreateResponse;
import com.trading.forex.connector.model.Position;
import com.trading.forex.connector.service.InstrumentService;
import com.trading.forex.connector.service.OrderService;
import com.trading.forex.connector.service.PositionService;
import com.trading.forex.entity.TradeHistory;
import com.trading.forex.indicators.impl.Ichimoku;
import com.trading.forex.model.Duration;
import com.trading.forex.model.InvestingTechIndicator;
import com.trading.forex.model.Trade;
import com.trading.forex.repository.TradeHistoryRepository;
import com.trading.forex.schedule.ScheduleBookingService;
import com.trading.forex.service.IndicatorService;
import com.trading.forex.strategies.IntradayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by hsouidi on 10/19/2017.
 */
@Service
@Slf4j
public class ScheduleBookingServiceImpl implements ScheduleBookingService {

    @Autowired
    private IndicatorService indicatorService;

    @Autowired
    private PositionService positionService;

    @Autowired
    private InstrumentService instrumentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private IntradayStrategy intradayStrategy;

    @Autowired
    private TradeHistoryRepository tradeHistoryRepository;

    @Value("${env.profile}")
    private String mode;

    private Boolean runBooking = true;

    @Value("${oanda.unit}")
    private Double unit;


    @Override
    @Scheduled(fixedDelay = 5000)
    public void runStrategy() throws Exception {

        Predicate<LocalDateTime> localDateTimePredicate = time -> time.getMinute() % 5 == 0;
        final List<OrderCreateResponse> orderCreateResponses = strategyIntraday(localDateTimePredicate);
        orderCreateResponses.stream().distinct().forEach(orderCreateResponse -> {
            final Symbol symbol = orderCreateResponse.getSymbol();
            InvestingTechIndicator investingTechIndicator = indicatorService.expertDecision(symbol, Duration.FIVE_MIN);
            Ichimoku ichimoku = new Ichimoku(instrumentService.getPricing(CandlestickGranularity.M5, symbol, 300));
            TradeHistory tradeHistory = TradeHistory.fromBookingResponse(orderCreateResponse, null, investingTechIndicator, indicatorService.expertDecision(symbol), ichimoku);
            tradeHistoryRepository.save(tradeHistory);
        });
        while (localDateTimePredicate.test(LocalDateTime.now())) {
            // Wait end process time
        }
    }

    private List<OrderCreateResponse> strategyIntraday(Predicate<LocalDateTime> localDateTimePredicate) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime currentTime = localDateTime.minusSeconds(localDateTime.getSecond()).minusNanos(localDateTime.getNano());
        if (runBooking && localDateTimePredicate.test(currentTime)) {
            log.info("Run Booking");
            java.util.Map<Symbol, Position> positionMap = positionService.getOpenedPositions().stream()
                    .collect(Collectors.toMap(o -> o.getSymbol(), Function.identity()));
            List<Trade> investingDataGroups = new CopyOnWriteArrayList<>(Symbol.values()).parallelStream()
                    .filter(symbol -> symbol.isActivated())
                    .map(symbol -> intradayStrategy.check(symbol, Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant())))
                    .filter(result -> result != null)
                    .collect(Collectors.toCollection((CustomList::new)));
            List<OrderCreateResponse> orderCreateResponses = new ArrayList<>();
            for (Trade investingDataGroup : investingDataGroups) {
                if (positionMap.containsKey(investingDataGroup.getSymbol())) {
                    Position position = positionMap.get(investingDataGroup.getSymbol());
                    if (!getPositionWay(position).equals(investingDataGroup.getWay())) {
                        positionService.closeOpenedPosition(position);
                    } else {
                        continue;
                    }
                }
                log.info("Order Exec for trade " + investingDataGroup.toString());
                orderCreateResponses.add(orderService.requestOrder(investingDataGroup.getSymbol()
                        , getUnit(unit, investingDataGroup.getWay()).longValue()
                        , investingDataGroup.getStopLoss()
                        , investingDataGroup.getTakeProift()));
            }
            return orderCreateResponses;
        }

        return new ArrayList<>();
    }

    private Way getPositionWay(Position position) {
        Double shortUnit = position.getShortValue();
        Double longUnit = position.getLongValue();
        return Math.abs(shortUnit) > Math.abs(longUnit) ? Way.PUT : Way.CALL;
    }

    private Double getUnit(Double unit, Way way) {
        return way.getValue() * unit;
    }

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public double getUnit() {
        return unit;
    }

    @Override
    public void setUnit(double unit) {
        this.unit = unit;
    }


    @Override
    public Boolean getRunBooking() {
        return runBooking;
    }

    @Override
    public void setRunBooking(Boolean runBooking) {
        this.runBooking = runBooking;
    }
}
