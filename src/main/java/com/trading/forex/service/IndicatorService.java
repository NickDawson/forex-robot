package com.trading.forex.service;

import com.trading.forex.entity.EconomicCalendar;
import com.trading.forex.model.*;
import com.trading.forex.utils.CustomList;

import java.util.Map;

/**
 * Created by wf on 04/19/2017.
 */
public interface IndicatorService {


    CustomList<EconomicCalendar> getEconomicCalendarData(Importance importanceFilter);

    InvestingDataGroup expertDecision(Symbol symbol);

    Map<Symbol, InvestingTechIndicator> expertDecision(Duration duration);

    InvestingTechIndicator expertDecision(Symbol symbol, Duration duration);
}
