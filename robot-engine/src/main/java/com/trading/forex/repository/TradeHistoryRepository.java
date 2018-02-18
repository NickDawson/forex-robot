package com.trading.forex.repository;

import com.trading.forex.entity.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * Created by hsouidi on 05/09/2017.
 */
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, String> {

    List<TradeHistory>  findByResultIsNull();
    List<TradeHistory>  findByTradeDateBetween (Date start, Date end);

}
