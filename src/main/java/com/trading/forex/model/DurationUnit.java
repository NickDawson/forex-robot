package com.trading.forex.model;

import lombok.Getter;

/**
 * Created by hsouidi on 05/14/2017.
 */
@Getter
public enum DurationUnit {

    MINUTE("m","min");

    private String brokerValue;
    private String indicatorValue;

    DurationUnit(String brokerValue,String indicatorValue){
        this.brokerValue=brokerValue;
        this.indicatorValue=indicatorValue;
    }

}
