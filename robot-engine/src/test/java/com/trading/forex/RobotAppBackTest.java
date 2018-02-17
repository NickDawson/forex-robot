package com.trading.forex;

import com.google.gson.Gson;
import com.trading.forex.common.model.Candle;
import com.trading.forex.common.model.CandlestickGranularity;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.model.Way;
import com.trading.forex.common.utils.CustomList;
import com.trading.forex.configuration.AuthorizationServerConfig;
import com.trading.forex.configuration.ResourceServerConfig;
import com.trading.forex.configuration.SecurityConfig;
import com.trading.forex.connector.service.InstrumentService;
import com.trading.forex.entity.EconomicCalendar;
import com.trading.forex.entity.TradeHistory;
import com.trading.forex.model.CompositeTrade;
import com.trading.forex.model.Importance;
import com.trading.forex.model.Trade;
import com.trading.forex.repository.EconomicCalendarRepository;
import com.trading.forex.repository.TradeHistoryRepository;
import com.trading.forex.service.impl.BalanceServiceImpl;
import com.trading.forex.strategies.IntradayStrategy;
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

import static com.trading.forex.common.utils.AlgoUtils.getFromDate;
import static com.trading.forex.common.utils.AlgoUtils.toPip;

/**
 * Created by hsouidi on 10/19/2017.
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.trading.forex.strategies", "com.trading.forex.configuration","com.trading.forex.oanda"}
        , excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {
                BalanceServiceImpl.class
                , SecurityConfig.class, AuthorizationServerConfig.class, ResourceServerConfig.class})})

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



    @Bean
    public InstrumentService instrumentService() {
        return new InstrumentServiceDBImpl();
    }

    public static void main(String[] args) throws IOException {

        application = SpringApplication.run(RobotAppBackTest.class, args);

       //List<DayResult> globalResult = backTestExecWeek(2016, 4, 3);


        int year = 2018;
        int month = 2;
        int day = 16;

      List<DayResult> globalResult = Arrays.asList(backTestExecDay(year, month, day));

        for (DayResult dayResult : globalResult) {
            List<Result> result=dayResult.results;
            log.info("Total:" + result.size());
            log.info("Result:" + result.stream().mapToDouble(result1 -> result1.pip()).sum());
            log.info("Porcentage Position Win:" + (new Double(result.stream().filter(result1 -> result1.getResult().compareTo(Double.MIN_VALUE) > 0).count()) / new Double(result.size())) * 100D + "%");

            dayResult.economicCalendars.stream().forEach(dayRslt ->
                log.info(new StringBuilder()
                        .append("Event=").append(dayRslt.getEconomicCalendarID().getEvent())
                        .append(", Importance=").append(dayRslt.getImportance())
                        .append(", Date=").append(DATE_FORMAT.format(dayRslt.getEconomicCalendarID().getEventDate()))
                        .append(", Currency=").append(dayRslt.getCurrency())
                        .append(", Previous=").append(dayRslt.getPrevious())
                        .append(", Actual=").append(dayRslt.getActual())
                        .append(", Forecast=").append(dayRslt.getForecast())

                        .toString())
            );
            for (Result res : result) {
                log.info("Result: Symbol: " + res.getSymbol() + " status: " + res.status() + " Pip=" + res.pip() + " ,Way =" + res.getWay() + " ,Begin =" + DATE_FORMAT.format(res.begin) + " End=" + DATE_FORMAT.format(res.end) + ", Comment = " + res.getComment() + " result=" + res.getResult());
            }
        }

        final Map<String, Double> previous = getPreviousBackTest();
        final Map<String, Double> current = globalResult.stream().filter(p -> !p.results.isEmpty())
                .collect(Collectors.toMap(p -> DATE_WT_FORMAT.format(p.results.get(0).getBegin()), result -> result.results.stream().mapToDouble(result1 -> result1.pip()).sum()));
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
        previous.putAll(current);
        Files.write(Paths.get(PATH_FILE), new Gson().toJson(previous).getBytes());
        printRealTradeHistories(year, month, day);

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
        List<TradeHistory> tradeHistories = application.getBean(TradeHistoryRepository.class).findByTradeDateBetween(begin, end);
        log.info("Real : Total Pip " + tradeHistories.parallelStream().mapToDouble(p -> p.getPip()).sum());
        log.info("Porcentage Position Win:" + (new Double(tradeHistories.stream().filter(result1 -> result1.getPip().compareTo(Double.MIN_VALUE) > 0).count()) / new Double(tradeHistories.size())) * 100D + "%");
        tradeHistories.stream().forEach(
                tradeHistory ->
                        log.info("Real : Symbol: " + tradeHistory.getSymbol() + " Pip=" + tradeHistory.getPip() + " Result=" + tradeHistory.getResult() + ",Way =" + tradeHistory.getWay() + " ,Begin =" + DATE_FORMAT.format(tradeHistory.getTradeDate()) + " End=" + DATE_FORMAT.format(tradeHistory.getEndTime())));

    }

    @Data
    @AllArgsConstructor
    public static class DayResult {
        private List<Result> results;
        private List<EconomicCalendar> economicCalendars;
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
            return toPip(symbol, result);
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


    private static DayResult backTestExecDay(int year, int month, int day) {
        return backTestExecDay(allDates(year, month, day), new ArrayList<>());
    }

    private static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static DayResult backTestExecDay(List<LocalDateTime> localDateTimes, List<Result> result) {
        Map<Symbol, TradeBackTest> openedPosition = new ConcurrentHashMap<>();
        Map<Symbol, Double> symbolLastPrice = new ConcurrentHashMap<>();
        CandlestickGranularity candlestickGranularity = CandlestickGranularity.M5;
        Date dateGlobal = null;
        for (LocalDateTime localDateTime : localDateTimes) {
            dateGlobal = toDate(localDateTime);
            symbols.parallelStream().forEach(symbol -> {
                Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                CustomList<Candle> candle = application.getBean(InstrumentServiceDBImpl.class).getPricing(candlestickGranularity, symbol, date, getFromDate(date, candlestickGranularity))
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
        final EconomicCalendarRepository economicCalendarRepository = application.getBean(EconomicCalendarRepository.class);
        Collections.sort(result,Comparator.comparing(o -> o.begin));
        return new DayResult(result, economicCalendarRepository.findAllByEventDateAndImportance(toDate(localDateTimes.get(0)), toDate(localDateTimes.get(localDateTimes.size() - 1)), Arrays.asList(Importance.HIGH, Importance.MEDIUM)));
    }

    private static List<TradeBackTest> filter(List<TradeBackTest> o, Symbol symbol) {
        return o.stream()
                .filter(tradeBackTest -> tradeBackTest.getTrade().getSymbol().equals(symbol))
                .collect(Collectors.toList());
    }


    private static List<DayResult> backTestExecWeek(int year, int month, int day) {

        List<DayResult> globalResult = new ArrayList<>();
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


    private static Candle getPrice(LocalDateTime localDateTime, Symbol symbol) {
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        CustomList<Candle> candle = application.getBean(InstrumentService.class).getPricing(CandlestickGranularity.M5, symbol, date, getFromDate(date, CandlestickGranularity.M5))
                .stream().filter(candle1 -> candle1.date().before(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()))).collect(Collectors.toCollection(CustomList::new));
        return candle.getLast();

    }
}
