package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.common.model.Candle;
import com.trading.forex.indicators.IndicatorUtils;

import java.util.List;

/**
 * Created by hsouidi on 06/05/2017.
 */
public class WilliamR extends IndicatorUtils {


    public static double[] values(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        double[] highPrice = candles.stream().mapToDouble(candle -> candle.getHigh()).toArray();
        double[] lowPrice = candles.stream().mapToDouble(candle -> candle.getLow()).toArray();
        core.willR(0, candles.size() - 1, highPrice,lowPrice,closePrice, periode, begin, length, out);
        return out;
    }

    public static Double value(List<Candle> candles, int periode){
        return  getValue(values(candles,periode));
    }



}
