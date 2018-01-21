package com.trading.forex.model;

import com.trading.forex.exceptions.RobotTechnicalException;

/**
 * Created by hsouidi on 11/19/2017.
 */
public enum Importance {

    HIGH("high"),MEDIUM("medium"),LOW("low");

    private String value;

    Importance(String value){
        this.value=value;
    }

    public static Importance  fromValue(String value){
        for(Importance importance:Importance.values()){
            if(value.trim().equals(importance.getValue())){
                return  importance;
            }
        }
        throw new RobotTechnicalException("Cannot found enum for  "+value);
    }

    public String getValue() {
        return value;
    }
}
