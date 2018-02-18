package com.trading.forex.common.utils;

import com.trading.forex.common.model.CandlestickGranularity;
import com.trading.forex.common.model.Symbol;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class AlgoUtilsTest {

    @Test
    public void testToPip() {

        // Given
        // When
        final double pip=AlgoUtils.toPip(Symbol.USD_CHF,0.00028D);
        // Then
        assertEquals(pip,2.8D);

    }

    @Test
    public void testNomrmalize() {

        // Given
        // When
        final double normalize=AlgoUtils.normalize(1.322033D,Symbol.GBP_JPY);
        // Then
        assertEquals(normalize,1.322D);

    }

    @Test
    public void testCandlestickGranularityToTimeStamp() {
        // Given
        // When
        long timestamp=AlgoUtils.candlestickGranularityToTimeStamp(CandlestickGranularity.M);
        // Then
        assertEquals(timestamp,2678400000L);

    }
}
