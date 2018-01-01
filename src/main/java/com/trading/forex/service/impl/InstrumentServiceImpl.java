package com.trading.forex.service.impl;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.primitives.InstrumentName;
import com.trading.forex.exceptions.RobotTechnicalException;
import com.trading.forex.model.Candle;
import com.trading.forex.model.Symbol;
import com.trading.forex.service.InstrumentService;
import com.trading.forex.utils.CustomList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Created by wf on 10/21/2017.
 */
@Service
@Slf4j
public class InstrumentServiceImpl implements InstrumentService {

    @Autowired
    private Context context;

    @Autowired
    private AccountID accountID;

    private static String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'";

    public InstrumentServiceImpl(){
        super();
    }

    public InstrumentServiceImpl(Context context, AccountID accountID) {
        this.context = context;
        this.accountID = accountID;
    }


    @Override
    public CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol,int count) {
        try {
            InstrumentCandlesResponse instrumentCandlesResponse = getPricing(candlestickGranularity, symbol.getBrokerValue(), null,null,count);
            return instrumentCandlesResponse.getCandles().stream().map(candlestick -> Candle.toCandle(candlestick,symbol)).collect(Collectors.toCollection(CustomList::new));
        } catch (Exception e) {
            throw new RobotTechnicalException(e);
        }
    }

    @Override
    public CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol, Date to, Date from) {
        try {
            log.info("Retrieving princing for CandlestickGranularity {}, Symbol {} , Date {} , Date {}",candlestickGranularity, symbol,to, from);
            InstrumentCandlesResponse instrumentCandlesResponse = getPricing(candlestickGranularity, symbol.getBrokerValue(), toDateTime(to),toDateTime(from),null);
            return instrumentCandlesResponse.getCandles().stream().map(candlestick -> Candle.toCandle(candlestick,symbol)).collect(Collectors.toCollection(CustomList::new));
        } catch (Exception e) {
            log.info("Error when retrieving princing for CandlestickGranularity {}, Symbol {} , Date {} , Date {}",candlestickGranularity, symbol,to, from);
            throw new RobotTechnicalException(e);
        }
    }


    private DateTime toDateTime(Date dateTime) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return new DateTime(simpleDateFormat.format(dateTime));
    }

    private InstrumentCandlesResponse getPricing(CandlestickGranularity candlestickGranularity, String instrumentName, DateTime to, DateTime from,Integer count) throws ExecuteException, RequestException {
        final InstrumentCandlesRequest instrumentCandlesRequest = new InstrumentCandlesRequest(new InstrumentName(instrumentName));
        instrumentCandlesRequest.setGranularity(candlestickGranularity);
        if (to != null && from != null) {
            instrumentCandlesRequest.setTo(to.toString());
            instrumentCandlesRequest.setFrom(from.toString());
        } else {
            instrumentCandlesRequest.setCount(count);
        }
        return context.instrument.candles(instrumentCandlesRequest);
    }

    @Override
    public PricingGetResponse getInstrumentInfos(Symbol symbol) {
        final PricingGetRequest pricingGetRequest = new PricingGetRequest(accountID, Arrays.asList(symbol.getBrokerValue()));
        try {
            return context.pricing.get(pricingGetRequest);
        } catch (Exception e) {
            throw new RobotTechnicalException(e);
        }
    }

    @Override
    public Price getCurrentPrice(Symbol symbol) {

        PricingGetRequest request = new PricingGetRequest(accountID, Arrays.asList(symbol.getBrokerValue()));
        PricingGetResponse resp = null;
        try {
            resp = context.pricing.get(request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RobotTechnicalException(e);
        }
        return resp.getPrices().get(0);
    }
}
