package com.trading.forex.service;

import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.utils.CustomList;
import com.trading.forex.entity.EconomicCalendar;
import com.trading.forex.model.Duration;
import com.trading.forex.model.Importance;
import com.trading.forex.model.InvestingDataGroup;
import com.trading.forex.model.InvestingTechIndicator;

import java.util.Map;

/**
 * Created by hsouidi on 04/19/2017.
 */
public interface IndicatorService {


    CustomList<EconomicCalendar> getEconomicCalendarData(Importance importanceFilter);

    InvestingDataGroup expertDecision(Symbol symbol);

    Map<Symbol, InvestingTechIndicator> expertDecision(Duration duration);

    InvestingTechIndicator expertDecision(Symbol symbol, Duration duration);
}
