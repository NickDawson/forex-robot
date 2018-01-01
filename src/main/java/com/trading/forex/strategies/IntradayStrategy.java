package com.trading.forex.strategies;

import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.indicators.impl.*;
import com.trading.forex.model.*;
import com.trading.forex.service.InstrumentService;
import com.trading.forex.utils.CustomList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.trading.forex.indicators.IndicatorUtils.getValue;
import static com.trading.forex.indicators.impl.MovingAverage.*;
import static com.trading.forex.utils.AlgoUtils.*;

/**
 * Created by wf on 11/01/2017.
 */
@Slf4j
@Service
public class IntradayStrategy {

    @Autowired
    private InstrumentService instrumentService;


    public Trade check(Symbol symbol, Date to) {
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

    private Trade check(Symbol symbol, CustomList<Candle> candleM1, CustomList<Candle> candleD1, CustomList<Candle> candleM5,
                        CustomList<Candle> candleM15, CustomList<Candle> candleH1) {

        return strategy5(symbol, candleM1, candleD1, candleM5, candleM15, candleH1);
    }

    private Trade buildTrade(String comment, Symbol symbol, Way way, double atr, double close, FibonacciResult fibonacciResultM5, FibonacciResult fibonacciResultM15, FibonacciResult fibonacciResultH1, FibonacciResult fibonacciResultD1) {
        Double defaultStopLoss = close - way.getValue() * atr;
        Double defaultTakeProfit = close + way.getValue() * atr;
        Fibonacci.getNextBarrier(defaultTakeProfit, way, fibonacciResultM15);
        Fibonacci.getNextBarrier(defaultTakeProfit, way, fibonacciResultD1);
        Fibonacci.getNextBarrier(defaultTakeProfit, way, fibonacciResultH1);
        Double profit = Fibonacci.getNextBarrier(defaultTakeProfit, way, fibonacciResultM5);//!= null ? defaultTakeProfit : null;
        Double loss = Fibonacci.getPreviousBarrier(defaultStopLoss, way, fibonacciResultM5);//!= null ? defaultStopLoss : null;
        if (profit == null || loss == null) {
            return null;
        }
        return new Trade(way, symbol
                , normalize(profit, symbol)
                , normalize(loss, symbol)
                , close, comment);
    }

    private Trade buildTradeSwing(Double marge, String comment, Symbol symbol, Way way, double close, Swing swingLows, Swing swingsHigh, FibonacciResult fibonacciResultM5) {

        Double defaultProfit = null;
        Double defaultStopLoss = null;
        StringBuilder commentDebug = new StringBuilder();
        if (way == Way.CALL) {
            defaultProfit = swingsHigh.getPrice() == 0 ? close : swingsHigh.getPrice();
            defaultStopLoss = swingLows.getPrice() == 0 ? close : swingLows.getPrice();
        }
        if (way == Way.PUT) {
            defaultStopLoss = swingsHigh.getPrice() == 0 ? close : swingsHigh.getPrice();
            defaultProfit = swingLows.getPrice() == 0 ? close : swingLows.getPrice();
        }
        Double loss = Fibonacci.getPreviousBarrier(defaultStopLoss, way, fibonacciResultM5);
        Double profit = fibonacciResultM5.getNextBarrier(defaultProfit, way, true);

        if (profit == null || toPip(symbol, (profit - close) * way.getValue()) < marge || loss == null || toPip(symbol, (loss - close) * way.getValue()) > -marge) {
            return null;
        }
        commentDebug.append(" ,Swing High=" + swingsHigh.candleDate());
        commentDebug.append(" ,Swing Low=" + swingLows.candleDate());
        commentDebug.append(" ," + fibonacciResultM5.dateBeginEnd());
        return new Trade(way, symbol
                , normalize(profit, symbol)
                , normalize(loss, symbol)
                , close, comment + commentDebug.toString());
    }

    private Swing margeWithSwings(double marge, List<Candle> candles, CustomList<Integer> swings, double currentPrice, Way way) {

        CustomList<Swing> result = new CustomList<>();
        for (int i = swings.size() - 1; i >= 0; i--) {
            Integer indexSwing = swings.get(i);
            final Candle candle = candles.get(indexSwing);
            double barrier = way == Way.PUT && candle.body() < 0 || way == Way.CALL && candle.body() > 0 ? candle.getClose() : candle.getOpen();
            double pip = toPip(candle.getSymbol(), way.getValue() * (barrier - currentPrice));
            if (pip > marge) {
                result.add(new Swing(pip, barrier, candle));

            }
        }
        Collections.sort(result);
        return result.isEmpty() ? new Swing(0D, 0D, null) : result.getFirst();
    }


    private Trade strategy5(Symbol symbol, CustomList<Candle> candleM1, CustomList<Candle> candleD1, CustomList<Candle> candleM5,
                            CustomList<Candle> candleM15, CustomList<Candle> candleH1) {
        Candle last = candleM5.getLast();
        Double close = last.getClose();
        candleM1.getLastMinus(1);
        candleM15.getLastMinus(1);
        double ema100 = valueEma(candleM5, 100);
        double ema300 = valueEma(candleM5, 300);
        double ema600 = valueEma(candleM5, 600);
        double ema1000 = valueEma(candleM5, 1000);
        Trend trend = Trend.getTrend(candleM5, symbol);
        Stoch.StochResult stochResult = Stoch.values(candleM5, 11, 5, 3);
        double stoch = getValue(stochResult.getOutSlowD());
        Candle jm1 = candleD1.getLastMinus(1);
        double atr = ATR.value(candleM5, 14);
        double atrPip = toPip(symbol, atr);
        double[] ema14values = valuesEma(candleM5, 14);
        double ema14 = getValue(ema14values);
        Trade trade = null;
        final FibonacciResult fibonacciResultM5 = Fibonacci.values(candleM5, 100);
        final PivotPointResult pivotPointResult = PivotPoint.calcul(jm1);
        Boolean isEmaCall = close > ema100 && close > ema300 && close > ema600 && close > ema1000 && close > ema14;
        Boolean isEmaPut = close < ema100 && close < ema300 && close < ema600 && close < ema1000 && close < ema14;


        CustomList<Integer> swingLows = HighLows.swingLows(candleM5, symbol);
        CustomList<Integer> swingHigh = HighLows.swingHighs(candleM5, symbol);

        if (atrPip < 5D) {
            return null;
        }
        double marge = atrPip*2.35D;

        if (trade == null) {
            final Swing margeWithSwingLows = margeWithSwings(marge, candleM5, swingLows, close, Way.PUT);
            final Swing margeWithSwingHigh = margeWithSwings(marge, candleM5, swingHigh, close, Way.CALL);

            if (trend == Trend.BULLISH) {
                return buildTradeSwing(marge, trend + " trend==Trend.BULLISH " + close, symbol, Way.CALL, close, margeWithSwingLows, margeWithSwingHigh, fibonacciResultM5);

            } else if (trend == Trend.BEARISH) {
                return buildTradeSwing(marge, trend + " trend==Trend.BEARISH " + close, symbol, Way.PUT, close, margeWithSwingLows, margeWithSwingHigh, fibonacciResultM5);

            }
            if (
                    close < pivotPointResult.getPivot() &&
                            stoch > 15 &&
                            close < ema14 && isEmaPut
                    ) {
                if (margeWithSwingHigh.getPip() < marge || margeWithSwingLows.getPip() < marge) {
                    return null;
                }
                return buildTradeSwing(marge, trend + " pivotPointResult.getPivot()<close " + pivotPointResult.getPivot() + ">" + close, symbol, Way.CALL, close, margeWithSwingLows, margeWithSwingHigh, fibonacciResultM5);
                //  return buildTrade("pivotPointResult.getPivot()<close " + pivotPointResult.getPivot() + ">" + close, symbol, Way.PUT, atr, close, fibonacciResultM5, fibonacciResultM15, fibonacciResultH1, fibonacciResultDay);

            } else if (
                    close > pivotPointResult.getPivot() &&
                            stoch < 85 &&
                            close > ema14 && isEmaCall
                    ) {

                if (margeWithSwingHigh.getPip() < marge || margeWithSwingLows.getPip() < marge) {
                    return null;
                }
                return buildTradeSwing(marge, trend + " pivotPointResult.getPivot()>close " + pivotPointResult.getPivot() + ">" + close, symbol, Way.PUT, close, margeWithSwingLows, margeWithSwingHigh, fibonacciResultM5);
                //return buildTrade("pivotPointResult.getPivot()>close " + pivotPointResult.getPivot() + ">" + close, symbol, Way.CALL, atr, close, fibonacciResultM5, fibonacciResultM15, fibonacciResultH1, fibonacciResultDay);

            }
            if ((isEmaPut && stoch > 15) || (isEmaCall && stoch > 85)) {
                return buildTradeSwing(marge, trend + " EmaPut", symbol, Way.PUT, close, margeWithSwingLows, margeWithSwingHigh, fibonacciResultM5);

            } else if ((isEmaCall && stoch < 85) || (isEmaPut && stoch < 15)) {
                return buildTradeSwing(marge, trend + " EmaCALL", symbol, Way.CALL, close, margeWithSwingLows, margeWithSwingHigh, fibonacciResultM5);

            }

        }
         return null;
    }

    private Trade strategy4(Symbol symbol, CustomList<Candle> candleM1, CustomList<Candle> candleD1, CustomList<Candle> candleM5,
                            CustomList<Candle> candleM15, CustomList<Candle> candleH1) {
        Candle last = candleM5.getLast();
        Double close = last.getClose();
        candleM1.getLastMinus(1);
        Candle jm1 = candleD1.getLastMinus(1);
        double atr = ATR.value(candleM5, 14);
        double atrPip = toPip(symbol, atr);
        double[] ema26values = valuesEma(candleM5, 26);
        double ema26 = getValue(ema26values);
        Integer lastCrossEma26 = getCrossWithPrice(ema26values, candleM5).getLast();
        if (atrPip < 5D) {
            return null;
        }
        if (lastCrossEma26 != null && lastCrossEma26 < 5) {
            return null;

        }

        Trend trend = Trend.getTrend(candleM5, symbol);


        Stoch.StochResult stochResult = Stoch.values(candleM5, 11, 5, 3);
        double stoch = getValue(stochResult.getOutSlowD());
        final FibonacciResult fibonacciResultDay = Fibonacci.values(new CustomList<>(Arrays.asList(jm1)), 100);
        final FibonacciResult fibonacciResultM15 = Fibonacci.values(candleM15, 100);
        final FibonacciResult fibonacciResultH1 = Fibonacci.values(candleH1, 100);
        final FibonacciResult fibonacciResultM5 = Fibonacci.values(candleM5, 100);

        final PivotPointResult pivotPointResult = PivotPoint.calcul(jm1);
        log.info("Symbol " + symbol + "  Pivot:" + pivotPointResult.getPivot());

        double[] ema100Values = valuesEma(candleM5, 100);
        double ema100 = getValue(ema100Values);
        double ema300 = valueEma(candleM5, 300);
        double[] ema600Values = valuesEma(candleM5, 600);
        double ema600 = getValue(ema600Values);
        double ema1000 = valueEma(candleM5, 1000);
        Ichimoku ichimoku5M = new Ichimoku(candleM5);
        final BolingerBands.BBOND bbond = BolingerBands.value(candleM5, 20);
        double positionWithCloud = ichimoku5M.positionWithCloud(last);
        Boolean isEmaCall = close > ema100 && close > ema300 && close > ema600 && close > ema1000 && close > ema26;
        Boolean isEmaPut = close < ema100 && close < ema300 && close < ema600 && close < ema1000 && close < ema26;
        Trade trade = null;
        int bbpos = bbond.positionWithBond(last);
        // Cross ema
        if (trade == null && stoch < 75 && stoch > 25) {
            double[] emaFastValues = valuesEma(candleM5, 9);
            double[] emaSlowValues = valuesEma(candleM5, 21);
            final CustomList<Integer> crossCall = getCrossList(emaFastValues, emaSlowValues, Way.CALL, symbol);
            final CustomList<Integer> crossPut = getCrossList(emaFastValues, emaSlowValues, Way.PUT, symbol);
            final int threshold = 2;
            if (trend == Trend.BULLISH && (!crossCall.isEmpty() && crossPut.isEmpty() && crossCall.getLast() < threshold) ||
                    (!crossCall.isEmpty() && !crossPut.isEmpty() && crossCall.getLast() < crossPut.getLast() && crossCall.getLast() < threshold)) {
                return buildTrade("cross ema5 and 20 & Ema Sup 100,300,600,1000 & stoch < 75&& stoch > 25  ", symbol, Way.CALL, atr, close, fibonacciResultM5, fibonacciResultM15, fibonacciResultH1, fibonacciResultDay);

            } else if (trend == Trend.BEARISH && (!crossPut.isEmpty() && crossCall.isEmpty() && crossPut.getLast() < threshold) ||
                    (!crossPut.isEmpty() && !crossCall.isEmpty() && crossPut.getLast() < crossCall.getLast() && crossPut.getLast() < threshold)) {
                return buildTrade("cross ema5 and 20 & Ema Inf 100,300,600,1000 &stoch < 75&& stoch > 25", symbol, Way.PUT, atr, close, fibonacciResultM5, fibonacciResultM15, fibonacciResultH1, fibonacciResultDay);

            }

        }

        if (trade == null && positionWithCloud > 0 && isEmaCall
                && close > pivotPointResult.getPivot()
                && stoch < 70
            //&& (lastStock - lastMinus1Stock) > 0
                ) {
            return buildTrade("positionWithCloud > 0 & Ema Sup 100,300,600,1000,26 & Sup pivot & stock <75 &(lastStock - previousStock) > 0", symbol, Way.CALL, atr, close, fibonacciResultM5, fibonacciResultM15, fibonacciResultH1, fibonacciResultDay);

        } else if (bbpos > -1 && trade == null && positionWithCloud < 0 && isEmaPut
                && close < pivotPointResult.getPivot()
                && stoch > 30
            //&& (lastStock - lastMinus1Stock) < 0
                ) {
            return buildTrade("positionWithCloud < 0 & Ema Inf 100,300,600,1000,26 & Inf pivot & stock >25 &(lastStock - previousStock)< 0", symbol, Way.PUT, atr, close, fibonacciResultM5, fibonacciResultM15, fibonacciResultH1, fibonacciResultDay);
        }


        if (trade == null) {
            if (isEmaPut && stoch > 80) {
                return buildTrade("stoch > 80 & Ema Sup 100,300,600,1000 ", symbol, Way.PUT, atr, close, fibonacciResultM5, fibonacciResultM15, fibonacciResultH1, fibonacciResultDay);
            } else if (isEmaCall && stoch < 20) {
                return buildTrade("stoch < 20 Ema Sup 100,300,600,1000 ", symbol, Way.CALL, atr, close, fibonacciResultM5, fibonacciResultM15, fibonacciResultH1, fibonacciResultDay);
            }
        }

        return null;
    }

    private Trade strategy3(Symbol symbol, CustomList<Candle> candleM5, CustomList<Candle> candleD1) {
        Stoch.StochResult stochResult = Stoch.values(candleM5, 11, 5, 3);
        int Lastindex = Stoch.getIndex(stochResult.getOutSlowK());
        double lastStock = stochResult.getOutSlowK()[Lastindex];
        double lastMinus1Stock = stochResult.getOutSlowK()[Lastindex - 1];
        Double close = candleM5.getLast().getClose();
        final PivotPointResult pivotPointResult = PivotPoint.calcul(candleD1.getLastMinus(1));
        log.info("Symbol +" + symbol + "  Pivot:" + pivotPointResult.getPivot());
        double ema100 = valueEma(candleM5, 100);
        double ema300 = valueEma(candleM5, 300);
        double[] ema600Values = valuesEma(candleM5, 600);
        double ema600 = getValue(ema600Values);
        double ema1000 = valueEma(candleM5, 1000);
        if (close > ema100 && close > ema300 && close > ema600 && close > ema1000 && close > pivotPointResult.getPivot() && lastMinus1Stock < 20 && lastStock > 20) {
            return new Trade(Way.CALL, symbol
                    , getProfitValue(null, null, symbol, close, Way.CALL)
                    , getStopValue(null, null, symbol, close, Way.CALL)
                    , close, null);
        } else if (close < ema100 && close < ema300 && close < ema600 && close < ema1000 && close < pivotPointResult.getPivot() && lastMinus1Stock > 80 && lastStock < 80) {
            return new Trade(Way.PUT, symbol
                    , getProfitValue(null, null, symbol, close, Way.PUT)
                    , getStopValue(null, null, symbol, close, Way.PUT)
                    , close, null);
        }

        return null;
    }

    private Trade strategy2(Symbol symbol, CustomList<Candle> candleM5, CustomList<Candle> candleM15, CustomList<Candle> candleH1, CustomList<Candle> candleD1, CustomList<Candle> candleM1) {
        List<Ichimoku.TrendLine> trendLines = new CustomList<>();
        List<Ichimoku.TrendLine> kijunTrendLines = new CustomList<>();
        Ichimoku ichimoku5M = new Ichimoku(candleM5);
        Ichimoku ichimoku15M = new Ichimoku(candleM15);
        Ichimoku ichimoku1H = new Ichimoku(candleH1);
        Ichimoku ichimoku1D = new Ichimoku(candleD1);
        Ichimoku ichimoku1M = new Ichimoku(candleM1);
        trendLines.addAll(ichimoku5M.getSpanBLine());
        trendLines.addAll(ichimoku15M.getSpanBLine());
        trendLines.addAll(ichimoku1H.getSpanBLine());
        trendLines.addAll(ichimoku1D.getSpanBLine());
        trendLines.addAll(ichimoku1M.getSpanBLine());
        kijunTrendLines.addAll(ichimoku5M.getKijunSenLine());
        kijunTrendLines.addAll(ichimoku15M.getKijunSenLine());
        kijunTrendLines.addAll(ichimoku1H.getKijunSenLine());
        kijunTrendLines.addAll(ichimoku1D.getKijunSenLine());
        kijunTrendLines.addAll(ichimoku1M.getKijunSenLine());

        Double close = candleM5.getLast().getClose();
        double adx = ADX.value(candleM5, 14);
        MACD.MACDValues macd = MACD.values(candleM5, 12, 26, 9);
        double hist = getValue(macd.getOutMACDHist()) * Math.pow(10, symbol.getDecimal());
        CustomList<Integer> cross = MACD.cross(macd.getOutMACDHist());

        if (!cross.isEmpty() && adx > 40 && cross.getFirst() < 6 && hist > 10 && isSorted(macd.getOutMACDHist(), 3, Way.CALL)) {
            Double profit = getProfitValue(trendLines, kijunTrendLines, symbol, close, Way.CALL);
            Double loss = getStopValue(trendLines, kijunTrendLines, symbol, close, Way.CALL);
            if (profit == null || loss == null) {
                return null;
            }
            return new Trade(Way.CALL, symbol, profit, loss, close, null);
        } else if (!cross.isEmpty() && adx > 40 && cross.getFirst() < 6 && hist < -10 && isSorted(macd.getOutMACDHist(), 3, Way.PUT)) {
            Double profit = getProfitValue(trendLines, kijunTrendLines, symbol, close, Way.PUT);
            Double loss = getStopValue(trendLines, kijunTrendLines, symbol, close, Way.PUT);
            if (profit == null || loss == null) {
                return null;
            }
            return new Trade(Way.PUT, symbol, profit, loss, close, null);

        }
        return null;
    }


    private Trade strategy1(Symbol symbol, CustomList<Candle> candleM5, CustomList<Candle> candleD1) {
        double ema35 = valueSma(candleM5, 35);
        double ema60 = valueSma(candleM5, 60);

        //  point pivot entre 35 et 60
        Double close = candleM5.getLast().getClose();

        final PivotPointResult pivotPointResult = PivotPoint.calcul(candleD1.getLastMinus(1));
        if (close < ema35 && close > ema60) {
            if (isBetween(pivotPointResult.getS1(), ema35, ema60)) {
                return new Trade(Way.CALL, symbol, pivotPointResult.getR1(), pivotPointResult.getS2(), close, null);
            } else if (isBetween(pivotPointResult.getS2(), ema35, ema60)) {
                return new Trade(Way.CALL, symbol, pivotPointResult.getS1(), pivotPointResult.getS3(), close, null);

            } else if (isBetween(pivotPointResult.getS3(), ema35, ema60)) {
                return new Trade(Way.CALL, symbol, pivotPointResult.getS2(), getStopValue(null, null, symbol, close, Way.CALL), close, null);

            } else if (isBetween(pivotPointResult.getR1(), ema35, ema60)) {

                return new Trade(Way.CALL, symbol, pivotPointResult.getR2(), pivotPointResult.getS1(), close, null);

            } else if (isBetween(pivotPointResult.getR2(), ema35, ema60)) {

                return new Trade(Way.CALL, symbol, pivotPointResult.getR3(), pivotPointResult.getR1(), close, null);

            } else if (isBetween(pivotPointResult.getR3(), ema35, ema60)) {

                return new Trade(Way.CALL, symbol, getProfitValue(null, null, symbol, close, Way.CALL), pivotPointResult.getR2(), close, null);

            }

        }
        return null;
    }

    private boolean isBetween(double val, double min, double max) {
        return val > min && val < max;
    }

    private boolean isSorted(double[] input, int NbelementTobeChecked, Way order) {
        List<Double> list = Arrays.stream(input).boxed().collect(Collectors.toList()).subList(input.length - NbelementTobeChecked, input.length);
        boolean sorted = true;

        for (int i = 1; i < list.size(); i++) {
            if ((order.getValue() * list.get(i).compareTo(list.get(i - 1))) >= 0) {
                sorted = true;
            } else {
                return false;

            }
        }

        return sorted;
    }


    private Double getStopValue(List<Ichimoku.TrendLine> ssb, List<Ichimoku.TrendLine> kijun, Symbol symbol, Double price, Way way) {

        CustomList<Double> ssbPrice= ssb.stream()
                .filter(trendLine -> way.getValue() * (trendLine.getPrice() - price)*Math.pow(10,symbol.getDecimal()) < -10)                .sorted((o1, o2) -> -1 * (way.getValue()) * o1.getPrice().compareTo(o2.getPrice()))
                .map(trendLine -> trendLine.getPrice())
                .collect(Collectors.toCollection(CustomList::new));
        if(!ssbPrice.isEmpty()){
            return ssbPrice.getFirst();
        }

        CustomList<Double> kijunPrice= kijun.stream()
                .filter(trendLine -> way.getValue() * (trendLine.getPrice() - price)*Math.pow(10,symbol.getDecimal()) < -10)
                .sorted((o1, o2) -> -1 * (way.getValue()) * o1.getPrice().compareTo(o2.getPrice()))
                .map(trendLine -> trendLine.getPrice())
                .collect(Collectors.toCollection(CustomList::new));
        if(!kijunPrice.isEmpty()){
            return kijunPrice.getFirst();
        }

        return null;

    }


    private Double getStopValue(FibonacciResult fibonacciResult) {
        return null;
    }

    private Double getProfitValue(List<Ichimoku.TrendLine> ssb, List<Ichimoku.TrendLine> kijun, Symbol symbol, Double price, Way way) {
        CustomList<Double> ssbPrice= ssb.stream()
                .filter(trendLine -> way.getValue() * (trendLine.getPrice() - price)*Math.pow(10,symbol.getDecimal()) > 10)
                .sorted((o1, o2) -> (way.getValue()) * o1.getPrice().compareTo(o2.getPrice()))
                .map(trendLine -> trendLine.getPrice())
                .collect(Collectors.toCollection(CustomList::new));
        if(!ssbPrice.isEmpty()){
            return ssbPrice.getFirst();
        }

        CustomList<Double> kijunPrice= kijun.stream()
                .filter(trendLine -> way.getValue() * (trendLine.getPrice() - price)*Math.pow(10,symbol.getDecimal()) > 10)
                .sorted((o1, o2) -> (way.getValue()) * o1.getPrice().compareTo(o2.getPrice()))
                .map(trendLine -> trendLine.getPrice())
                .collect(Collectors.toCollection(CustomList::new));
        if(!kijunPrice.isEmpty()){
            return kijunPrice.getFirst();
        }

        return null;

    }

    private boolean check(Way way, Ichimoku ichimoku, Symbol symbol) {

        return way.getValue() * (ichimoku.getKumo().getLast() * Math.pow(10, symbol.getDecimal())) > 10
                && way.getValue() * (ichimoku.getSpanB().getLast() - ichimoku.getSpanB().getLastMinus(5)) > 0
                && way.getValue() * (ichimoku.getSpanA().getLast() - ichimoku.getSpanA().getLastMinus(5)) > 0
                && (ichimoku.getTwist().getLast() == null || ichimoku.getTwist().getLast() > 10);
    }


}

