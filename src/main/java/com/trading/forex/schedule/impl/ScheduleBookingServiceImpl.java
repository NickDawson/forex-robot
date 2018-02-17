package com.trading.forex.schedule.impl;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.position.Position;
import com.trading.forex.entity.TradeHistory;
import com.trading.forex.indicators.impl.Ichimoku;
import com.trading.forex.model.*;
import com.trading.forex.repository.TradeHistoryRepository;
import com.trading.forex.schedule.ScheduleBookingService;
import com.trading.forex.service.IndicatorService;
import com.trading.forex.service.InstrumentService;
import com.trading.forex.service.OrderService;
import com.trading.forex.service.PositionService;
import com.trading.forex.strategies.IntradayHedgingStrategy;
import com.trading.forex.strategies.IntradayStrategy;
import com.trading.forex.utils.CustomList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
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
    private IntradayHedgingStrategy intradayHedgingStrategy;

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
            Symbol symbol = Symbol.fromBrokerValue(orderCreateResponse.getOrderFillTransaction().getInstrument().toString());
            InvestingTechIndicator investingTechIndicator = indicatorService.expertDecision(symbol, Duration.FIVE_MIN);
            Ichimoku ichimoku = new Ichimoku(instrumentService.getPricing(CandlestickGranularity.M5, symbol, 300));
            TradeHistory tradeHistory = TradeHistory.fromBookingResponse(orderCreateResponse, null, investingTechIndicator, indicatorService.expertDecision(symbol), ichimoku);
            tradeHistoryRepository.save(tradeHistory);
        });
        while (localDateTimePredicate.test(LocalDateTime.now())) {
            // Wait end process time
        }
    }


    private List<OrderCreateResponse> strategyIntradayHedging(Predicate<LocalDateTime> localDateTimePredicate) throws RequestException, ExecuteException {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime currentTime = localDateTime.minusSeconds(localDateTime.getSecond()).minusNanos(localDateTime.getNano());
        if (runBooking && localDateTimePredicate.test(currentTime)) {
            log.info("Run Booking");
            java.util.Map<Symbol, Position> positionMap = positionService.getOpenedPositions().stream()
                    .collect(Collectors.toMap(o -> Symbol.fromBrokerValue(o.getInstrument().toString()), Function.identity()));
            List<CompositeTrade> investingDataGroups = new CopyOnWriteArrayList<>(Arrays.asList(Symbol.EUR_USD)).parallelStream()
                    .filter(symbol -> symbol.isActivated())
                    .map(symbol -> intradayHedgingStrategy.check(symbol, Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant())))
                    .filter(result -> result != null)
                    .collect(Collectors.toCollection((CustomList::new)));
            List<OrderCreateResponse> orderCreateResponses = new ArrayList<>();
            for (CompositeTrade investingDataGroup : investingDataGroups) {
                if (positionMap.containsKey(investingDataGroup.getMaster().getSymbol())) {
                    Position position = positionMap.get(investingDataGroup.getMaster().getSymbol());
                    if (!getPositionWay(position).equals(investingDataGroup.getMaster().getWay())) {
                        positionService.closeOpenedPosition();
                    } else {
                        continue;
                    }
                }
                Trade master = investingDataGroup.getMaster();
                Trade hedge = investingDataGroup.getHedge();
                orderCreateResponses.add(orderService.requestOrder(master.getSymbol()
                        , getUnit(unit, master.getWay()).longValue()
                        , master.getStopLoss()
                        , master.getTakeProift()));
                orderCreateResponses.add(orderService.requestOrder(hedge.getSymbol()
                        , getUnit(unit, hedge.getWay()).longValue()
                        , hedge.getStopLoss()
                        , hedge.getTakeProift()));
            }
            return orderCreateResponses;
        } else if (positionService.getProfitOpenedPositions() > 10D) {
            positionService.closeOpenedPosition();
        }
        return new ArrayList<>();
    }


    private List<OrderCreateResponse> strategyIntraday(Predicate<LocalDateTime> localDateTimePredicate) throws RequestException, ExecuteException {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime currentTime = localDateTime.minusSeconds(localDateTime.getSecond()).minusNanos(localDateTime.getNano());
        if (runBooking && localDateTimePredicate.test(currentTime)) {
            log.info("Run Booking");
            java.util.Map<Symbol, Position> positionMap = positionService.getOpenedPositions().stream()
                    .collect(Collectors.toMap(o -> Symbol.fromBrokerValue(o.getInstrument().toString()), Function.identity()));
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
        Double shortUnit = position.getShort().getUnits().doubleValue();
        Double longUnit = position.getLong().getUnits().doubleValue();
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
