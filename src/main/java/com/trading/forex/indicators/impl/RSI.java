package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.indicators.IndicatorUtils;
import com.trading.forex.model.Candle;
import com.trading.forex.model.Way;

import java.util.List;

/**
 * Created by wf on 06/05/2017.
 */
public class RSI extends IndicatorUtils {


    public static double[] values(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.rsi(0, candles.size() - 1, closePrice, periode, begin, length, out);
        return out;
    }

    public static Double value(List<Candle> candles, int periode){
        return  getValue(values(candles,periode));
    }

    public static boolean check(List<Candle> candles, int periode,Way way){
        double value=getValue(values(candles,periode));
        if( Way.CALL.equals(way)){
            return value<80;
        } else  if( Way.PUT.equals(way)){
            return value>30;
        }
        return false;
    }


}
