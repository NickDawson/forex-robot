package com.trading.forex.indicators.impl;

import com.trading.forex.indicators.IndicatorUtils;
import com.trading.forex.model.Candle;
import com.trading.forex.model.FibonacciResult;
import com.trading.forex.model.SearchResult;
import com.trading.forex.model.Way;
import com.trading.forex.utils.CustomList;

import java.util.function.Function;

/**
 * Created by wf on 12/15/2017.
 */
public class Fibonacci extends IndicatorUtils {


    public static Double getNextBarrier(double price, Way way, FibonacciResult... fibonacciResults) {
        for (FibonacciResult fibonacciResult : fibonacciResults) {

            final Double value = fibonacciResult.getNextBarrier(price, way);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static Double getPreviousBarrier(double price, Way way, FibonacciResult... fibonacciResults) {
        for (FibonacciResult fibonacciResult : fibonacciResults) {

            final Double value = fibonacciResult.getPreviousBarrier(price, way);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static FibonacciResult values(CustomList<Candle> candles, int periode) {

        final SearchResult lowSearchResult = getLow(candles, periode);
        final SearchResult highSearchResult = getHigh(candles, periode);
        final Double low = lowSearchResult.getValue();
        final Double high = highSearchResult.getValue();
        Function<Double, Double> retracement = null;
        Function<Double, Double> extention = null;
        final Way way = lowSearchResult.getIndex() == highSearchResult.getIndex() ? candles.getFirst().getClose() > candles.getFirst().getOpen() ? Way.CALL : Way.PUT : lowSearchResult.getIndex() < highSearchResult.getIndex() ? Way.CALL : Way.PUT;
        if (way == Way.CALL) {
            retracement = percentage -> high - ((high - low) * percentage);
            extention = percentage -> high + ((high - low) * percentage);

        } else {
            retracement = percentage -> low + ((high - low) * percentage);
            extention = percentage -> low - ((high - low) * percentage);
        }
        return FibonacciResult.builder()
                .ret_0(retracement.apply(0D))
                .ret_23_6(retracement.apply(0.236D))
                .ret_38_2(retracement.apply(0.382D))
                .ret_50(retracement.apply(0.50D))
                .ret_61_8(retracement.apply(0.618))
                .ret_76_4(retracement.apply(0.764))
                .ret_100(retracement.apply(1D))
                .ext_61_8(extention.apply(0.618))
                .ext_100(extention.apply(1D))
                .ext_138_2(extention.apply(0.1382))
                .ext_161_8(extention.apply(1.618))
                .ext_200(extention.apply(2D))
                .ext_261_8(extention.apply(2.618))
                .indexHigh(candles.get(highSearchResult.getIndex()))
                .indexLow(candles.get(lowSearchResult.getIndex()))
                .way(way)
                .build();
    }


}




