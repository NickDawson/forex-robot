package com.trading.forex.indicators;

import com.trading.forex.exceptions.RobotTechnicalException;
import com.trading.forex.model.Candle;
import com.trading.forex.model.SearchResult;
import com.trading.forex.utils.CustomList;

/**
 * Created by hsouidi on 10/24/2017.
 */
public class IndicatorUtils {

    public static double getValue(double[] out){

        for(int i=out.length-1;i>=0;i--){
            if(out[i]!=0.0){
                return out[i];
            }
        }
        throw  new RobotTechnicalException("Empty Table");
    }

    public static SearchResult getLow(CustomList<Candle> candles, int periode){
        final int size=candles.size();
        if(candles.isEmpty()&&size<periode){
            throw new RobotTechnicalException("Candle array size  must be greater than periode ");
        }
        int skip=size<periode?0:size-periode;
        Double min=candles.getLast().getLow();
        int index=candles.size()-1;
        for(int i=skip;i<size;i++){
            Double low=candles.get(i).getLow();
            if(min>low){
                min=low;
                index=i;
            }
        }
        return SearchResult.builder().index(index).value(min).build();
    }

    public static SearchResult getHigh(CustomList<Candle> candles,int periode){
        final int size=candles.size();
        if(candles.isEmpty()&&size<periode){
            throw new RobotTechnicalException("Candle array size  must be greater than periode ");
        }
        int skip=size<periode?0:size-periode;
        double max=candles.getLast().getHigh();
        Integer index=candles.size()-1;
        for(int i=skip;i<size;i++){
            double high=candles.get(i).getHigh();
            if(max<high){
                max=high;
                index=i;
            }
        }
        return SearchResult.builder().index(index).value(max).build();
    }

    public static  Integer getValue(int[] out){

        for(int i=out.length-1;i>0;i--){
            if(out[i]!=0.0){
                return out[i];
            }
        }
        return null;
    }

    public static int getIndex(double[] out){

        for(int i=out.length-1;i>0;i--){
            if(out[i]!=0.0){
                return i;
            }
        }
        throw  new RobotTechnicalException("Empty Table");
    }

    public static Integer getIndex(int[] out){

        for(int i=out.length-1;i>0;i--){
            if(out[i]!=0.0){
                return i;
            }
        }
        throw  new RobotTechnicalException("Empty Table");
    }
}
