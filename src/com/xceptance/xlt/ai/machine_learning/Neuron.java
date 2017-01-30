// Catalano Neuro Library
// The Catalano Framework
//
// Copyright © Diego Catalano, 2015
// diego.catalano at live.com
//
// Copyright © Andrew Kirillov, 2007-2008
// andrew.kirillov at gmail.com
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

package com.xceptance.xlt.ai.machine_learning;

import com.xceptance.xlt.ai.core.FloatRange;

import java.io.Serializable;
import java.util.Random;

/**
 * Base neuron class.
 * @author Diego Catalano edited by Thomas Volkmann
 */
public class Neuron implements Serializable
{   
	/**
     * Neuron's inputs count.
     */
    protected int inputsCount = 0;
    
    /**
     * Neuron's weights.
     */
    protected double weight = 0;
    
    /**
     * Neuron's output value.
     */
    protected double output = 0;
    
    /**
     * Random number generator.
     */
    protected Random r = new Random();
    
    /**
     * Random generator range.
     * Sets the range of random generator. Affects initial values of neuron's weight. Default value is [0, 1].
     */
    protected static FloatRange range = new FloatRange(0.0f, 1.0f);

    /**
     * Get the range.
     * @return range FloatRagne value
     */
    public static FloatRange getRange()
    {
        return range;
    }

    /**
     * Set the range.
     * @param range FloatRange value
     */
    public static void setRange(FloatRange range) 
    {
        Neuron.range = range;
    }
    
    /**
     * Get Neuron's inputs count.
     * @return Neuron's inputs count.
     */
    public int getInputCount()
    {
        return inputsCount;
    }
    
    /**
     * Get Neuron's output value.
     * The calculation way of neuron's output value is determined by inherited class.
     * @return Neuron's output value.
     */
    public double getOutput()
    {
        return output;
    }
    
    /**
     * Get Neuron weight.
     * @return weights Double value of the weight.
     */
    public double getWeight()
    {
        return weight;
    }
    
    /**
     * Set Neuron weight.
     * @param weight Weights.
     */
    public void setWeight(double weight)
    {
        this.weight = weight;
    }
    
    /**
     * Initializes a new instance of the Neuron class.
     */
    public Neuron()
    {
        double d = range.length();
        weight = r.nextDouble( ) * d + range.getMin();
    }
    
    /**
     * Randomize neuron.
     * Initialize neuron's weights with random values within the range specified by Range.
     */
    public void Randomize()
    {
        double d = range.length();        
        weight = r.nextDouble( ) * d + range.getMin();                
    }  
    
    /**
	 * Auto generated serial number.
	 */
	private static final long serialVersionUID = 1L;

}