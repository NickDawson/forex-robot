package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.indicators.IndicatorUtils;
import com.trading.forex.model.Candle;

import java.util.List;

/**
 * Created by wf on 10/24/2017.
 */
public class ADX extends IndicatorUtils {


    public static double[] values(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.adxLookback(periode);
        core.adxr(0, closePrice.length - 1, candles.stream().mapToDouble(candle -> candle.getHigh()).toArray(),
                candles.stream().mapToDouble(candle -> candle.getLow()).toArray(),
                closePrice, periode, begin, length, out);
        return out;
    }

    public static Double value(List<Candle> candles, int periode) {
        return getValue(values(candles, periode));
    }
}
