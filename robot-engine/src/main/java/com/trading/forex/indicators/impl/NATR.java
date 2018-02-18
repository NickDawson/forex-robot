package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.common.model.Candle;
import com.trading.forex.indicators.IndicatorUtils;

import java.util.List;

/**
 * Created by hsouidi on 10/24/2017.
 */
public class NATR extends IndicatorUtils {

    public static double[] values(List<Candle> candles, int period) {
        Core lib=new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];

        lib.natr(0,candles.size()-1,candles.stream().mapToDouble(candle -> candle.getHigh()).toArray()
                ,candles.stream().mapToDouble(candle -> candle.getLow()).toArray()
                ,candles.stream().mapToDouble(candle -> candle.getClose()).toArray()
                ,period,begin,length,out);
        return out;
    }

    public static Double value(List<Candle> candles, int periode){
        return  getValue(values(candles,periode));
    }
}
