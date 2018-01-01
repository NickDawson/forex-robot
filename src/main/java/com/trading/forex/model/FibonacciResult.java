package com.trading.forex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by wf on 12/15/2017.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FibonacciResult {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm");


    private Way way;
    private double ret_0;
    private double ret_23_6;
    private double ret_38_2;
    private double ret_50;
    private double ret_61_8;
    private double ret_76_4;
    private double ret_100;
    private double ext_261_8;
    private double ext_200;
    private double ext_161_8;
    private double ext_138_2;
    private double ext_100;
    private double ext_61_8;
    private Candle indexHigh;
    private Candle indexLow;

    public  String dateBeginEnd (){
        Date begin;
        Date end;
        if(way==Way.CALL){
            begin=indexLow.date();
            end=indexHigh.date();
        }else {
            begin=indexHigh.date();
            end=indexLow.date();
        }
        return "Fibonacci BeginDate="+DATE_FORMAT.format(begin)+", EndDate="+DATE_FORMAT.format(end);
    }

    public Double getNextBarrier(double price,Way wayTrade) {
        return getNextBarrier(price,wayTrade,false) ;
    }

    public Double getNextBarrier(double price,Way wayTrade,boolean beforePrice) {
        List<Double> barries = Arrays.asList(
                ret_0,
                ret_23_6,
                ret_38_2,
                ret_50,
                ret_61_8,
                ret_76_4,
                ret_100);

        int size=barries.size();
        Double preBarrier;
        Double barrier=null;
        if(wayTrade==Way.CALL){
             for(int i=0;i<size;i++){
                 preBarrier=barrier;
                 barrier=barries.get(i);
                 if(!beforePrice&&barrier>price){
                     return barrier;

                 }else if(beforePrice&&barrier>price){
                     return preBarrier==null?barrier:preBarrier;
                 }
             }
        }else{
            inverse(barries);
            for(int i=size-1;i>=0;i--){
                preBarrier=barrier;
                barrier=barries.get(i);
                if(!beforePrice&&barrier<price){
                    return barrier;

                }else if(beforePrice&&barrier<price){
                    return preBarrier==null?barrier:preBarrier;
                }
            }
        }
        return null;
    }

    private void inverse(List input){
        int size=input.size();
        for(int j=0,i=size-1;i>j;i--,j++){
            Object tmp=input.get(i);
            input.set(i,input.get(j));
            input.set(j,tmp);
        }
    }

    public Double getPreviousBarrier(double price,Way wayTrade) {
        return getPreviousBarrier(price,wayTrade,false) ;
    }


    public Double getPreviousBarrier(double price,Way wayTrade,boolean beforePrice) {
        List<Double> barries = Arrays.asList(
                ret_0,
                ret_23_6,
                ret_38_2,
                ret_50
                ,
                ret_61_8,
                ret_76_4,
                ret_100
                );
        Double preBarrier;
        Double barrier=null;
        int size=barries.size();
        if(wayTrade==Way.CALL){
            for(int i=0;i<size;i++){
                preBarrier=barrier;
                barrier=barries.get(i);
                if(!beforePrice&&barrier<price){
                    return barrier;

                }else if(beforePrice&&barrier<price){
                    return preBarrier==null?barrier:preBarrier;
                }
            }
        }else{
            inverse(barries);
            for(int i=size-1;i>=0;i--){
                preBarrier=barrier;
                barrier=barries.get(i);
                if(!beforePrice&&barrier>price){
                    return barrier;

                }else if(beforePrice&&barrier>price){
                    return preBarrier==null?barrier:preBarrier;
                }

            }
        }
        return null;
    }
}
