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

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Base neural layer class.
 * @author Diego Catalano edited by Thomas Volkmann
 */
public class Layer implements Serializable
{    
	/**
     * Layer's inputs count.
     */
    public int inputsCount;
   
    /**
     * Layer's neurons.
     */
    protected ActivationNeuron activeNeuron;
    
    /**
     * Layer's output vector.
     */
    protected ArrayList<Double> output;

    /**
     * Singleton pattern for the Layer.
     * @param inputCount Size of the input.
     * @return Layer Instance of the Layer.
     */
    public static Layer getInstance(int inputCount)
    {
    	if (Layer.instance == null)
    	{
    		Layer.instance  = new Layer(inputCount);
    	}
    	return Layer.instance;
    }
    
    /**
     * Get Neurons from layer.
     * @return Neurons.
     */
    public ActivationNeuron getNeurons()
    {
        return activeNeuron;
    }
    
    /***
     * Get specified neuron from Layer.
     * @return Neuron.
     */
    public ActivationNeuron getActivationNeuron()
    {
        return activeNeuron;
    }
    
    /**
     * Set the {@link ActivationNeuron}.
     * @param actNeuron {@link ActivationNeuron}.
     */
    public void setActivationNeuron(ActivationNeuron actNeuron)
    {
    	activeNeuron = actNeuron;
    }

    /**
     * Get Layer's output vector.
     * @return Layer's output vector.
     */
    public ArrayList<Double> getOutput() 
    {
        return output;
    }

    /**
     * Initializes a new instance of the Layer class.
     * @param inputCount Layer's inputs count.
     */
    private Layer(int inputCount)
    {
        this.inputsCount = Math.max( 1, inputsCount);
    }
    
    /**
     * Compute output vector of the Layer.
     * @param input Input vector.
     * @return Returns layer's output vector.
     */
    public ArrayList<Double> Compute(ArrayList<Integer> input)
    {
        // local variable to avoid mutlithread conflicts
    	ArrayList<Double> output = new ArrayList<>();

        // compute each neuron
        for (int i = 0; i < input.size(); i++)
        {
            output.add(activeNeuron.Compute(input.get(i), i));
        }        
        return output;
    }
    
    /**
     * Compute output vector of the layer.
     * @param input Input vector.
     * @return double layer's output vector summarized.
     */
    public double computeSum(ArrayList<Integer> input)
    {
    	double result = 0.0;
    	int hits = 0;
    	
    	int size = input.size() < activeNeuron.getNeurons().size() ? input.size() : activeNeuron.getNeurons().size();
    	
        // compute each neuron
        for (int i = 0; i < size; i++)
        {
        	double value = 0.0;
        	value = activeNeuron.getNeurons().get(i).getWeight() * input.get(i);
        	hits += value <= 0 ? 0 : 1;
            result += value;
        }

        // norming of the value
        result += activeNeuron.getThreshold();
        // / size original
        result = result / (size - hits);
        result = activeNeuron.function.Function(result);
        
        return result;
    }
    
    /**
	 * Auto generated serial number.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Instance of {@link Layer}.
	 */
    private static Layer instance;
	
}