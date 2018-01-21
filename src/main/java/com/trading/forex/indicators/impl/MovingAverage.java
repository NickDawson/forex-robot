package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.indicators.IndicatorUtils;
import com.trading.forex.model.Candle;
import com.trading.forex.model.Symbol;
import com.trading.forex.model.Way;
import com.trading.forex.utils.CustomList;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import static com.trading.forex.utils.AlgoUtils.toPip;

/**
 * Created by hsouidi on 10/24/2017.
 */
@Slf4j
public class MovingAverage extends IndicatorUtils {


    public static double[] valuesSma(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.sma(0, closePrice.length - 1, closePrice, periode, begin, length, out);
        return out;
    }

    public static double[] valuesWma(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.wma(0, closePrice.length - 1, closePrice, periode, begin, length, out);
        return out;
    }

    public static Double valueWma(List<Candle> candles, int periode) {
        return getValue(valuesWma(candles, periode));
    }

    public static Double valueSma(List<Candle> candles, int periode) {
        return getValue(valuesSma(candles, periode));
    }

    public static Double valueEma(List<Candle> candles, int periode) {
        return getValue(valuesEma(candles, periode));
    }

    public static CustomList<Integer> getCrossWithPrice(double[] outMin, CustomList<Candle> candles) {

        int mmIndex = getIndex(outMin);
        int decalage = candles.size() - 1 - mmIndex;
        final CustomList<Integer> result = new CustomList<>();
        for (int i = mmIndex; i > decalage; i--) {
            Candle candle = candles.get(i + decalage);
            double ema = outMin[i];
            if (ema < candle.getHigh() && ema > candle.getLow()) {
                result.add(mmIndex - i);

            }
        }
        Collections.reverse(result);

        return result;
    }

    public static CustomList<Integer> getCrossList(double[] outMin, double[] outMax, Way way, Symbol symbol) {

        int currentEma6 = getIndex(outMin);
        int currentEma24 = getIndex(outMax);
        int decalage = currentEma6 - currentEma24;
        final CustomList<Integer> result = new CustomList<>();
        for (int i = currentEma6; i > decalage; i--) {
            double anglePrec = toPip(symbol, way.getValue()
                    * Double.valueOf((outMin[i - 1] - outMax[i - decalage - 1])));
            double angleNext = toPip(symbol, way.getValue()
                    * Double.valueOf((outMin[i] - outMax[i - decalage])));
            if (anglePrec < 0D && angleNext > 0D) {
                result.add(currentEma6 - i);
            }
        }
        Collections.reverse(result);

        return result;
    }

    public static Double valueDema(List<Candle> candles, int periode) {
        return getValue(valuesDema(candles, periode));
    }


    public static double[] valuesEma(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.ema(0, closePrice.length - 1, closePrice, periode, begin, length, out);
        return out;
    }

    public static double[] valuesDema(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.dema(0, closePrice.length - 1, closePrice, periode, begin, length, out);
        return out;
    }
}
