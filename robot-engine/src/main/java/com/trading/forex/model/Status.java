package com.trading.forex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by hsouidi on 07/11/2017.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Status {

    private Boolean serverStatus;
    private Boolean inverse;
    private Double solde;
    private Double maxProfit;
    private Double maxLoss;
    private String mode;
    private String strategy;
    private Boolean limit;
    private Integer nbOpenedTransaction;
    private Double payout ;
    private Boolean coverage;
    private Double  positionProfit;
}
