package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.common.model.Candle;
import com.trading.forex.common.utils.CustomList;
import com.trading.forex.indicators.IndicatorUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by hsouidi on 10/24/2017.
 */
public class MACD extends IndicatorUtils {

    public static MACDValues values(List<Candle> candles, int fastPeriod, int slowPeriod, int signalPeriod) {
        Core lib = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] outMACD = new double[candles.size()];
        double[] outMACDSignal = new double[candles.size()];
        double[] outMACDHist = new double[candles.size()];
        lib.macd(0, candles.size() - 1, candles.stream().mapToDouble(candle -> candle.getClose()).toArray(), fastPeriod, slowPeriod, signalPeriod, begin, length, outMACD, outMACDSignal, outMACDHist);
        return new MACDValues(outMACD, outMACDSignal, outMACDHist);
    }

    public static MACDValue value(List<Candle> candles, int fastPeriod, int slowPeriod, int signalPeriod) {
        MACDValues macdValues = values(candles, fastPeriod, slowPeriod, signalPeriod);
        return new MACDValue(getValue(macdValues.getOutMACD()), getValue(macdValues.getOutMACDSignal()), getValue(macdValues.getOutMACDHist()));
    }

    public static CustomList<Integer> cross(double[] out) {
        CustomList<Integer> result = new CustomList<>();
        int realLength=getIndex(out);
        for (int i = realLength; i > 0; i--) {
            if ((out[i] > 0 && out[i - 1] < 0) || (out[i] < 0 && out[i - 1] > 0)) {
                result.add(realLength - i);
            }
        }
        return result;
    }

    @Getter
    @AllArgsConstructor
    public static class MACDValues {
        private double[] outMACD;
        private double[] outMACDSignal;
        private double[] outMACDHist;
    }

    @Getter
    @AllArgsConstructor
    public static class MACDValue {
        private double macd;
        private  double macdSignal;
        private double macdHist;
    }

}
