package com.trading.forex;

import com.trading.forex.common.model.Candle;
import com.trading.forex.common.model.CandlestickGranularity;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.common.utils.CustomList;
import com.trading.forex.connector.service.InstrumentService;
import com.trading.forex.entity.CandleEntity;
import com.trading.forex.oanda.service.OandaInstrumentServiceImpl;
import com.trading.forex.repository.CandleHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.stream.Collectors;

import static com.trading.forex.common.utils.AlgoUtils.candlestickGranularityToTimeStamp;
import static com.trading.forex.common.utils.AlgoUtils.checkDateWithCandlestickGranularity;

/**
 * Created by wf on 10/21/2017.
 */
@Service
@Slf4j
public class InstrumentServiceDBImpl implements InstrumentService {

    @Autowired
    private CandleHistoryRepository candleHistoryRepository;

    @Autowired
    private OandaInstrumentServiceImpl oandaInstrumentService;


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
            CustomList<Candle> candles = oandaInstrumentService.getPricing(candlestickGranularity, symbol, to, from);
            candleHistoryRepository.save(candles.stream().map(candle1 -> CandleEntity.build(candle1, candlestickGranularity, symbol))
                    .collect(Collectors.toList()));
            candleHistoryRepository.findOne(candles.stream()
                    .map(candle1 -> CandleEntity.build(candle1, candlestickGranularity, symbol)).collect(Collectors.toList()).get(0).getKey());
            return candles;

        }
    }

}
