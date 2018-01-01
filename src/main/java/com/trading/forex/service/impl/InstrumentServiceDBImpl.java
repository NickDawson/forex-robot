package com.trading.forex.service.impl;

import com.oanda.v20.Context;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.pricing.Price;
import com.oanda.v20.pricing.PricingGetResponse;
import com.trading.forex.entity.CandleEntity;
import com.trading.forex.model.Candle;
import com.trading.forex.model.Symbol;
import com.trading.forex.repository.CandleHistoryRepository;
import com.trading.forex.service.InstrumentService;
import com.trading.forex.utils.CustomList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.stream.Collectors;

import static com.trading.forex.utils.AlgoUtils.candlestickGranularityToTimeStamp;
import static com.trading.forex.utils.AlgoUtils.checkDateWithCandlestickGranularity;

/**
 * Created by wf on 10/21/2017.
 */
@Service
@Slf4j
public class InstrumentServiceDBImpl implements InstrumentService {

    @Autowired
    private CandleHistoryRepository candleHistoryRepository;

    @Autowired
    private Context context;

    @Autowired
    private AccountID accountID;

    private InstrumentService instrumentService;

    @PostConstruct
    public void init() {
        instrumentService = new InstrumentServiceImpl(context, accountID);

    }

    @Override
    public CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol, int count) {
        return null;
    }

    @Override
    public CustomList<Candle> getPricing(CandlestickGranularity candlestickGranularity, Symbol symbol, Date to, Date from) {
        final CustomList<Candle> candlesFromDb = CandleEntity.toCandle(candleHistoryRepository.findAllByKeyEpochBetweenAndKeySymbolAndKeyCandlestickGranularityOrderByKeyEpoch(
                from.getTime()-candlestickGranularityToTimeStamp(candlestickGranularity), to.getTime(), symbol, candlestickGranularity));
        if (!candlesFromDb.isEmpty() &&checkDateWithCandlestickGranularity(candlestickGranularity,candlesFromDb.getLast().date(),to)) {

            return candlesFromDb;
        } else {
            // download & Push candles to cache
            CustomList<Candle> candles = instrumentService.getPricing(candlestickGranularity, symbol, to, from);
            candleHistoryRepository.save(candles.stream().map(candle1 -> CandleEntity.build(candle1, candlestickGranularity, symbol))
                    .collect(Collectors.toList()));
            candleHistoryRepository.findOne(candles.stream()
                    .map(candle1 -> CandleEntity.build(candle1, candlestickGranularity, symbol)).collect(Collectors.toList()).get(0).getKey());
            return candles;

        }
    }

    @Override
    public PricingGetResponse getInstrumentInfos(Symbol symbol) {
        return null;
    }

    @Override
    public Price getCurrentPrice(Symbol symbol) {
        return null;
    }


}
