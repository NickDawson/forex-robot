package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.common.model.Candle;
import com.trading.forex.indicators.IndicatorUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class StandardDeviation extends IndicatorUtils {

    public static double[] values(List<Candle> candles, int periode, int nbdev) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.stdDev(0, closePrice.length - 1, candles.stream().mapToDouble(candle -> candle.getHigh()).toArray()
                , periode, nbdev, begin, length, out);
        return out;
    }

    public static Double value(List<Candle> candles, int periode, int nbdev) {
        return getValue(values(candles, periode, nbdev));
    }
}
