package com.xceptance.xlt.visualassertion.algorithm;

public class ColorFuzzy extends ComparisonAlgorithm
{
    public ColorFuzzy(double colorTolerance)
    {
        super(ComparisonType.COLORFUZZY, 0, colorTolerance, 0);
    }

    public ColorFuzzy()
    {
        super(ComparisonType.COLORFUZZY, 0, 0.1, 0);
    }
}
