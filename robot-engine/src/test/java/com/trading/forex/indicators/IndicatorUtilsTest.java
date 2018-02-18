package com.trading.forex.indicators;

import com.trading.forex.common.model.Candle;
import com.trading.forex.common.utils.CustomList;
import com.trading.forex.model.SearchResult;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;

/**
 * Created by hsouidi on 12/16/2017.
 */
public class IndicatorUtilsTest {


    @Test
    public void testGetHigh() throws Exception {
        // Given
        CustomList<Candle> candles = new CustomList<>(Arrays.asList(
                Candle.builder()
                .low(16.08)
                .high(16.86)
                .close(16.15)
                .build(),
                Candle.builder()
                .low(16.18)
                .high(16.96)
                .close(16.25)
                .build(),
                Candle.builder()
                .low(16.28)
                .high(17.16)
                .close(16.35)
                .build(),
                Candle.builder()
                .low(16.38)
                .high(17.26)
                .close(16.45)
                .build()));
        // When
        SearchResult high=IndicatorUtils.getHigh(candles,6);
        // Then
        assertEquals(high.getValue(),17.26);

    }

    @Test
    public void testGetLow() throws Exception {
        // Given
        CustomList<Candle> candles = new CustomList<>( Arrays.asList(
                Candle.builder()
                        .low(16.08)
                        .high(16.86)
                        .close(16.15)
                        .build(),
                Candle.builder()
                        .low(16.18)
                        .high(16.96)
                        .close(16.25)
                        .build(),
                Candle.builder()
                        .low(16.28)
                        .high(17.16)
                        .close(16.35)
                        .build(),
                Candle.builder()
                        .low(16.38)
                        .high(17.26)
                        .close(16.45)
                        .build()));
        // When
        SearchResult low=IndicatorUtils.getLow(candles,3);
        // Then
        assertEquals(low.getValue(),16.18);

    }

}