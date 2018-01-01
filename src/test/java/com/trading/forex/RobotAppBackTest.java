package com.trading.forex;

import com.google.gson.Gson;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.model.*;
import com.trading.forex.repository.TradeHistoryRepository;
import com.trading.forex.service.InstrumentService;
import com.trading.forex.service.impl.BalanceServiceImpl;
import com.trading.forex.service.impl.InstrumentServiceDBImpl;
import com.trading.forex.service.impl.InstrumentServiceImpl;
import com.trading.forex.strategies.IntradayHedgingStrategy;
import com.trading.forex.strategies.IntradayStrategy;
import com.trading.forex.utils.AlgoUtils;
import com.trading.forex.utils.CustomList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by wf on 10/19/2017.
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.trading.forex.strategies", "com.trading.forex.configuration"}
        , excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {
                BalanceServiceImpl.class, InstrumentServiceImpl.class})})

@Slf4j
public class RobotAppBackTest {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static SimpleDateFormat DATE_WT_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static ConfigurableApplicationContext application = null;
    private static final String PATH_FILE = "./backtest.log";

    private static List<Symbol> symbols = Arrays.asList(
            Symbol.EUR_JPY, Symbol.EUR_CHF, Symbol.EUR_USD, Symbol.EUR_GBP,
            Symbol.USD_CAD, Symbol.USD_CHF, Symbol.USD_JPY,
            Symbol.AUD_JPY, Symbol.AUD_USD,
            Symbol.GBP_CHF, Symbol.GBP_JPY,
            Symbol.NZD_USD);


    //static List<Symbol> symbols = Arrays.asList(Symbol.EUR_CHF);

