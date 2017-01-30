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

/**
 * The network trainer is a tool for creating networks. A network is the result from loading and analyzing images. 
 * The analyzing is the learning of the structure within the images. 
 * The network trainer need three parameters to work:
 * 1. path to folder where the images are saved
 * 2. path to properties file (by default under git_folder/xlt-vissual-asser/config/ai.properties)
 * 3. optional network name, could also be changed afterwards
 * The network trainer learn every images under the first argument using the parameters in the properties file.
 * After processing all images the network trainer test himself, with all images and deliver a percentage recognized value for each image.
 * The network is saved under NetworkTrainer.results.
 * @author Thomas Volkmann
 *
 */
public class NetworkTrainer 
{
    // args[0] = path to the learning folder
    // args[1] = path to properties file
	// args[2] = optional network name 
	/**
	 * Entry point for running the network trainer. 
	 * @param args String array first two arguments are mandatory, third argument is optional.
	 */
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
        final File networkDirectoryPath = new File("NetworkTrainer.result");
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
    	
    	double resultVerfication = 0.0;	 
    	for (PatternHelper pattern : patternList)
		{
			pl.Run(pattern.getPatternList());
			resultVerfication += an.getLayer().computeSum(pattern.getPatternList());
		}
    	
    	System.out.println("Selftest value summed: " + (resultVerfication / patternList.size()));     	
    	
    	for (PatternHelper pattern : patternList)
    	{
    		System.out.println("Recognized value of image " + pattern.getTagName() + " = " + an.checkForRecognitionAsString(pattern.getPatternList()) + " %");
    	}
    	
		an.Save(networkFile.toString(), im.getAverageMetric());
	}
}
