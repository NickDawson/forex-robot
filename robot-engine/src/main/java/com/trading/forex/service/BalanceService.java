package com.trading.forex.service;

/**
 * Created by hsouidi on 10/20/2017.
 */
public interface BalanceService {

    Double getSolde();

    Double getMaxloss();

    Double getMaxProfit();

    void updateBalanceInfos();

    void reset();
}
