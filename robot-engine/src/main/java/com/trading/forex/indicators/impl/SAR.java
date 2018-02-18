package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.common.model.Candle;
import com.trading.forex.indicators.IndicatorUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Created by hsouidi on 06/08/2017.
 */
@Slf4j
public class SAR extends IndicatorUtils {


    public static double[] values(List<Candle> candles) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.sar(0, closePrice.length - 1, candles.stream().mapToDouble(candle -> candle.getHigh()).toArray(),
                candles.stream().mapToDouble(candle -> candle.getLow()).toArray(),
                0.02,0.2,begin, length, out);
        return out;
    }

    public static Double value(List<Candle> candles){
        return  getValue(values(candles));
    }
}
