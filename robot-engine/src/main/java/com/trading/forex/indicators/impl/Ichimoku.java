package com.trading.forex.indicators.impl;

import com.trading.forex.common.model.Candle;
import com.trading.forex.common.utils.CustomList;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by hsouidi on 06/19/2017.
 */
@Getter
@Slf4j
public class Ichimoku {

    @Getter(AccessLevel.NONE)
    private int period1=9;
    @Getter(AccessLevel.NONE)
    private int period2=26;
    @Getter(AccessLevel.NONE)
    private int period3=52;


    private CustomList<Double> tenkenSen;
    private CustomList<TrendLine> tenkenSenLine;
    private CustomList<Double> kijunSen;
    private CustomList<TrendLine> kijunSenLine;
    private CustomList<Double> spanB;
    private CustomList<TrendLine> spanBLine;
    private CustomList<Double> spanA;
    private CustomList<TrendLine> spanALine;
    private CustomList<Double> chikou;
    private CustomList<Double> kumo = new CustomList<>();
    private CustomList<Integer> twist =new CustomList<>();

    public Ichimoku(List<Candle> candles) {
        tenkenSen = calculIndicator(candles, period1);
        kijunSen = calculIndicator(candles, period2);
        spanB = calculIndicator(candles, period3);
        spanA = calculIndicatorSpanA(tenkenSen, kijunSen);
        tenkenSenLine=calculTrendLine(tenkenSen);
        kijunSenLine=calculTrendLine(kijunSen);
        spanBLine=calculTrendLine(spanB);
        spanALine=calculTrendLine(spanA);
        chikou = calculChikouIndicator(candles);
        calculKumo();
        calculTwist();
    }

    public Double checkTrendLine(Candle candle, CustomList<TrendLine> lines, int minWidth){
        double max=Math.max(candle.getOpen(),candle.getClose());
        double min=Math.min(candle.getOpen(),candle.getClose());
        for(TrendLine trendLine:lines){
            if(trendLine.lineWidth>=minWidth&&(trendLine.price <= min && trendLine.price >=candle.getLow())
             || (trendLine.price >= max && trendLine.price <=candle.getHigh())){
                return trendLine.price;
            }
        }
        return null;


    }


    public double  positionWithTenkenSen(Candle candle){

        double maxCandle=Math.max(candle.getOpen(),candle.getClose());
        return  maxCandle-tenkenSen.getLast();
    }


    public double  positionWithCloud(Candle candle){

        double max=Math.max(spanA.getLastMinus(period2),spanB.getLastMinus(period2));
        double min=Math.min(spanA.getLastMinus(period2),spanB.getLastMinus(period2));

        double maxCandle=Math.max(candle.getOpen(),candle.getClose());
        double minCandle=Math.min(candle.getOpen(),candle.getClose());

        if(minCandle > max ){
            return minCandle-max;
        }if(maxCandle < min ){
            return minCandle-min;
        }
        return 0;

    }

    public List<LineSource>  getTrendLine(Candle candle){
        int precision=3;
        Double ssb=checkTrendLine(candle,spanBLine,precision);
        int weight=0;
        List<LineSource> result=new ArrayList<>();
        if(ssb!=null){
            weight+=5;
            result.add(LineSource.SSB);
        }
        Double ssa=checkTrendLine(candle,spanALine,precision);
        if(ssa!=null){
            weight+=4;
            result.add( LineSource.SSA);
        }
        Double kijun=checkTrendLine(candle,kijunSenLine,precision);
        if(kijun!=null){
            weight+=3;
            result.add( LineSource.KIJUINSEN);
        }
        Double tenken=checkTrendLine(candle,tenkenSenLine,precision);
        if(tenken!=null){
            weight+=2;
            result.add( LineSource.TENKENSEN);
        }
        log.info("weight ="+weight);
        return result;


    }

    @Builder
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class  TrendLine {

        private int lineWidth;
        private Double price;
    }

    public enum LineSource{
        SSA,SSB,TENKENSEN,KIJUINSEN
    }

    private void calculTwist() {

         for(int i=0;i<kumo.size()-1;i++){
             if(kumo.get(i)==0D||(kumo.get(i)>0&&kumo.get(i+1)<0)||(kumo.get(i)<0&&kumo.get(i+1)>0)){
                 twist.add(kumo.size()-1-i);
             }
         }
    }

    private CustomList<TrendLine> calculTrendLine(CustomList<Double> indicatorValues) {
        CustomList<TrendLine> result=new CustomList<>();
        for (int i=0;i<indicatorValues.size();i++){
            int count=1;
            Double current=indicatorValues.get(i);
            for (int j=i+1;j<indicatorValues.size();j++){
                 if(current.equals(indicatorValues.get(j))){
                     count++;
                 }else{
                     i=j-1;
                     break;
                 }
            }
            if(count>1){
                result.add(TrendLine.builder().lineWidth(count).price(current).build());
                if(count+i>=indicatorValues.size()){
                    break;
                }
            }

        }
        return result;
    }

    private void calculKumo() {
        int decalage = Math.abs(spanB.size() - spanA.size());
        int min = Math.min(spanB.size(), spanA.size());
        for (int i =0;i<min;i++){
            kumo.add(spanA.get(i+decalage)- spanB.get(i));
        }
    }

    private CustomList<Double> calculIndicator(List<Candle> candlesBrut, int periode) {
        CustomList<Double> result = new CustomList<>();
        for (int i = candlesBrut.size(); i > periode; i--) {
            List<Candle> candles = candlesBrut.subList(i - periode, i);
            Optional<Double> high = candles.stream().map(candle -> candle.getHigh()).max((o1, o2) -> o1.compareTo(o2));
            double low = candles.stream().map(candle -> candle.getLow()).min((o1, o2) -> o1.compareTo(o2)).get();
            result.add((high.get() + low) / 2);
        }
        Collections.reverse(result);
        return result;
    }

    private CustomList<Double> calculIndicatorSpanA(List<Double> tenkenSen, List<Double> kijunSen) {
        List<Double> result = new ArrayList<>();
        List<Double> tenkenSenM = tenkenSen.subList(tenkenSen.size() - kijunSen.size(), tenkenSen.size());
        for (int i = 0; i < kijunSen.size(); i++) {
            result.add((tenkenSenM.get(i) + kijunSen.get(i)) / 2);
        }
        return new CustomList(result);
    }

    private CustomList<Double> calculChikouIndicator(List<Candle> candlesBrut) {

        return new CustomList<>(candlesBrut.stream().map(candle -> candle.getClose()).collect(Collectors.toList()));
    }
}
