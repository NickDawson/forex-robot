package com.trading.forex.model;

import com.trading.forex.common.model.Symbol;
import lombok.Builder;
import lombok.Data;

/**
 * Created by hsouidi on 07/10/2017.
 */
@Data
@Builder
public class InvestingDataGroup {

    private InvestingData movingAverage;
    private InvestingData technicalIndicator;
    private InvestingData summary;
    private Symbol symbol;
}
