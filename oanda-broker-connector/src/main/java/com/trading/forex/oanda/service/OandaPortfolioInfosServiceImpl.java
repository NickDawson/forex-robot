package com.trading.forex.oanda.service;

import com.oanda.v20.Context;
import com.oanda.v20.ExecuteException;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.trading.forex.connector.exceptions.ConnectorTechnicalException;
import com.trading.forex.connector.service.PortfolioInfosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OandaPortfolioInfosServiceImpl implements PortfolioInfosService{



    @Autowired
    private Context context;

    @Autowired
    private AccountID accountID;


    @Override
    public Double getBalance() {
        try {
            return context.account.get(accountID).getAccount().getBalance().doubleValue();
        } catch (RequestException | ExecuteException e) {
            throw new ConnectorTechnicalException(e);
        }
    }
}
