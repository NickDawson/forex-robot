package com.trading.forex.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompositeTrade {

    private Trade master;
    private Trade hedge;

    public boolean checkConsistancy(){

        return master.checkConsistancy()&&hedge.checkConsistancy();
    }
}
