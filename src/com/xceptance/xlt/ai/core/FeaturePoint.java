// Copyright © Diego Catalano, 2012-2016
// diego.catalano at live.com
//
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//
package com.xceptance.xlt.ai.core;

import java.io.Serializable;
import java.util.Comparator;

import com.xceptance.xlt.ai.core.IntPoint;
import com.xceptance.xlt.ai.corner.FastCornersDetector;

/**
 * Feature Point class. 
 * @author Diego Catalano edited by Thomas Volkmann
 */
public class FeaturePoint implements Comparable<FeaturePoint>, Comparator<FeaturePoint>, Serializable
{

    /**
	 * Auto generated serial number.
	 */
	private static final long serialVersionUID = 1L;

	/**
     * X axis coordinate.
     */
    public int x;
    
    /**
     * Y axis coordinate.
     */
    public int y;
    
    /**
     * Representation of the intensity of the pixel, which is considered by the FAST - Algorithm {@link FastCornersDetector}.
     */
    public int score;
    
    /**
     * Current node state, for grouping of FeaturePoints, indicate that the node was visited or not.
     */
    public boolean visited;

    /**
     * Initializes a new instance of the FeaturePoint class.
     */
    public FeaturePoint() 
    {    	
    }

    /**
     * Initializes a new instance of the FeaturePoint class.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     */
    public FeaturePoint(int x, int y)
    {
        this.x = x;
        this.y = y;
        this.visited = false;
    }

    /**
     * Initializes a new instance of the FeaturePoint class.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     * @param score Score.
     */
    public FeaturePoint(int x, int y, int score) 
    {
        this.x = x;
        this.y = y;
        this.score = score;
        this.visited = false;
    }
    
    /**
     * Initializes a new instance of the FeaturePoint class.
     * @param x X axis coordinate.
     * @param y Y axis coordinate.
     * @param score Score.
     * @param value.
     */
    public FeaturePoint(int x, int y, int score, boolean value) 
    {
        this.x = x;
        this.y = y;
        this.score = score;
        this.visited = value;
    }

    /**
     * Convert to IntPoint.
     * @return IntPoint.
     */
    public IntPoint toIntPoint()
    {
        return new IntPoint(x, y);
    }  
    
    /***
     * Compare function for sorting and grouping
     */    
    public int compare(FeaturePoint fp1, FeaturePoint fp2)
    { 
    	Integer x1 = fp1.x;
    	Integer x2 = fp2.x;
    	
    	int sComp = x1.compareTo(x2);
    	if (sComp == 0)
    	{
    		Integer y1 = fp1.y;
    		Integer y2 = fp2.y;
    		sComp = y1.compareTo(y2);
    	}
    	return sComp;
    	
//    	if ()
//    	
//    	return fp1.x - fp2.x;
    }

    @Override
    public int compareTo(FeaturePoint o) 
    {
        if (o.score < this.score) return 1;
        else if (o.score == this.score) return 0;
        else return -1;
    }
}