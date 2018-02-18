package com.trading.forex.indicators.impl;

import com.trading.forex.common.model.Candle;
import com.trading.forex.model.PivotPointResult;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by hsouidi on 11/28/2017.
 */
public class PivotPointTest {
    @Test
    public void testCalcul() throws Exception {

        // Given
        Candle candle = Candle.builder()
                .low(16.08)
                .high(16.86)
                .close(16.15)
                .build();
        // When
        PivotPointResult pivotPointResult=PivotPoint.calcul(candle);
        // Then
        assertEquals(pivotPointResult.getS1(),15.866666666666667);
        assertEquals(pivotPointResult.getS2(),15.583333333333332);
        assertEquals(pivotPointResult.getS3(),15.086666666666666);

        assertEquals(pivotPointResult.getR1(),16.64666666666667);
        assertEquals(pivotPointResult.getR2(),17.143333333333334);
        assertEquals(pivotPointResult.getR3(),17.42666666666667);
    }

}