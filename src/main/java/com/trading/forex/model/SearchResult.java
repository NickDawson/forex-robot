package com.trading.forex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by wf on 12/17/2017.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResult {

    private double value;
    private int index;
}
