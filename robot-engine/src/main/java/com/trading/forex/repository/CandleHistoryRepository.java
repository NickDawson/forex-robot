package com.trading.forex.repository;

import com.trading.forex.common.model.CandlestickGranularity;
import com.trading.forex.common.model.Symbol;
import com.trading.forex.entity.CandleEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by hsouidi on 05/09/2017.
 */
public interface CandleHistoryRepository extends CrudRepository<CandleEntity, CandleEntity.Key> {

    List<CandleEntity> findAllByKeyEpochBetweenAndKeySymbolAndKeyCandlestickGranularityOrderByKeyEpoch(long epochFrom, long epochTo, Symbol symbol, CandlestickGranularity candlestickGranularity);

}
