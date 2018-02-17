package com.trading.forex.connector.service;


import com.trading.forex.common.model.Candle;
import com.trading.forex.common.model.CandlestickGranularity;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.utils.CustomList;

import java.util.Date;

/**
 * Created by hsouidi on 10/21/2017.
 */
public interface InstrumentService {

    CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol, int count);

    CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol, Date to, Date from);

}
