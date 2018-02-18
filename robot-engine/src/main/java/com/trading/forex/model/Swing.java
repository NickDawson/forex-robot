package com.trading.forex.model;

import com.trading.forex.common.model.Candle;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.SimpleDateFormat;

@Data
@AllArgsConstructor
public class Swing implements Comparable<Swing> {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private Double pip;
    private Double price;
    private Candle candle;

    public String candleDate() {
        return "Swing Date=" + (candle != null ? DATE_FORMAT.format(candle.date()) : "No Swing Date");
    }

    @Override
    public int compareTo(Swing o) {
        return this.pip.compareTo(o.pip);
    }
}
