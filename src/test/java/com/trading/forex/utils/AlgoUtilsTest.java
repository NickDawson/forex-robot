package com.trading.forex.utils;

import com.oanda.v20.instrument.CandlestickGranularity;
import com.trading.forex.model.Symbol;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class AlgoUtilsTest {

    @Test
    public void testToPip() throws Exception {

        // Given
        // When
        final double pip=AlgoUtils.toPip(Symbol.USD_CHF,0.00028D);
        // Then
        assertEquals(pip,2.8D);

    }

    @Test
    public void testNomrmalize() throws Exception {

        // Given
        // When
        final double normalize=AlgoUtils.normalize(1.322033D,Symbol.GBP_JPY);
        // Then
        assertEquals(normalize,1.322D);

    }

    @Test
    public void testCandlestickGranularityToTimeStamp() throws Exception {
        // Given
        // When
        long timestamp=AlgoUtils.candlestickGranularityToTimeStamp(CandlestickGranularity.M);
        // Then
        assertEquals(timestamp,2678400000L);

    }
}