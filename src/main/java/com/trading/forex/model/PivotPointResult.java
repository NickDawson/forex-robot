package com.trading.forex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wf on 11/28/2017.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PivotPointResult {

    private Double r1;
    private Double r2;
    private Double r3;
    private Double s1;
    private Double s2;
    private Double s3;
    private Double pivot ;
    private Double pr1;
    private Double pr2;
    private Double pr3;
    private Double ps1;
    private Double ps2;
    private Double ps3;
}
