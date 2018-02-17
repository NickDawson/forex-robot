package com.trading.forex.model;

import com.trading.forex.common.model.Symbol;
import lombok.Builder;
import lombok.Data;

/**
 * Created by hsouidi on 07/31/2017.
 */
@Data
@Builder
public class InvestingTechIndicator {

    private IndicatorAction rsi;
    private IndicatorAction stoch;
    private IndicatorAction stochRsi;
    private IndicatorAction adx;
    private IndicatorAction macd;
    private IndicatorAction williamR;
    private IndicatorAction cci;
    private IndicatorAction atr;
    private IndicatorAction highLows;
    private IndicatorAction ultimateOscilator;
    private IndicatorAction roc;
    private IndicatorAction bullBearPower;
    private Symbol symbol;
    private Double buy;
    private Double sell;
    private Decision summary;
    private Double neutral;

    @Data
    @Builder
    public static class IndicatorAction{

        private Double value;
        private Decision action;
    }
}
