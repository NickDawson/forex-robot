package com.trading.forex.oanda.service;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.CandlestickData;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.primitives.DateTime;
import com.oanda.v20.primitives.InstrumentName;
import com.trading.forex.common.exceptions.RobotTechnicalException;
import com.trading.forex.common.model.Candle;
import com.trading.forex.common.model.CandlestickGranularity;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.utils.CustomList;
import com.trading.forex.connector.exceptions.ConnectorTechnicalException;
import com.trading.forex.connector.service.InstrumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Created by wf on 10/21/2017.
 */
@Service
@Slf4j
public class OandaInstrumentServiceImpl implements InstrumentService {

    @Autowired
    private Context context;

    private static String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'";

    @Override
    public CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol, int count) {
        try {
            InstrumentCandlesResponse instrumentCandlesResponse = getPricing(candlestickGranularity, symbol.getBrokerValue(), null,null,count);
            return instrumentCandlesResponse.getCandles().stream().map(candlestick -> toCandle(candlestick,symbol)).collect(Collectors.toCollection(CustomList::new));
        } catch (Exception e) {
            throw new RobotTechnicalException(e);
        }
    }


    public static Candle toCandle(Candlestick candlestick, Symbol symbol) {
        final CandlestickData candlestickData = candlestick.getMid();
        return Candle.builder()
                .open(candlestickData.getO().doubleValue())
                .close(candlestickData.getC().doubleValue())
                .high(candlestickData.getH().doubleValue())
                .low(candlestickData.getL().doubleValue())
                .volume(candlestick.getVolume())
                .epoch(toEpoch(candlestick.getTime()))
                .symbol(symbol)
                .build();
    }

    private static Long toEpoch(DateTime dateTime) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT")));
        try {
            return simpleDateFormat.parse(dateTime.toString()).getTime();
        } catch (ParseException e) {
            throw new ConnectorTechnicalException(e);
        }
    }

    @Override
    public CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol, Date to, Date from) {
        try {
            log.info("Retrieving princing for CandlestickGranularity {}, Symbol {} , Date {} , Date {}",candlestickGranularity, symbol,to, from);
            InstrumentCandlesResponse instrumentCandlesResponse = getPricing(candlestickGranularity, symbol.getBrokerValue(), toDateTime(to),toDateTime(from),null);
            return instrumentCandlesResponse.getCandles().stream().map(candlestick -> toCandle(candlestick,symbol)).collect(Collectors.toCollection(CustomList::new));
        } catch (Exception e) {
            log.info("Error when retrieving princing for CandlestickGranularity {}, Symbol {} , Date {} , Date {}",candlestickGranularity, symbol,to, from);
            log.error(e.getMessage(),e);
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
        instrumentCandlesRequest.setGranularity(com.oanda.v20.instrument.CandlestickGranularity.valueOf(candlestickGranularity.name()));
        if (to != null && from != null) {
            instrumentCandlesRequest.setTo(to.toString());
            instrumentCandlesRequest.setFrom(from.toString());
        } else {
            instrumentCandlesRequest.setCount(count);
        }
        return context.instrument.candles(instrumentCandlesRequest);
    }
}
