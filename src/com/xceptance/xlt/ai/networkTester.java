package com.xceptance.xlt.ai;

import java.io.IOException;
import java.util.ArrayList;
import com.xceptance.xlt.ai.image.FastBitmap;
import com.xceptance.xlt.ai.image.PatternHelper;
import com.xceptance.xlt.ai.machine_learning.ActivationNetwork;
import com.xceptance.xlt.ai.machine_learning.BipolarSigmoidFunction;
import com.xceptance.xlt.ai.machine_learning.PerceptronLearning;
import com.xceptance.xlt.ai.pre_processing.ImageTransformation;
import com.xceptance.xlt.ai.util.Constants;
import com.xceptance.xlt.ai.util.Helper;

public class networkTester 
{

	// args[0] = network location
	// args[1] = folder to check the images
	public static void main(String[] args) 
	{
		Constants.NETWORK_MODE = true;
		
		ActivationNetwork an = new ActivationNetwork(new BipolarSigmoidFunction(), 1); 
        ImageTransformation im;
        ArrayList<PatternHelper> patternList = new ArrayList<>();
        ArrayList<FastBitmap> imgList = new ArrayList<>(); 
		
        if (args.length == 0)
        {
        	System.out.println("No parameter given.");
        	return;
        }
        	
        if (args.length > 0)
        {
          	an = (ActivationNetwork) an.Load(args[0]); 
          	an.setConstants();
          	if (args.length >= 1)
          	{
          		imgList = an.scanFolderForChanges(args[1]);
          		im = new ImageTransformation(imgList, an.getAverageMetric(), false);
          		patternList = im.computeAverageMetric();
          		
            	PerceptronLearning pl = new PerceptronLearning(an);
            	pl.setLearningRate(Constants.LEARNING_RATE);
            	
            	for (PatternHelper pattern : patternList)
            	{
            		System.out.println("Recognized value of image " + pattern.getTagName() + " = " + an.checkForRecognitionAsString(pattern.getPatternList()) + " %");
            	}
          	}
        }
	}

}
