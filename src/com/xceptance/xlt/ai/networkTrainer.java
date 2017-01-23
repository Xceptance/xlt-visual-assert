package com.xceptance.xlt.ai;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.xceptance.xlt.ai.image.PatternHelper;
import com.xceptance.xlt.ai.machine_learning.ActivationNetwork;
import com.xceptance.xlt.ai.machine_learning.BipolarSigmoidFunction;
import com.xceptance.xlt.ai.machine_learning.PerceptronLearning;
import com.xceptance.xlt.ai.pre_processing.ImageTransformation;
import com.xceptance.xlt.ai.util.Constants;
import com.xceptance.xlt.ai.util.Helper;

public class networkTrainer 
{
    // args[0] = path to the learning folder
    // args[1] = path to properties file
	// args[2] = optional network name 
	public static void main(String[] args) 
	{	
        //--------------------------------------------------------------------------------
        // Get Properties and convert them from String if necessary
        //--------------------------------------------------------------------------------
  
		try 
		{
			Helper.readProperties(args[1]);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

        String networkName = "";
        // Directory of the network file
        final File networkDirectoryPath = new File("networkTrainer.result");
        networkDirectoryPath.mkdirs();
        
        // Path to working folder
        if (args.length == 0)
        {
        	System.out.println("No directory path was given as Parameter 1");        	   
        	return;
        }
        
        networkName = (args.length == 3 ? args[2] : "unnamed");
        
        final File networkFile = new File(networkDirectoryPath, networkName);

        // initialization        
        ActivationNetwork an = new ActivationNetwork(new BipolarSigmoidFunction(), 1); 
        ImageTransformation im;
        ArrayList<PatternHelper> patternList = new ArrayList<>();
        
        im = new ImageTransformation(args[0]);
        
        patternList = im.computeAverageMetric();
        // internal list in network for self testing and image confirmation        
        an.setInternalList(patternList);            
    	PerceptronLearning pl = new PerceptronLearning(an);
    	pl.setLearningRate(Constants.LEARNING_RATE);
    	for (PatternHelper pattern : patternList)
		{
			pl.Run(pattern.getPatternList());
		}
    	
    	an.onSelfTest(new ArrayList<PatternHelper>(), true);	
		an.Save(networkFile.toString(), im.getAverageMetric());
	}
}
