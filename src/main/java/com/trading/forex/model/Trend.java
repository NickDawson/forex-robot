package com.trading.forex.model;

import com.trading.forex.utils.CustomList;

import static com.trading.forex.indicators.impl.MovingAverage.valuesEma;
import static com.trading.forex.utils.AlgoUtils.*;

public enum Trend {

    BULLISH, BEARISH, RANGE;

    public static Trend getTrend(CustomList<Candle> candles, Symbol symbol) {

        double threshold = 0.1;
        Candle last = candles.getLast();
        Candle lastMinus3 = candles.getLastMinus(3);
        final CustomList<Double> ema5 = toList(valuesEma(candles, 5));
        final CustomList<Double> ema26 = toList(valuesEma(candles, 26));
        final CustomList<Double> ema100 = toList(valuesEma(candles, 100));
        final CustomList<Double> ema300 = toList(valuesEma(candles, 300));
        final CustomList<Double> ema600 = toList(valuesEma(candles, 600));
        final CustomList<Double> ema1000 = toList(valuesEma(candles, 1000));

        double diffEma5 = toPip(symbol, (ema5.getLast() - ema5.getLastMinus(5)));
        double diffEma26 = toPip(symbol, (ema26.getLast() - ema26.getLastMinus(10)));
        double diffEma100 = toPip(symbol, (ema100.getLast() - ema100.getLastMinus(10)));
        double diffEma300 = toPip(symbol, (ema300.getLast() - ema300.getLastMinus(10)));
        double diffEma600 = toPip(symbol, (ema600.getLast() - ema600.getLastMinus(10)));
        double diffEma1000 = toPip(symbol, (ema1000.getLast() - ema1000.getLastMinus(10)));


        if (isCrossPrice(last, ema26.getLast()) || isCrossPrice(lastMinus3, ema5.getLast())
                || isCrossPrice(last, ema100.getLast()) || isCrossPrice(lastMinus3, ema100.getLast())
                || isCrossPrice(last, ema300.getLast()) || isCrossPrice(lastMinus3, ema300.getLast())
                || isCrossPrice(last, ema600.getLast()) || isCrossPrice(lastMinus3, ema600.getLast())
                || isCrossPrice(last, ema1000.getLast()) || isCrossPrice(lastMinus3, ema1000.getLast())

                ) {
            return Trend.RANGE;
        }
        if (diffEma5 > threshold && diffEma26 > threshold && diffEma100 > threshold && diffEma300 > threshold
                && diffEma600 > threshold && diffEma1000 > threshold
                ) {
            return Trend.BULLISH;

        } else if (diffEma5 < -threshold && diffEma26 < -threshold && diffEma100 < -threshold && diffEma300 < -threshold
                && diffEma600 < -threshold && diffEma1000 < -threshold
                ) {
            return Trend.BEARISH;
        }
        return Trend.RANGE;
    }
}
