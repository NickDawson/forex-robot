package com.trading.forex.model;

import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.CandlestickData;
import com.oanda.v20.primitives.DateTime;
import com.trading.forex.exceptions.RobotTechnicalException;
import lombok.Builder;
import lombok.Getter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static com.trading.forex.utils.AlgoUtils.toPip;

/**
 * Created by hsouidi on 10/21/2017.
 */
@Builder
@Getter
public class Candle {

    private Long epoch;
    private Double close;
    private Double high;
    private Double low;
    private Double open;
    private double volume;
    private Symbol symbol;

    private static String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'";

    public double body() {
        return toPip(symbol,this.close - this.open);
    }

    public Date date() {
        return new Date(epoch);
    }



    public static Candle toCandle(Candlestick candlestick,Symbol symbol) {
        final CandlestickData candlestickData = candlestick.getMid();
        return Candle.builder()
                .open(candlestickData.getO().doubleValue())
                .close(candlestickData.getC().doubleValue())
                .high(candlestickData.getH().doubleValue())
                .low(candlestickData.getL().doubleValue())
                .volume(candlestick.getVolume())
                .epoch(toEpoch(candlestick.getTime()))
                .symbol(symbol)
                .build();
    }

    private static Long toEpoch(DateTime dateTime) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT")));
        try {
            return simpleDateFormat.parse(dateTime.toString()).getTime();
        } catch (ParseException e) {
            throw new RobotTechnicalException(e);
        }
    }
}
