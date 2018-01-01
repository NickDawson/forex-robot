package com.trading.forex.service;

import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Created by wf on 10/20/2017.
 */
public interface BalanceService {

    Double getSolde();

    Double getMaxloss();

    Double getMaxProfit();

    @Scheduled(fixedDelay = 10000)
    void updateBalanceInfos() throws ExecuteException, RequestException;

    void reset();
}
