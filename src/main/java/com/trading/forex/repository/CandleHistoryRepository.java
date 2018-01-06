package com.trading.forex.repository;

import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.entity.CandleEntity;
import com.trading.forex.model.Symbol;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by wf on 05/09/2017.
 */
public interface CandleHistoryRepository extends CrudRepository<CandleEntity, CandleEntity.Key> {

    List<CandleEntity> findAllByKeyEpochBetweenAndKeySymbolAndKeyCandlestickGranularityOrderByKeyEpoch(long epochFrom,long epochTo, Symbol symbol, CandlestickGranularity candlestickGranularity);

}
