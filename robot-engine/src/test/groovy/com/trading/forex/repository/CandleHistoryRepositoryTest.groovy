package com.trading.forex.repository

import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.RobotApp
import Symbol;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.context.junit4.SpringRunner
import spock.lang.Specification;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@SpringBootTest(
        classes = RobotApp.class)
@SqlGroup([
        @Sql(scripts = "classpath:sql/candle-insert.sql", executionPhase = BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/candle-rollback.sql", executionPhase = AFTER_TEST_METHOD)
])
class CandleHistoryRepositoryTest extends Specification{

    @Autowired
    private CandleHistoryRepository candleHistoryRepository;

    def "GetCandlesHistory"() {

        given:
        def symbol = Symbol.EUR_USD
        def candlestickGranularity= CandlestickGranularity.M5
        def epochFrom=1515064500000
        def epochTo=1515084900000
        when: 'Find pricing data for specific symbol and date  '
        def result=candleHistoryRepository.findAllByKeyEpochBetweenAndKeySymbolAndKeyCandlestickGranularityOrderByKeyEpoch(epochFrom,epochTo,symbol,candlestickGranularity)

        then: 'expect result'
        result.size()==69
        noExceptionThrown()
    }

}