    public static void main(String[] args) throws IOException {

        application = SpringApplication.run(RobotAppBackTest.class, args);
        //List<List<Result>> globalResult = backTestExecWeek(2017, 12, 24);


        int year = 2018, month = 1, day = 4;
        printRealTradeHistories(year, month, day);
         List<List<Result>> globalResult = Arrays.asList(backTestExecDay(year, month, day).stream().sorted(Comparator.comparing(o -> o.begin)).collect(Collectors.toList()));

        for (List<Result> result : globalResult) {
            log.info("Total:" + result.size());
            log.info("Result:" + result.stream().mapToDouble(result1 -> result1.pip()).sum());
            log.info("Porcentage Position Win:" + (new Double(result.stream().filter(result1 -> result1.getResult().compareTo(Double.MIN_VALUE) > 0).count()) / new Double(result.size())) * 100D + "%");
            for (Result res : result) {
                log.info("Result: Symbol: " + res.getSymbol() + " status: " + res.status() + " Pip=" + res.pip() + " ,Way =" + res.getWay() + " ,Begin =" + DATE_FORMAT.format(res.begin) + " End=" + DATE_FORMAT.format(res.end) + ", Comment = " + res.getComment() + " result=" + res.getResult());
            }

        }

        final Map<String, Double> previous = getPreviousBackTest();
        final Map<String, Double> current = globalResult.stream().collect(Collectors.toMap(p -> DATE_WT_FORMAT.format(p.get(0).getBegin()), result -> result.stream().mapToDouble(result1 -> result1.pip()).sum()));
        final String resultCheck = current.entrySet().stream().map((k) -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(k.getKey() + "=");
                    if (!previous.containsKey(k.getKey()) || previous.get(k.getKey()).equals(k.getValue())) {
                        stringBuilder.append("EQ");
                    } else if (previous.get(k.getKey()) > k.getValue()) {
                        stringBuilder.append("DOWN");

                    } else if (previous.get(k.getKey()) < k.getValue()) {
                        stringBuilder.append("UP");

                    }
                    return stringBuilder.toString();
                }

        ).collect(Collectors.joining(","));
        log.info(resultCheck);
        Files.write(Paths.get(PATH_FILE), new Gson().toJson(current).getBytes());
        System.exit(0);

    }

    private static Map<String, Double> getPreviousBackTest() throws IOException {
        if (!Files.exists(Paths.get(PATH_FILE))) {
            return new HashMap<>();
        } else {
            return new Gson().fromJson(new String(Files.readAllBytes(Paths.get(PATH_FILE))), Map.class);
        }

    }


    public static List<LocalDateTime> allDates(int year, int month, int day) {
        LocalDateTime dateTime = LocalDateTime.of(year, month, day, 9, 00);
        LocalDateTime endDateTime = LocalDateTime.of(year, month, day, 18, 00);
        List<LocalDateTime> localDateTimes = new ArrayList<>();
        for (; !dateTime.isAfter(endDateTime); dateTime = dateTime.plusMinutes(5)) {
            localDateTimes.add(dateTime);
        }

        return localDateTimes;
    }

    private static void printRealTradeHistories(int year, int month, int day) {
        List<LocalDateTime> localDateTimes = allDates(year, month, day);
        final Date begin = Date.from(localDateTimes.get(0).atZone(ZoneId.systemDefault()).toInstant());
        final Date end = Date.from(localDateTimes.get(localDateTimes.size() - 1).atZone(ZoneId.systemDefault()).toInstant());
        application.getBean(TradeHistoryRepository.class).findByTradeDateBetween(begin, end).stream().forEach(
                tradeHistory ->
                        log.info("Real : Symbol: " + tradeHistory.getSymbol() + " Pip=" + tradeHistory.getPip() + " Result=" + tradeHistory.getResult() + ",Way =" + tradeHistory.getWay() + " ,Begin =" + DATE_FORMAT.format(tradeHistory.getTradeDate()) + " End=" + DATE_FORMAT.format(tradeHistory.getEndTime())));

    }

    @Data
    @AllArgsConstructor
    public static class Result {
        private Double result;
        private Way way;
        private Date begin;
        private Date end;
        private Symbol symbol;
        private String comment;


        public Double pip() {
            return AlgoUtils.toPip(symbol, result);
        }

        public String status() {
            return result.compareTo(Double.MIN_VALUE) > 0 ? "WIN" : "LOOSE";
        }

    }

    @Data
    @AllArgsConstructor
    public static class TradeBackTest {
        private Trade trade;
        private Date date;

    }

    @Data
    @AllArgsConstructor
    public static class CompositeTradeBackTest {
        private CompositeTrade trade;
        private Date date;

    }


    private static List<Result> backTestExecDay(int year, int month, int day) {
        return backTestExecDay(allDates(year, month, day), new ArrayList<>());
    }

    private static List<Result> backTestExecDay(List<LocalDateTime> localDateTimes, List<Result> result) {
        Map<Symbol, TradeBackTest> openedPosition = new ConcurrentHashMap<>();
        Map<Symbol, Double> symbolLastPrice = new ConcurrentHashMap<>();
        CandlestickGranularity candlestickGranularity = CandlestickGranularity.M5;
        Date dateGlobal = null;
        for (LocalDateTime localDateTime : localDateTimes) {
            dateGlobal = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            symbols.parallelStream().forEach(symbol -> {
                Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                CustomList<Candle> candle = application.getBean(InstrumentService.class).getPricing(candlestickGranularity, symbol, date, AlgoUtils.getFromDate(date, candlestickGranularity))
                        .stream().filter(candle1 -> candle1.date().before(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))).collect(Collectors.toCollection(CustomList::new));
                Candle last = candle.getLast();
                Double currentPrice = last.getClose();
                symbolLastPrice.put(symbol, currentPrice);
                if (openedPosition.containsKey(symbol)) {
                    TradeBackTest position = openedPosition.get(symbol);
                    Trade trade = position.getTrade();
                    double status = trade.status(last);
                    if (status != 0D) {
                        result.add(new Result(trade.currentProfit(status), trade.getWay(), position.getDate(), date, symbol, trade.getComment()));
                        openedPosition.remove(symbol);
                        log.info("close position for " + position);
                    }
                }
                log.info("start for " + date);
                Trade trade = application.getBean(IntradayStrategy.class).check(symbol, date);
                if (trade != null) {
                    trade.checkConsistancy();
                    if (openedPosition.containsKey(symbol)) {
                        TradeBackTest position = openedPosition.get(symbol);
                        // Cancel and replace
                        if (!trade.getWay().equals(position.getTrade().getWay())) {
                            result.add(new Result(position.getTrade().currentProfit(currentPrice), position.getTrade().getWay(), position.getDate(), date, symbol, "origin comment =" + position.getTrade() + " replace=" + trade.getComment() + " Cancel and replace " + position.getTrade().getWay() + " to " + trade.getWay()));
                            openedPosition.remove(symbol);
                            openedPosition.put(symbol, new TradeBackTest(trade, date));
                            log.info("Close and Booking " + trade);
                        }
                    } else {
                        log.info("Booking " + trade);
                        openedPosition.put(symbol, new TradeBackTest(trade, date));
                    }
                }
            });
        }
        // Close positions at EOD
        for (Map.Entry<Symbol, TradeBackTest> position : openedPosition.entrySet()) {
            Double currentPrice = symbolLastPrice.get(position.getKey());
            result.add(new Result(position.getValue().getTrade().currentProfit(currentPrice), position.getValue().getTrade().getWay(), position.getValue().getDate(), dateGlobal, position.getValue().getTrade().getSymbol(), position.getValue().getTrade().getComment() + " Close at end of Day"));
            openedPosition.remove(position.getValue().getTrade().getSymbol());
        }
        return result;
    }

    private static List<TradeBackTest> filter(List<TradeBackTest> o, Symbol symbol) {
        return o.stream()
                .filter(tradeBackTest -> tradeBackTest.getTrade().getSymbol().equals(symbol))
                .collect(Collectors.toList());
    }


    private static List<List<Result>> backTestExecWeek(int year, int month, int day) {

        List<List<Result>> globalResult = new ArrayList<>();
        for (List<LocalDateTime> localDateTimes : allDatesSemaine(year, month, day)) {
            List<Result> result = new ArrayList<>();
            globalResult.add(backTestExecDay(localDateTimes, result));
        }
        return globalResult;
    }

    public static List<List<LocalDateTime>> allDatesSemaine(int year, int month, int day) {
        List<List<LocalDateTime>> lists = new ArrayList<>();
        LocalDate dateTime = LocalDate.of(year, month, day);
        for (int i = 1; i <= 5; i++) {
            LocalDate localDate = dateTime.plusDays(i);
            lists.add(allDates(localDate.getYear(), localDate.getMonth().getValue(), localDate.getDayOfMonth()));
        }

        return lists;

    }


    @Bean
    public InstrumentService instrumentService() {
        return new InstrumentServiceDBImpl();
    }

    private static Candle getPrice(LocalDateTime localDateTime, Symbol symbol) {
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        CustomList<Candle> candle = application.getBean(InstrumentService.class).getPricing(CandlestickGranularity.M5, symbol, date, AlgoUtils.getFromDate(date, CandlestickGranularity.M5))
                .stream().filter(candle1 -> candle1.date().before(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))).collect(Collectors.toCollection(CustomList::new));
        return candle.getLast();

    }

    private static List<Result> backTestExecDayHedging(List<LocalDateTime> localDateTimes, List<Result> result) {
        java.util.Map<Symbol, CompositeTradeBackTest> openedPosition = new ConcurrentHashMap<>();
        java.util.Map<Symbol, Double> symbolLastPrice = new ConcurrentHashMap<>();
        Symbol symbol = Symbol.EUR_USD;
        Symbol symbolHedge = Symbol.USD_CHF;
        Date dateGlobal = null;
        for (LocalDateTime localDateTime : localDateTimes) {
            dateGlobal = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            Candle last = getPrice(localDateTime, symbol);
            Candle lastHedge = getPrice(localDateTime, symbolHedge);
            Double currentPrice = last.getClose();
            Double hedgeCurrentPrice = lastHedge.getClose();
            symbolLastPrice.put(symbol, currentPrice);
            symbolLastPrice.put(symbolHedge, hedgeCurrentPrice);
            if (openedPosition.containsKey(symbol)) {
                CompositeTradeBackTest position = openedPosition.get(symbol);
                Trade trade = position.getTrade().getMaster();
                Trade hedge = position.getTrade().getHedge();
                double status = trade.status(last);
                if (status != 0D) {
                    result.add(new Result(trade.currentProfit(status), trade.getWay(), position.getDate(), date, symbol, trade.getComment()));
                    result.add(new Result(hedge.currentProfit(hedgeCurrentPrice), hedge.getWay(), position.getDate(), date, symbolHedge, hedge.getComment()));

                    openedPosition.remove(symbol);
                    log.info("close position for " + position);
                }
            }
            log.info("start for " + date);
            CompositeTrade trade = application.getBean(IntradayHedgingStrategy.class).check(symbol, date);
            if (trade != null) {
                trade.checkConsistancy();
                if (openedPosition.containsKey(symbol)) {
                    CompositeTradeBackTest position = openedPosition.get(symbol);
                    // Cancel and replace
                    if (!trade.getMaster().getWay().equals(position.getTrade().getMaster().getWay())) {
                        result.add(new Result(position.getTrade().getMaster().currentProfit(currentPrice), position.getTrade().getMaster().getWay(), position.getDate(), date, symbol, "origin comment =" + position.getTrade() + " replace=" + position.getTrade().getMaster().getComment() + " Cancel and replace " + position.getTrade().getMaster().getWay() + " to " + position.getTrade().getMaster().getWay()));
                        result.add(new Result(position.getTrade().getHedge().currentProfit(hedgeCurrentPrice), position.getTrade().getHedge().getWay(), position.getDate(), date, symbolHedge, "origin comment =" + position.getTrade() + " replace=" + trade.getHedge().getComment() + " Cancel and replace " + position.getTrade().getHedge().getWay() + " to " + trade.getHedge().getWay()));
                        openedPosition.remove(symbol);
                        openedPosition.put(symbol, new CompositeTradeBackTest(trade, date));
                        log.info("Close and Booking " + trade);
                    }
                } else {
                    log.info("Booking " + trade);
                    openedPosition.put(symbol, new CompositeTradeBackTest(trade, date));
                }
            }
        }
        // Close positions at EOD
        for (java.util.Map.Entry<Symbol, CompositeTradeBackTest> position : openedPosition.entrySet()) {
            Double currentPrice = symbolLastPrice.get(position.getKey());
            result.add(new Result(position.getValue().getTrade().getMaster().currentProfit(currentPrice), position.getValue().getTrade().getMaster().getWay(), position.getValue().getDate(), dateGlobal, position.getValue().getTrade().getMaster().getSymbol(), position.getValue().getTrade().getMaster().getComment() + " Close at end of Day"));
            result.add(new Result(position.getValue().getTrade().getHedge().currentProfit(symbolLastPrice.get(position.getValue().trade.getHedge().getSymbol())), position.getValue().getTrade().getHedge().getWay(), position.getValue().getDate(), dateGlobal, position.getValue().getTrade().getHedge().getSymbol(), position.getValue().getTrade().getHedge().getComment() + " Close at end of Day"));

            openedPosition.remove(position.getValue().getTrade().getMaster().getSymbol());
        }
        return result;
    }
}
