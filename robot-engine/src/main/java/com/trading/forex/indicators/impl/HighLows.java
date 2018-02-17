package com.trading.forex.indicators.impl;

import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.trading.forex.common.model.Candle;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.utils.CustomList;

import java.util.List;

import static com.trading.forex.indicators.IndicatorUtils.toList;

/**
 * Created by hsouidi on 11/01/2017.
 */
public class HighLows {


    public static double[] valuesMin(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.min(0, closePrice.length - 1, closePrice, periode, begin, length, out);
        return out;
    }

    public static int[] valuesMinIndex(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        int[] out = new int[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getLow()).toArray();
        core.minIndex(0, closePrice.length - 1, closePrice, periode, begin, length, out);
        return out;
    }

    public static CustomList<Integer> swingLows(List<Candle> candles, Symbol symbol) {
        CustomList<Integer> result = new CustomList<>();
        CustomList<Integer> indexs = toList(HighLows.valuesMinIndex(candles, 5));
        for (int i = 1; i < indexs.size() - 2; i++) {
            int ind = indexs.get(i);
            if(result.contains(ind)){
                continue;
            }
            if (result.isEmpty() ||
                    (ind - result.getLast() > 5 )) {

                Candle prev = candles.get(ind - 1);
                Candle next = candles.get(ind + 1);
                if (prev.body() < 0 && next.body() > 0) {
                    result.add(ind);
                }
            }
        }
        return result;
    }

    public static CustomList<Integer> swingHighs(List<Candle> candles, Symbol symbol) {
        CustomList<Integer> result = new CustomList<>();
        CustomList<Integer> indexs = toList(HighLows.valuesMaxIndex(candles, 5));
        for (int i = 1; i < indexs.size() - 2; i++) {
            int ind = indexs.get(i);
            if(result.contains(ind)){
                continue;
            }
            if (result.isEmpty() ||
                    (ind - result.getLast() > 5 )) {

                Candle prev = candles.get(ind - 1);
                Candle next = candles.get(ind + 1);
                if (prev.body() > 0 && next.body() < 0) {
                    result.add(ind);
                }
            }
        }
        return result;
    }



    public static int[] valuesMaxIndex(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        int[] out = new int[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getHigh()).toArray();
        core.maxIndex(0, closePrice.length - 1, closePrice, periode, begin, length, out);
        return out;
    }

    public static double[] valuesMax(List<Candle> candles, int periode) {
        Core core = new Core();
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double[] out = new double[candles.size()];
        double[] closePrice = candles.stream().mapToDouble(candle -> candle.getClose()).toArray();
        core.max(0, closePrice.length - 1, closePrice, periode, begin, length, out);
        return out;
    }
}
