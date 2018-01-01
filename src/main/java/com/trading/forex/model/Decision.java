package com.trading.forex.model;

import com.trading.forex.exceptions.RobotTechnicalException;

/**
 * Created by wf on 04/23/2017.
 */
public enum Decision {

    BUY("Buy"),STRONG_BUY("Strong Buy"),SELL("Sell"),STRONG_SELL("Strong Sell"),NEUTRAL("Neutral"),STOP_TRADING("Stop")
    ,OVERBOUGHT("Overbought"),OVERSOLD("Oversold"),HIGH_VOLATILITY("High Volatility")
    ,LESS_VOLATILITY("Less Volatility");
    private String value;

    Decision(String value){

        this.value=value;
    }

    public static Decision fromValue(String valueStr){
        for(Decision decision:Decision.values()){
            if(decision.value.equalsIgnoreCase(valueStr)){
                return decision;
            }
        }
        throw new RobotTechnicalException("cannot found Decision"+valueStr);
    }

}

