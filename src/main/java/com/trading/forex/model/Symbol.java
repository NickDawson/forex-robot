package com.trading.forex.model;

import com.trading.forex.exceptions.RobotTechnicalException;
import lombok.Getter;

/**
 * Created by hsouidi on 04/23/2017.
 */
@Getter
public enum Symbol {

    EUR_USD("EURUSD","EUR_USD","eur-usd",0,5,true),
    EUR_JPY("EURJPY","EUR_JPY","eur-jpy",1,3,true),
    EUR_GBP("EURGBP","EUR_GBP","eur-gbp",2,5,true),
    EUR_CHF("EURCHF","EUR_CHF","eur-chf",3,5,true),
    USD_JPY("USDJPY","USD_JPY","usd-jpy",4,3,true),
    USD_CHF("USDCHF","USD_CHF","usd-chf",5,5,true),
    GBP_USD("GBPUSD","GBP_USD","gbp-usd",6,5,false), // a eviter tres volatille
    GBP_JPY("GBPJPY","GBP_JPY","gbp-jpy",7,3,true),
    GBP_CHF("GBPCHF","GBP_CHF","gbp-chf",8,5,true),
    AUD_USD("AUDUSD","AUD_USD","aud-usd",9,5,true),
    USD_CAD("USDCAD","USD_CAD","usd-cad",10,5,true),
    AUD_JPY("AUDJPY","AUD_JPY","aud-jpy",11,3,true),
    NZD_USD("NZDUSD","NZD_USD","nzd-usd",12,5,true);

    private String indicatorValue;
    private String brokerValue;
    private String investingValue;
    private int value;
    public int decimal;
    private boolean activated;

    Symbol(String indicatorValue,String brokerValue,String investingValue,int value,int decimal,boolean activated ){
        this.brokerValue=brokerValue;
        this.indicatorValue=indicatorValue;
        this.investingValue=investingValue;
        this.value = value;
        this.decimal = decimal;
        this.activated=activated;

    }

    public static Symbol fromInvestingValue(String investingValue){
        for(Symbol symbol:Symbol.values()){
            if(symbol.getInvestingValue().equals(investingValue)){
                return symbol;
            }
        }
        throw new RobotTechnicalException("cannot found investingValue"+investingValue);
    }

    public static Symbol fromBrokerValue(String brokerValue){
        for(Symbol symbol:Symbol.values()){
            if(symbol.getBrokerValue().equalsIgnoreCase(brokerValue)){
                return symbol;
            }
        }
        throw new RobotTechnicalException("cannot found brokerValue"+brokerValue);
    }
    public static Symbol fromIndicatorValue(String indicatorValue){
        for(Symbol symbol:Symbol.values()){
            if(symbol.getIndicatorValue().equalsIgnoreCase(indicatorValue)){
                return symbol;
            }
        }
        throw new RobotTechnicalException("cannot found indicatorValue"+indicatorValue);
    }
}
