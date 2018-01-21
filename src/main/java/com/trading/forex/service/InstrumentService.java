package com.trading.forex.service;

import com.oanda.v20.RequestException;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.pricing.PricingGetResponse;
import com.trading.forex.model.Candle;
import com.trading.forex.model.Symbol;
import com.trading.forex.utils.CustomList;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.Date;

/**
 * Created by hsouidi on 10/21/2017.
 */
public interface InstrumentService {

    CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol,int count);

    @Retryable(
            value = { RuntimeException.class,RequestException.class },
            maxAttempts = 4,
            backoff = @Backoff(delay = 5000))
    CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol, Date to, Date from);

    PricingGetResponse getInstrumentInfos(Symbol symbol);

    Price getCurrentPrice(Symbol symbol);
}
