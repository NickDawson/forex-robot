package com.trading.forex.entity;


import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.model.Candle;
import com.trading.forex.model.Symbol;
import com.trading.forex.utils.CustomList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by wf on 09/06/2017.
 */
@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CandleEntity {


    @EmbeddedId
    private Key key;
    private Double close;
    private Double high;
    private Double low;
    private Double open;
    private double volume;


    @Embeddable
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {

        private Long epoch;
        @Enumerated(EnumType.STRING)
        private Symbol symbol;
        @Enumerated(EnumType.STRING)
        private CandlestickGranularity candlestickGranularity;
    }

    public static List<CandleEntity> build(List<Candle> candles, CandlestickGranularity candlestickGranularity, Symbol symbol) {
        return candles.parallelStream().map(candle -> build(candle,candlestickGranularity, symbol)).collect(Collectors.toList());
    }

    public static CandleEntity build(Candle candle,CandlestickGranularity candlestickGranularity, Symbol symbol) {
        return CandleEntity.builder().key(Key.builder().epoch(candle.getEpoch()).symbol(symbol)
                .candlestickGranularity(candlestickGranularity).build())
                .close(candle.getClose())
                .high(candle.getHigh())
                .low(candle.getLow())
                .open(candle.getOpen())
                .volume(candle.getVolume())
                .build();
    }

    public static CustomList<Candle> toCandle(List<CandleEntity> candle) {
        return candle.parallelStream().map(candleEntity -> toCandle(candleEntity)).collect(Collectors.toCollection(CustomList::new));
    }

    public static Candle toCandle(CandleEntity candle) {
        return Candle.builder()
                .epoch(candle.getKey().epoch)
                .close(candle.getClose())
                .high(candle.getHigh())
                .low(candle.getLow())
                .open(candle.getOpen())
                .volume(candle.getVolume())
                .symbol(candle.getKey().getSymbol())
                .build();
    }



}
