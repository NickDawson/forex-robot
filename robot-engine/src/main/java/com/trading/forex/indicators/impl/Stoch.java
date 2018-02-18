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
 * Created by hsouidi on 12/02/2017.
 */
public class Stoch extends IndicatorUtils {

    // 11,5,3
    public static StochResult values(List<Candle> candles, int periodeFastK,int periodeSlowK,int peiodeSlowD) {
        Core lib=new Core();
        MAType optInSlowK_MAType=MAType.Sma;
        MAType optInSlowD_MAType=MAType.Sma;
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        double[] outSlowK = new double[candles.size()];
        double[] outSlowD = new double[candles.size()];
        lib.stoch(0,candles.size()-1,candles.stream().mapToDouble(candle -> candle.getHigh()).toArray()
                ,candles.stream().mapToDouble(candle -> candle.getLow()).toArray()
                ,candles.stream().mapToDouble(candle -> candle.getClose()).toArray()
                ,periodeFastK,periodeSlowK,optInSlowK_MAType,peiodeSlowD,optInSlowD_MAType
                ,outBegIdx,outNBElement,outSlowK,outSlowD);
        return new StochResult(outSlowK,outSlowD);
    }


    public static StochRSI valuesRSI(List<Candle> candles,int periode, int periodeFastK,int peiodeFastD) {
        Core lib=new Core();
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        double[] outFastK = new double[candles.size()];
        double[] outFastD = new double[candles.size()];
        lib.stochRsi(0,candles.size()-1
                ,candles.stream().mapToDouble(candle -> candle.getClose()).toArray(),
                periode,periodeFastK,peiodeFastD,MAType.Sma
                ,outBegIdx,outNBElement,outFastK,outFastD);
        return new StochRSI(outFastK,outFastD);
    }

    @Getter
    @AllArgsConstructor
    public static class StochResult{
        private double[] outSlowK ;
        private double[] outSlowD;
    }

    @Getter
    @AllArgsConstructor
    public static class StochResultValue{
        private double outSlowK ;
        private double outSlowD;
    }

    @Getter
    @AllArgsConstructor
    public static class StochRSI{
        private double[] outFastK ;
        private double[] outFastD;
    }

    public static StochResultValue value(List<Candle> candles, int periodeFastK,int periodeSlowK,int peiodeSlowD){
        final StochResult stochResult=values(candles,periodeFastK,periodeSlowK,peiodeSlowD);
        return  new StochResultValue(getValue(stochResult.getOutSlowK()),getValue(stochResult.getOutSlowD()));
    }
}
