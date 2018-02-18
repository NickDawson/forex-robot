package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.common.model.Candle;
import com.trading.forex.indicators.IndicatorUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Created by hsouidi on 10/24/2017.
 */
public class BolingerBands extends IndicatorUtils {


    public static BBONDS values(List<Candle> candles, int period) {
        Core lib = new Core();
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        double[] outRealUpperBand = new double[candles.size()];
        double[] outRealMiddleBand = new double[candles.size()];
        double[] outRealLowerBand = new double[candles.size()];
        lib.bbands(0, candles.size() - 1, candles.stream().mapToDouble(candle -> candle.getClose()).toArray()
                , period
                , 2
                , 2
                , MAType.Sma
                , outBegIdx
                , outNBElement
                , outRealUpperBand
                , outRealMiddleBand
                , outRealLowerBand
        );
        return new BBONDS(outRealUpperBand, outRealMiddleBand, outRealLowerBand);
    }

    public static BBOND value(List<Candle> candles, int periode) {
        BBONDS bbonds = values(candles, periode);
        return new BBOND(getValue(bbonds.getOutRealLowerBand()), getValue(bbonds.getOutRealMiddleBand()), getValue(bbonds.getOutRealUpperBand()));
    }


    @Getter
    @AllArgsConstructor
    public static class BBONDS {
        private double[] outRealUpperBand;
        private double[] outRealMiddleBand;
        private double[] outRealLowerBand;
    }

    @Getter
    @AllArgsConstructor
    public static class BBOND {
        private double upperBand;
        private double middleBand;
        private double lowerBand;

        public int positionWithBond(Candle candle) {
            if (candle.body() > 0) {
                return upperBand < candle.getClose() ? 1 : 0;
            } else {
                return lowerBand > candle.getClose() ? -1 : 0;
            }
        }
    }

}
