package com.trading.forex.utils;

import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.exceptions.RobotTechnicalException;
import com.trading.forex.indicators.IndicatorUtils;
import com.trading.forex.model.Candle;
import com.trading.forex.model.Symbol;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;

/**
 * Created by wf on 12/09/2017.
 */
public class AlgoUtils {


    private AlgoUtils() {
        throw new RuntimeException();
    }


    public static boolean checkDateWithCandlestickGranularity(CandlestickGranularity candlestickGranularity ,Date toCheck,Date dateInput){
               return candlestickGranularityToTimeStamp(candlestickGranularity)>=Math.abs(dateInput.getTime()-toCheck.getTime());
    }
    public static Date getFromDate(Date to, CandlestickGranularity candlestickGranularity) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(to.toInstant(), ZoneId.systemDefault());
        Function<LocalDateTime, LocalDateTime> function = null;
        switch (candlestickGranularity) {
            case H1:
                function = localDateTime1 -> localDateTime1.minusHours(1);
                return process(localDateTime, function, 200);
            case M15:
                function = localDateTime1 -> localDateTime1.minusMinutes(15);
                return process(localDateTime, function, 400);
            case M5:
                function = localDateTime1 -> localDateTime1.minusMinutes(5);
                return process(localDateTime, function, 4002);
            case D:
                function = localDateTime1 -> localDateTime1.minusDays(1);
                return process(localDateTime, function, 30);
            case M:
                function = localDateTime1 -> localDateTime1.minusMonths(1);
                return process(localDateTime, function, 4);
            default:
                throw new RobotTechnicalException("Cannot found handler for candlestickGranularity " + candlestickGranularity);
        }

    }


    public static long  candlestickGranularityToTimeStamp(CandlestickGranularity candlestickGranularity) {
        long sixty=60L;
        long minute=60L*1000;
        switch (candlestickGranularity) {
            case H1:
                return sixty*minute;
            case M15:
                return 15L*minute;
            case M5:
                return 5L*minute;
            case D:
                return 24L*sixty*minute;
            case M:
                return 31L*24L*sixty*minute;
            default:
                throw new RobotTechnicalException("Cannot found handler for candlestickGranularity " + candlestickGranularity);
        }

    }

    public static CustomList<Double> toList(double[] array) {

        int length = IndicatorUtils.getIndex(array);
        final CustomList<Double> result = new CustomList<>();
        for (int i = 0; i <= length; i++) {
            result.add(array[i]);
        }
        return result;
    }

    public static CustomList<Integer> toList(int[] array) {

        int length = IndicatorUtils.getIndex(array);
        final CustomList<Integer> result = new CustomList<>();
        for (int i = 0; i <= length; i++) {
            result.add(array[i]);
        }
        return result;
    }


    public static Double normalize(Double value, Symbol symbol) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(symbol.getDecimal(), RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Date getStartOfDay(Date day) {
        final Date input=day == null?new Date():day;
        Calendar cal = Calendar.getInstance();
        cal.setTime(input);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    public static Date getEndOfDay(Date day) {
        final Date input=day == null?new Date():day;
        Calendar cal = Calendar.getInstance();
        cal.setTime(input);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal.getTime();
    }

    public static double toPip(Symbol symbol, double value) {
        return value * Math.pow(10, symbol.getDecimal() - 1);
    }

    public static Date process(LocalDateTime localDateTime, Function<LocalDateTime, LocalDateTime> function, int count) {

        LocalDateTime dateTime = localDateTime;
        for (int i = 0; i < count; i++) {
            dateTime = function.apply(dateTime);
        }

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static boolean isCrossPrice(Candle candle, Double price) {

        int fact = candle.body() > 0 ? 1 : -1;
        return fact * (price - candle.getOpen()) > 0 && fact * (price - candle.getClose()) < 0;
    }


}







