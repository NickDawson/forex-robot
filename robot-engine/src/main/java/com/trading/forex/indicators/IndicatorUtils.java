package com.trading.forex.indicators;

import com.trading.forex.common.exceptions.RobotTechnicalException;
import com.trading.forex.common.model.Way;
import com.trading.forex.common.utils.CustomList;
import com.trading.forex.model.SearchResult;
import com.trading.forex.common.model.Candle;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public static boolean isSorted(double[] input, int NbelementTobeChecked, Way order) {
        final int index=getIndex(input);
        List<Double> list = Arrays.stream(input).boxed().collect(Collectors.toList()).subList(index - NbelementTobeChecked, index);
        boolean sorted = true;

        for (int i = 1; i < list.size(); i++) {
            if ((order.getValue() * list.get(i).compareTo(list.get(i - 1))) < 0) {
                return false;
            }
        }

        return sorted;
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
