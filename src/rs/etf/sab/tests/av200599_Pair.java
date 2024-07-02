/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rs.etf.sab.tests;

import java.math.BigDecimal;
import rs.etf.sab.operations.PackageOperations;

/**
 *
 * @author Byk
 */
public class av200599_Pair implements PackageOperations.Pair<Integer, BigDecimal>
{

    private final Integer firstParam;
    private final BigDecimal secondParam;

    public av200599_Pair(Integer firstParam, BigDecimal secondParam) 
    {
        this.firstParam = firstParam;
        this.secondParam = secondParam;
    }

    @Override
    public Integer getFirstParam() {
        return firstParam;
    }

    @Override
    public BigDecimal getSecondParam() {
        return secondParam;
    }

    
}
