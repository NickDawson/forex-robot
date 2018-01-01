package com.trading.forex.strategies;

import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.indicators.impl.ADX;
import com.trading.forex.indicators.impl.ATR;
import com.trading.forex.indicators.impl.Fibonacci;
import com.trading.forex.indicators.impl.PivotPoint;
import com.trading.forex.model.*;
import com.trading.forex.service.InstrumentService;
import com.trading.forex.utils.CustomList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.stream.Collectors;

import static com.trading.forex.utils.AlgoUtils.*;

/**
 * Created by wf on 11/01/2017.
 */
@Slf4j
@Service
public class IntradayHedgingStrategy {

    @Autowired
    private InstrumentService instrumentService;


    public CompositeTrade check(Symbol symbol, Date to) {

        CustomList<Candle> candleM1 = instrumentService.getPricing(CandlestickGranularity.M, symbol, to, getFromDate(to, CandlestickGranularity.M))
                .stream().filter(candle -> candle.date().before(to)).collect(Collectors.toCollection(CustomList::new));
        CustomList<Candle> candleD1 = instrumentService.getPricing(CandlestickGranularity.D, symbol, to, getFromDate(to, CandlestickGranularity.D))
                .stream().filter(candle -> candle.date().before(to)).collect(Collectors.toCollection(CustomList::new));
        CustomList<Candle> candleM5 = instrumentService.getPricing(CandlestickGranularity.M5, symbol, to, getFromDate(to, CandlestickGranularity.M5))
                .stream().filter(candle -> candle.date().before(to)).collect(Collectors.toCollection(CustomList::new));
        CustomList<Candle> candleM15 = instrumentService.getPricing(CandlestickGranularity.M15, symbol, to, getFromDate(to, CandlestickGranularity.M15))
                .stream().filter(candle -> candle.date().before(to)).collect(Collectors.toCollection(CustomList::new));
        CustomList<Candle> candleH1 = instrumentService.getPricing(CandlestickGranularity.H1, symbol, to, getFromDate(to, CandlestickGranularity.H1))
                .stream().filter(candle -> candle.date().before(to)).collect(Collectors.toCollection(CustomList::new));
        return check(symbol, candleM1, candleD1, candleM5,
                candleM15, candleH1);
    }


    private CompositeTrade check(Symbol symbol, CustomList<Candle> candleM1, CustomList<Candle> candleD1, CustomList<Candle> candleM5,
                        CustomList<Candle> candleM15, CustomList<Candle> candleH1) {

        return strategy5(symbol, candleD1, candleM5, candleH1);
    }

    private CompositeTrade buildTrade(String comment, Symbol symbol, Way way, double atr, double close,double closehedge, FibonacciResult fibonacciResultH1) {
        Double defaultStopLoss = close - way.getValue() * atr;
        Double defaultTakeProfit = close + way.getValue() * atr;
        Double profit = Fibonacci.getNextBarrier(defaultTakeProfit, way, fibonacciResultH1);//!= null ? defaultTakeProfit : null;
        Double loss = Fibonacci.getPreviousBarrier(defaultStopLoss, way, fibonacciResultH1);//!= null ? defaultStopLoss : null;
        if (profit == null || loss == null) {
            return null;
        }

        return new CompositeTrade(new Trade(way, symbol
                , normalize(profit, symbol)
                , normalize(loss, symbol)
                , close, comment),new Trade(way.inverse(), Symbol.USD_CHF
                , null
                , null
                , closehedge, comment));
    }


    private CompositeTrade strategy5(Symbol symbol, CustomList<Candle> candleD1, CustomList<Candle> candleM5,CustomList<Candle> candleH1) {
        Candle last = candleM5.getLast();
        Double close = last.getClose();
        Candle jm1 = candleD1.getLastMinus(1);
        double atr = ATR.value(candleM5, 14);
        double atrPip = toPip(symbol, atr);
        Trade trade = null;

        final FibonacciResult fibonacciResultH1 = Fibonacci.values(candleH1, 100);

        final PivotPointResult pivotPointResult = PivotPoint.calcul(jm1);

        double closeHedge = instrumentService.getPricing(CandlestickGranularity.M5, Symbol.USD_CHF, last.date(), getFromDate( last.date(), CandlestickGranularity.M5))
                .stream().filter(candle -> candle.date().before( last.date())).collect(Collectors.toCollection(CustomList::new)).getLast().getClose();


        if (atrPip < 3D) {
            return null;
        }
        if (trade == null
                //&& adx > 25
                ) {

            if (pivotPointResult.getPivot() < close) {
                return buildTrade("pivotPointResult.getPivot()<close " + pivotPointResult.getPivot() + ">" + close, symbol, Way.PUT, atr, close,closeHedge,fibonacciResultH1);

            } else if(pivotPointResult.getPivot()>close){
                return  buildTrade("pivotPointResult.getPivot()>close " + pivotPointResult.getPivot() + ">" + close, symbol, Way.CALL, atr, close,closeHedge,fibonacciResultH1);

            }
        }
       return null;
    }


}

