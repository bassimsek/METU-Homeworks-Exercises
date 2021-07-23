package com.ceng495.hw2.util;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class Precision {

    public double twoPrecision(double input ) {

        BigDecimal bd = new BigDecimal(input).setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
