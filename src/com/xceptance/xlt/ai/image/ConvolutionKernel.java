// Copyright © Diego Catalano, 2012-2016
// diego.catalano at live.com
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
package com.xceptance.xlt.ai.image;

/**
 * Kernels for convolution filter.
 * @author Diego Catalano edited by Thomas Volkmann
 */
public final class ConvolutionKernel
{
    /**
     * Don't let anyone instantiate this class.
     */
    private ConvolutionKernel() 
    {    	
    }

    /**
     *  Edge Detection.
     * -1 -1 -1
     * <br>-1 8 -1
     * <br>-1 -1 -1
     */
    public static final int[][] Laplacian = 
    	{
    		{-1, -1, -1},
        	{-1,  8, -1},
        	{-1, -1, -1}
        };

    /**
     * Vertical edge detection.
     * -1 0 1
     * <br>-2 0 2
     * <br>-1 0 -1
     */
    public static final int[][] SobelVertical = 
    	{
    		{-1,  0,  1},
        	{-2,  0,  2},
        	{-1,  0,  1}
        };

    /**
     * Horizontal edge detection. 
     * -1 -2 -1
     * <br>0 0 0
     * <br>1 2 1
     */
    public static final int[][] SobelHorizontal = 
    	{
    		{-1, -2, -1},
    		{ 0,  0,  0},
    		{ 1,  2,  1}
        };
    
    /**
     * Edge Detection.
     * -1 0 1
     * <br>0 0 0
     * <br>1 0 -1
     */
    public static final int[][] SobelCross3x3 = 
    	{
    		{-1,  0,  1},
        	{ 0,  0,  0},
        	{ 1,  0, -1}
        };
    
    /**
     * Edge Detection with bigger impact.
     * -1 -2 0 2 1
     * <br>-2 -4 0 4 0
     * <br>0 0 0 0 0
     * <br>2 4 0 -4 -2
     * <br>1 2 0 -2 -1
     */
    public static final int[][] SobelCross5x5 = 
    	{
    		{-1, -2,  0,  2,  1},
    		{-2, -4,  0,  4,  0},
        	{ 0,  0,  0,  0,  0},
        	{ 2,  4,  0, -4, -2},
        	{ 1,  2,  0, -2, -1}
        };
    
    /**
     * 1 2 1
     * <br>-2 -4 -2
     * <br>1 2 1
     */
    public static final int[][] SobelHorizontalSD3x3 = 
    	{
    		{ 1,  2,  1},
    		{-2, -4, -2},
    		{ 1,  2,  1}
        };
    
    /**
     * 1 4 6 4 1
     * <br>0 0 0 0 0
     * <br>-2 -8 -12 -8 -2
     * <br>0 0 0 0 0
     * <br>1 4 6 4 1
     */
    public static final int[][] SobelHorizontalSD5x5 = 
    	{
    		{ 1,  4,  6,   4,  1},
    		{ 0,  0,  0,   0,  0},
    		{-2, -8, -12, -8, -2},
    		{ 0,  0,  0,   0,  0},
    		{ 1,  4,  6,   4,  1}
        };
    
    /**
     * 1 -2 1
     * <br>2 -4 2
     * <br>1 -2 1
     */
    public static final int[][] SobelVerticalSD3x3 = 
    	{
    		{ 1, -2,  1},
        	{ 2, -4,  2},
        	{ 1, -2,  1}
        };
    
    /**
     * 1 0 -2 0 1
     * <br>4 0 -8 0 4
     * <br>6 0 -12 0 6
     * <br>4 0 -8 0 4
     * <br>1 0 -2 0 1
     */
    public static final int[][] SobelVerticalSD5x5 = 
    	{
    		{ 1,  0, -2,   0,  1},
        	{ 4,  0, -8,   0,  4},
        	{ 6,  0, -12,  0,  6},
        	{ 4,  0, -8,   0,  4},
        	{ 1,  0, -2,   0,  1}
        };

    /**
     * 1 0 -1
     * <br>1 0 -1
     * <br>1 0 -1
     */
    public static final int[][] PrewittHorizontal = 
    	{
    		{ 1,  0, -1},
        	{ 1,  0, -1},
        	{ 1,  0, -1 }
        };

    /**
     * 1 1 1
     * <br>0 0 0
     * <br>-1 -1 -1
     */
    public static final int[][] PrewittVertical= 
    	{
    		{ 1,  1,  1},
    		{ 0,  0,  0},
    		{-1, -1, -1 }
    	};

    /**
     * 1 0
     * <br>0 -1
     */
    public static final int[][] RobertsHorizontal = 
    {
   		{ 1,  0},
   		{ 0, -1}
   	};

    /**
     * 0 1
     * <br>-1 0
     */
    public static final int[][] RobertsVertical = 
    {
        { 0,  1},
        {-1,  0}
    };
    
    /**
     * 3 10 3
     * <br>0 0 0
     * <br>-3 -10 -3
     */
    public static final int[][] ScharrHorizontal = 
    {
        { 3,  10,  3},
        { 0,   0,  0},
        {-3, -10, -3}
    };
    
    /**
     * 3 0 -3
     * <br>10 0 -10
     * <br>3 0 -3
     */
    public static final int[][] ScharrVertical = 
    {
        {  3,  0,  -3},
        { 10,  0, -10},
        {  3,  0,  -3}
    };
    
    /**
     * 0 0 -1 0 0
     * <br>0 -1 -2 -1 0
     * <br>-1 -2 16 -2 -1
     * <br>0 -1 -2 -1 0
     * <br>0 0 -1 0 0
     */
    public static final int[][] LaplacianOfGaussian = 
    {
        { 0,  0, -1,  0,  0},
        { 0, -1, -2, -1,  0},
        {-1, -2, 16, -2, -1},
        { 0, -1, -2, -1,  0},
        { 0,  0, -1,  0,  0}
    };
    
    /**
     * Smooth Noise Robust (Horizontal).
     * This mask use 1/32 conv(mask) as weight.
     * Combines isotropic noise suppression and precise gradient estimation.
     * References: http://www.holoborodko.com/pavel/image-processing/edge-detection/
     */
    public static final int[][] SmoothNoiseRobust_Horizontal = 
    {
        {-1, -2, -1},
        {-2, -4, -2},
        { 0,  0,  0},
        { 2,  4,  2},
        { 1,  2,  1}
    };
    
    /**
     * Smooth Noise Robust (Vertical).
     * This mask use 1/32 conv(mask) as weight.
     * Combines isotropic noise suppression and precise gradient estimation.
     * References: http://www.holoborodko.com/pavel/image-processing/edge-detection/
     */
    public static final int[][] SmoothNoiseRobust_Vertical = 
    {
        {-1, -2, 0, 2, 1},
        {-2, -4, 0, 4, 2},
        {-1, -2, 0, 2, 1}
    };
}