package com.trading.forex.indicators.impl;

import com.trading.forex.model.Candle;
import com.trading.forex.model.PivotPointResult;

/**
 * Created by wf on 11/28/2017.
 */
public class PivotPoint {


    public static PivotPointResult calcul(Candle jm1Candle) {

        double c = jm1Candle.getClose();
        double h = jm1Candle.getHigh();
        double l = jm1Candle.getLow();

        double pivot = (l + h + c) / 3;

        double s1 = (pivot * 2) - h;
        double s2 = pivot - (h - l);
        double s3 = l - 2 * (h - pivot);

        double r1 = (pivot * 2) - l;
        double r2 = pivot + (h - l);
        double r3 = h + 2 * (pivot - l);

        double ps1 = 100 * (s1 - c) / c;
        double ps2 = 100 * (s2 - c) / c;
        double ps3 = 100 * (s3 - c) / c;

        double pr1 = 100 * (r1 - c) / c;
        double pr2 = 100 * (r2 - c) / c;
        double pr3 = 100 * (r3 - c) / c;

        return PivotPointResult.builder()
                .pivot(pivot)
                .r1(r1)
                .pr1(pr1)
                .r2(r2)
                .pr2(pr2)
                .r3(r3)
                .pr3(pr3)
                .s1(s1)
                .ps1(ps1)
                .s2(s2)
                .ps2(ps2)
                .s3(s3)
                .ps3(ps3)
                .build();
    }
}
