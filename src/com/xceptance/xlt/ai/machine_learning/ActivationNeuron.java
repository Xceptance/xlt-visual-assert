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

import java.util.ArrayList;

/**
 * Activation neuron.
 * @author Diego Catalano edited by Thomas Volkmann
 */
public class ActivationNeuron extends Neuron
{    
    /**
	 * Auto generated serial number.
	 */
	private static final long serialVersionUID = 1L;

	protected double threshold = 0.0;
    
    protected IActivationFunction function = null;
    
    private ArrayList<Neuron> neurons;

    /**
     * Get Threshold value.
     * The value is added to inputs weighted sum before it is passed to activation function.
     * @return Threshold value.
     */
    public double getThreshold() 
    {
        return threshold;
    }

    public ArrayList<Neuron> getNeurons()
    {
    	return neurons;
    }
    
    /**
     * Set Threshold value.
     * The value is added to inputs weighted sum before it is passed to activation function.
     * @param threshold Threshold value.
     */
    public void setThreshold(double threshold) 
    {
        this.threshold = threshold;
    }
    
    /**
     * Get Neuron's activation function.
     * @return Neuron's activation function.
     */
    public IActivationFunction getActivationFunction()
    {
        return function;
    }
    
    /**
     * Set Neuron's activation function.
     * @param function Neuron's activation function.
     */
    public void setActivationFunction(IActivationFunction function)
    {
        this.function = function;
    }

    /**
     * Initializes a new instance of the ActivationNeuron class.
     * @param function Neuron's activation function.
     */
    public ActivationNeuron(IActivationFunction function) 
    {
        super();
        this.function = function;
        neurons = new ArrayList<>();
    }
    
    @Override
    public void Randomize()
    {
        super.Randomize();
        this.threshold = r.nextDouble() * (range.length()) + range.getMin();
    }

    public double Compute(int input, int index) 
    {
        // initial sum value
        double sum = 0.0;

        sum = neurons.get(index).getWeight() * input;
        sum += threshold;

        // local variable to avoid mutlithreaded conflicts
        double output = function.Function( sum );
        // assign output property as well (works correctly for single threaded usage)
        this.output = output;

        return output;
    }
}