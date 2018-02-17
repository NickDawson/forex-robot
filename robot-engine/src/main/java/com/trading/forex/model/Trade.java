package com.trading.forex.model;

import com.trading.forex.common.exceptions.RobotTechnicalException;
import com.trading.forex.common.model.Candle;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.model.Way;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by hsouidi on 11/01/2017.
 */
@Data
@AllArgsConstructor
public class Trade {

    private Way way;
    private Symbol symbol;
    private Double takeProift;
    private Double stopLoss;
    private Double price;
    private String comment;

    public Double currentProfit(Double tick){
        return  way.getValue()*(tick-price);
    }

    public boolean checkConsistancy(){
        if(takeProift==null&&stopLoss==null){
            return true;
        }
        if(way.getValue()*(takeProift-price)<0||way.getValue()*(stopLoss-price)>0){
           throw new RobotTechnicalException("Trade Consistancy check is KO !!");
        }

        return true;
    }

    public Double status(Candle candle){
        if(candle==null){
            throw new RobotTechnicalException("Candle  is null !!!!");
        }
        switch (way) {
            case CALL:
                return  takeProift!=null&&candle.getHigh()>takeProift?takeProift:stopLoss!=null&&candle.getLow()<stopLoss?stopLoss:0;
            case PUT:
                return  takeProift!=null&&candle.getLow()<takeProift?takeProift:stopLoss!=null&&candle.getHigh()>stopLoss?stopLoss:0;
            default:
                return 0D;
        }
    }
}
