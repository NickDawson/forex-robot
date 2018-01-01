package com.trading.forex.configuration;

import com.oanda.v20.Context;
import com.oanda.v20.account.Account;
import com.oanda.v20.account.AccountID;
import com.trading.forex.exceptions.RobotTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by wf on 10/19/2017.
 */
@Configuration
public class OandaConfiguration {

    @Value( "${oanda.url}" )
    private String url;

    @Value( "${oanda.token}" )
    private String token;

    @Value( "${oanda.account-id}" )
    private String accountId;

    @Bean
    public Context context(){
        return new Context(url, token);
    }

    @Bean
    public AccountID accountID(){
        return new AccountID(accountId);
    }

    @Bean
    public Account account(){
        try {
            return context().account.get(accountID()).getAccount();
        } catch (Exception e) {
            throw new RobotTechnicalException(e);
        }
    }
}
