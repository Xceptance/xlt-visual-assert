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
 * Supervised learning interface.
 * @author Diego Catalano edited by Thomas Volkmann
 */
public interface ISupervisedLearning 
{
    /**
     * Runs learning iteration.
     * @param input Input vector.
     * @return Returns learning error.
     */
    double Run(ArrayList<Integer> input);
    
    /**
     * Runs learning epoch.
     * Epoch = In training a neural net, the term epoch is used to describe a complete pass through all of the training patterns. 
     * The weights in the neural net may be updated after each pattern is presented to the net, or they may be updated just once at the end of the epoch. 
     * Frequently used as a measure of speed of learning - as in "training was complete after x epochs".
     * @param input Array of input vectors.
     * @param output Array of output vectors.
     * @return Returns sum of learning errors.
     */
    double RunEpoch(ArrayList<ArrayList<Integer>> input, ArrayList<ArrayList<Double>> output);
}