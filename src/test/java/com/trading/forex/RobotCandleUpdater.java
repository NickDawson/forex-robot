package com.trading.forex;

import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.entity.CandleEntity;
import com.trading.forex.model.Symbol;
import com.trading.forex.repository.CandleHistoryRepository;
import com.trading.forex.service.InstrumentService;
import com.trading.forex.service.impl.InstrumentServiceImpl;
import com.trading.forex.utils.AlgoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.trading.forex.RobotAppBackTest.allDatesSemaine;

/**
 * Created by hsouidi on 12/09/2017.
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.trading.forex.service", "com.trading.forex.configuration"}
        , excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {RobotAppBackTest.class})})
@Slf4j
public class RobotCandleUpdater {

    private static ConfigurableApplicationContext application = null;
    private static List<Symbol> symbols = Arrays.asList(
            Symbol.EUR_JPY, Symbol.EUR_CHF, Symbol.EUR_USD, Symbol.EUR_GBP,
            Symbol.USD_CAD, Symbol.USD_CHF, Symbol.USD_JPY,
            Symbol.AUD_JPY, Symbol.AUD_USD,
            Symbol.GBP_CHF, Symbol.GBP_JPY,
            Symbol.NZD_USD);

    public static void main(String[] args) {

        application = SpringApplication.run(RobotCandleUpdater.class, args);

        saveCandleWeek(2017, 12, 25);
        saveCandle(RobotAppBackTest.allDates(2017, 12, 25));
        saveCandle(RobotAppBackTest.allDates(2017, 12, 29));


    }


    private static void saveCandleWeek(int year, int month, int day) {

        for (List<LocalDateTime> localDateTimes : allDatesSemaine(year, month, day)) {
            saveCandle(localDateTimes);
        }
    }


    private static void saveCandle(List<LocalDateTime> localDateTimes) {
        for (LocalDateTime localDateTime : localDateTimes) {
            Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            for (Symbol symbol : symbols) {
                save(CandlestickGranularity.M5, date, symbol);
                save(CandlestickGranularity.M15, date, symbol);
                save(CandlestickGranularity.H1, date, symbol);
                save(CandlestickGranularity.D, date, symbol);
                save(CandlestickGranularity.M, date, symbol);
                log.info("En Symbol " + symbol + " Date=" + date);
            }
        }
        log.info("End ALL");
    }

    private static void save(CandlestickGranularity candlestickGranularity, Date date, Symbol symbol) {
        application.getBean(CandleHistoryRepository.class).save(((InstrumentService) application.getBean("instrumentServiceImpl")).getPricing(candlestickGranularity, symbol, date, AlgoUtils.getFromDate(date, candlestickGranularity))
                .stream().map(candle1 -> CandleEntity.build(candle1, candlestickGranularity, symbol))
                .collect(Collectors.toList()));

    }


    @Bean
    public InstrumentService instrumentService() {
        return new InstrumentServiceImpl();
    }

}
