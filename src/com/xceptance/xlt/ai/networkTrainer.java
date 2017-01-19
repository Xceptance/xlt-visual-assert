package com.xceptance.xlt.ai;

import java.io.File;
import java.util.ArrayList;

import com.xceptance.xlt.ai.image.PatternHelper;
import com.xceptance.xlt.ai.machine_learning.ActivationNetwork;
import com.xceptance.xlt.ai.machine_learning.BipolarSigmoidFunction;
import com.xceptance.xlt.ai.machine_learning.PerceptronLearning;
import com.xceptance.xlt.ai.pre_processing.ImageTransformation;
import com.xceptance.xlt.ai.util.Constants;
import com.xceptance.xlt.api.util.XltProperties;

public class networkTrainer 
{
	private static final String PREFIX = "com.xceptance.xlt.ai.";
 
    // args[0] = Path to the learning folder
    // args[1] = Action Name
	public static void main(String[] args) 
	{		
	    // the property names        
		final String PROPERTY_ENABLED 					= PREFIX + "enabled";
		final String PROPERTY_RESULT_DIRECTORY 			= "networkTrainer.result";
		final String PROPERTY_TESTCASE_BOUND			= PREFIX + "TESTCASE_BOUND";
		final String PROPERTY_TESTCASE_NAME				= PREFIX + "TESTCASE_NAME";
		final String PROPERTY_MODE						= PREFIX + "TRAINING";
		final String PROPERTY_USE_ORIGINAL_SIZE 		= PREFIX + "USE_ORIGINAL_SIZE";
		final String PROPERTY_USE_COLOR_FOR_COMPARISON 	= PREFIX + "USE_COLOR_FOR_COMPARISON";
		final String PROPERTY_LEARNING_RATE 			= PREFIX + "LEARNING_RATE"; 
		final String PROPERTY_INTENDED_PERCENTAGE_MATCH = PREFIX + "INTENDED_PERCENTAGE_MATCH";
		final String PROPERTY_PERCENTAGE_DIFFERENCE 	= PREFIX + "PERCENTAGE_DIFFERENCE";
		final String PROPERTY_IMAGE_HEIGHT 				= PREFIX + "IMAGE_HEIGHT";
		final String PROPERTY_IMAGE_WIDTH 				= PREFIX + "IMAGE_WIDTH";
		final String PROPERTY_FORMAT 					= PREFIX + "FORMAT";
		
		final XltProperties props = XltProperties.getInstance();
	    // check if we have to do anything?
	    final boolean enabled = props.getProperty(PROPERTY_ENABLED, true);
	    if (!enabled)
	    {
	    	// skipped silently
	    	return;
	    }	
	    
        //--------------------------------------------------------------------------------
        // Get Properties and convert them from String if necessary
        //--------------------------------------------------------------------------------

        final int percentageDifferenceValue = props.getProperty(PROPERTY_PERCENTAGE_DIFFERENCE, Constants.PERCENTAGE_DIFFERENCE);
        
        Constants.TESTCASE_BOUND_NAME		= props.getProperty(PROPERTY_TESTCASE_NAME, Constants.TESTCASE_BOUND_NAME);
        Constants.TESTCASE_BOUND			= props.getProperty(PROPERTY_TESTCASE_BOUND, Constants.TESTCASE_BOUND);
        Constants.NETWORK_MODE				= props.getProperty(PROPERTY_MODE, Constants.NETWORK_MODE);
        Constants.IMAGE_HEIGHT 				= props.getProperty(PROPERTY_IMAGE_HEIGHT, Constants.IMAGE_HEIGHT);
        Constants.IMAGE_WIDTH 				= props.getProperty(PROPERTY_IMAGE_WIDTH, Constants.IMAGE_WIDTH);
        Constants.FORMAT 					= props.getProperty(PROPERTY_FORMAT, Constants.FORMAT);        
        Constants.USE_ORIGINAL_SIZE 		= props.getProperty(PROPERTY_USE_ORIGINAL_SIZE, Constants.USE_COLOR_FOR_COMPARISON);
        Constants.USE_COLOR_FOR_COMPARISON 	= props.getProperty(PROPERTY_USE_COLOR_FOR_COMPARISON, Constants.USE_COLOR_FOR_COMPARISON);
        
        final String learningRateValue = props.getProperty(PROPERTY_LEARNING_RATE, Constants.LEARNING_RATE);
        final double learningRate = Double.parseDouble(learningRateValue);
        
        final String indentedPercentageMatchValue = props.getProperty(PROPERTY_INTENDED_PERCENTAGE_MATCH, Constants.INTENDED_PERCENTAGE_MATCH);
        final double indentedPercentageMatch = Double.parseDouble(indentedPercentageMatchValue);
        String networkName = "";
        
        // Directory of the network file
        final File networkDirectoryPath = new File(PROPERTY_RESULT_DIRECTORY, "");
        networkDirectoryPath.mkdirs();
        // Path of the network file
        
        if (args.length == 0)
        {
        	System.out.println("No directory path was given as Parameter 1");        	   
        	return;
        }
        
        if (args.length == 2)
       	{
       		networkName = args[1];
       	}
       	else
       	{
       		networkName = "unnamed";
       	}
        
        final File networkFile = new File(networkDirectoryPath, networkName);

        // initialization        
        ActivationNetwork an = new ActivationNetwork(new BipolarSigmoidFunction(), 1); 
        ImageTransformation im;
        ArrayList<PatternHelper> patternList = new ArrayList<>();
        
        im = new ImageTransformation(args[0]);
        
        patternList = im.computeAverageMetric(percentageDifferenceValue);
        // internal list in network for self testing and image confirmation        
        an.setInternalList(patternList);            
    	PerceptronLearning pl = new PerceptronLearning(an, learningRate);
    	pl.setLearningRate(learningRate);
    	for (PatternHelper pattern : patternList)
		{
			pl.Run(pattern.getPatternList());
		}
    	
    	an.onSelfTest(indentedPercentageMatch, "");	
		an.Save(networkFile.toString(), im.getAverageMetric());
	}
}
