// Copyright 2017 Thomas Volkmann
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this
// software and associated documentation files (the "Software"), 
// to deal in the Software without restriction, including without limitation the rights 
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
// and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all 
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
// BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.xceptance.xlt.ai;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.xceptance.xlt.ai.machine_learning.ActivationNetwork;
import com.xceptance.xlt.ai.machine_learning.BipolarSigmoidFunction;
import com.xceptance.xlt.ai.pre_processing.ImageTransformation;
import com.xceptance.xlt.ai.machine_learning.PerceptronLearning;
import com.xceptance.xlt.ai.image.FastBitmap;
import com.xceptance.xlt.ai.image.PatternHelper;
import com.xceptance.xlt.ai.util.Constants;
import com.xceptance.xlt.ai.util.Helper;
import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.scripting.WebDriverCustomModule;
import com.xceptance.xlt.api.util.XltProperties;

/**
 * Module for the visual assertion of changes in a browser page. The module is called in an
 * action and takes a screenshot of the current page. This screenshot is then compared to already taken
 * reference images of the same page, or stored as reference image.
 *
 * The configurations for this module are done in the visualassertion.properties under /config
 * There are different algorithms that can be used for the comparison of the images and different ways to
 * visualize those differences for the evaluation.
 */
public class AI implements WebDriverCustomModule
{
    /**
     * Counter for the current screenshots
     */
    private static ThreadLocal<Integer> indexCounter = new ThreadLocal<>();

    private final String ALL = "all";
	private final String PREFIX = "com.xceptance.xlt.ai.";
    private final String RESULT_DIRECTORY = "results" + File.separator + "ai";

    // subdirectories
    private final String RESULT_DIRECTORY_TRAINING 			= "training";
    private final String RESULT_DIRECTORY_TRAINING_LEARN 	= "used for training";
    private final String RESULT_DIRECTORY_TRAINING_VALIDATE = "used for validation";
    private final String RESULT_DIRECTORY_NETWORKS 			= "networks";
    private final String RESULT_DIRECTORY_UNRECOGNIZED 		= "unrecognized";
    private final String RESULT_DIRECTORY_RECOGNIZED 		= "recognized";

    // the property names        
    public final String PROPERTY_ENABLED 					= PREFIX + "enabled";
    public final String PROPERTY_RESULT_DIRECTORY 			= PREFIX + "resultDirectory";
    public final String PROPERTY_ID 						= PREFIX + "ID";
    public final String PROPERTY_TESTCASE_BOUND				= PREFIX + "TESTCASE_BOUND";
    public final String PROPERTY_TESTCASE_NAME				= PREFIX + "TESTCASE_NAME";
    public final String PROPERTY_MODE						= PREFIX + "TRAINING";
    public final String PROPERTY_WAITING_TIME 				= PREFIX + "WAITINGTIME";
    public final String PROPERTY_USE_ORIGINAL_SIZE 			= PREFIX + "USE_ORIGINAL_SIZE";
    public final String PROPERTY_USE_COLOR_FOR_COMPARISON 	= PREFIX + "USE_COLOR_FOR_COMPARISON";
    public final String PROPERTY_LEARNING_RATE 				= PREFIX + "LEARNING_RATE"; 
    public final String PROPERTY_INTENDED_PERCENTAGE_MATCH 	= PREFIX + "INTENDED_PERCENTAGE_MATCH";
    public final String PROPERTY_PERCENTAGE_DIFFERENCE 		= PREFIX + "PERCENTAGE_DIFFERENCE";
    public final String PROPERTY_IMAGE_HEIGHT 				= PREFIX + "IMAGE_HEIGHT";
    public final String PROPERTY_IMAGE_WIDTH 				= PREFIX + "IMAGE_WIDTH";
    public final String PROPERTY_FORMAT 					= PREFIX + "FORMAT";

    @Override
    public void execute(final WebDriver webdriver, final String... arguments)
    {
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

        // Identification of the current environment for this test
        final String id = props.getProperty(PROPERTY_ID, ALL);
        // Parent directory of the visual assertion results
        final String resultDirectory = props.getProperty(PROPERTY_RESULT_DIRECTORY, RESULT_DIRECTORY);

        // Wait time for the page to load completely
        final int waitTime = props.getProperty(PROPERTY_WAITING_TIME, Constants.WAITINGTIME);
        
        Constants.TESTCASE_BOUND_NAME		= props.getProperty(PROPERTY_TESTCASE_NAME, Constants.TESTCASE_BOUND_NAME);
        Constants.TESTCASE_BOUND			= props.getProperty(PROPERTY_TESTCASE_BOUND, Constants.TESTCASE_BOUND);
        Constants.NETWORK_MODE				= props.getProperty(PROPERTY_MODE, Constants.NETWORK_MODE);
        Constants.IMAGE_HEIGHT 				= props.getProperty(PROPERTY_IMAGE_HEIGHT, Constants.IMAGE_HEIGHT);
        Constants.IMAGE_WIDTH 				= props.getProperty(PROPERTY_IMAGE_WIDTH, Constants.IMAGE_WIDTH);
        Constants.FORMAT 					= props.getProperty(PROPERTY_FORMAT, Constants.FORMAT);        
        Constants.USE_ORIGINAL_SIZE 		= props.getProperty(PROPERTY_USE_ORIGINAL_SIZE, Constants.USE_COLOR_FOR_COMPARISON);
        Constants.USE_COLOR_FOR_COMPARISON 	= props.getProperty(PROPERTY_USE_COLOR_FOR_COMPARISON, Constants.USE_COLOR_FOR_COMPARISON);
        Constants.LEARNING_RATE				= Double.valueOf(props.getProperty(PROPERTY_LEARNING_RATE, Double.toString(Constants.LEARNING_RATE)));
        Constants.INTENDED_PERCENTAGE_MATCH	= Double.valueOf(props.getProperty(PROPERTY_INTENDED_PERCENTAGE_MATCH, Double.toString(Constants.INTENDED_PERCENTAGE_MATCH)));
        Constants.PERCENTAGE_DIFFERENCE		= Double.valueOf(props.getProperty(PROPERTY_PERCENTAGE_DIFFERENCE, Double.toString(Constants.PERCENTAGE_DIFFERENCE)));

        //--------------------------------------------------------------------------------
        // Get the current environment
        //--------------------------------------------------------------------------------

        // Get the name of the test case for the correct folder identifier
        final String currentTestCaseName;
        if (Constants.TESTCASE_BOUND)
        {
        	currentTestCaseName = Session.getCurrent().getUserName();
        }
        else
        {
        	currentTestCaseName = Constants.TESTCASE_BOUND_NAME;
        }

        // Get browser name and browser version for the subfolders
        final String browserName = getBrowserName(webdriver);

        // Get the name of the action that called the visual verification
        final String currentActionName;
        if (arguments[0] == null)
        {
        	currentActionName = Session.getCurrent().getCurrentActionName();
        }
        else
        {
        	currentActionName = arguments[0];
        }
        
        //--------------------------------------------------------------------------------
        // Initialize the directory and file paths, create the directories if necessary
        //--------------------------------------------------------------------------------

        // Generate the child directories for the current environment in the parent result folder
        final File targetDirectory = new File(new File(new File(resultDirectory, id),
                currentTestCaseName),
                browserName);
                
        targetDirectory.mkdirs();

        // Retrieve current index counter for the image file names, only used internal
        Integer index = indexCounter.get();
        if (index == null)
        {
            index = 1;
        }
        else
        {
            index = index + 1;
        }
        // Update the index
        indexCounter.set(index);

        String screenshotName = "";
        
        // Name of the image file for the screenshot
        if (arguments[0] == null)
        {
        	screenshotName = currentActionName;
        }
        else
        {
        	// if the argument is not null, take the destination from the script and change everything according 
        	screenshotName = Helper.checkFolderForMatch(targetDirectory + File.separator + RESULT_DIRECTORY_TRAINING, currentActionName) + currentActionName;
        	Session.getCurrent().setID(Session.getCurrent().getID() + index );
        }
        
        // Directory for the training images
        final File trainingDirectory = new File(new File(targetDirectory, RESULT_DIRECTORY_TRAINING), screenshotName);
        final File trainingDirectory_uft = new File(trainingDirectory, RESULT_DIRECTORY_TRAINING_LEARN);
        final File trainingDirectory_val = new File(trainingDirectory, RESULT_DIRECTORY_TRAINING_VALIDATE);
        
        // Path of the screenshot image file
        final String exactScreenshotName = Session.getCurrent().getCurrentActionName()+ "_" + Session.getCurrent().getID() + "." + Constants.FORMAT;
        final File trainingScreenShotFile = new File(trainingDirectory_uft, exactScreenshotName);
        
        // Directory of the network file
        final File networkDirectoryPath = new File(targetDirectory, RESULT_DIRECTORY_NETWORKS);
        networkDirectoryPath.mkdirs();
        // Path of the network file
        final File networkFile = new File(networkDirectoryPath, screenshotName);

        //--------------------------------------------------------------------------------
        // Wait for the page to fully load, so that a correct screenshot can be taken
        //--------------------------------------------------------------------------------

        try
        {
            TimeUnit.MILLISECONDS.sleep(waitTime);
        }
        catch (final InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        
        //--------------------------------------------------------------------------------
        // Make the screenshot and load the network or create a new one
        //--------------------------------------------------------------------------------
        
        // initialization         
        final FastBitmap screenshot;
        ActivationNetwork an = new ActivationNetwork(new BipolarSigmoidFunction(), 1); 
        ImageTransformation im;
        ArrayList<FastBitmap> imgList = new ArrayList<>();  
        ArrayList<PatternHelper> patternList = new ArrayList<>();

        if (networkFile.exists())
        {
        	// load the corresponding network and all settings which are saved 
          	an = (ActivationNetwork) an.Load(networkFile.getPath()); 
          	an.setConstants();
            screenshot = new FastBitmap(takeScreenshot(webdriver), exactScreenshotName, Constants.USE_ORIGINAL_SIZE);
            
        	// TODO Has this to be handled in a different way?
            // webdriver cannot take the screenshot -> RETURN
            if (screenshot == null)
            {
            	System.exit(-1);
            }
           	
            // if the network is not done with training check the training folder for changes 
            // if there are changes, all unknwon images get loaded
           	imgList.add(screenshot);
           	if (Constants.NETWORK_MODE)
           	{
           		trainingDirectory.mkdirs();
           		trainingDirectory_uft.mkdir();
           		trainingDirectory_val.mkdir();
           		imgList.addAll(an.scanFolderForChanges(
           				trainingScreenShotFile.getParent(), 
           				exactScreenshotName));
           	}
            // transform the new screenshot
            im = new ImageTransformation(
               		imgList,                		
               		an.getAverageMetric(), 
               		Constants.NETWORK_MODE);                
            imgList = null;
        }
        else
        {  
        	trainingDirectory.mkdirs();
        	trainingDirectory_uft.mkdir();
        	trainingDirectory_val.mkdir();
        	screenshot = new FastBitmap(takeScreenshot(webdriver), exactScreenshotName, Constants.USE_ORIGINAL_SIZE);
        	
        	imgList.add(screenshot);
    		// TODO Has this to be handled in a different way?
    		// webdriver cannot take the screenshot -> RETURN
        	if (screenshot == null)
        	{
        		System.exit(-1);
        	}         	
        	
        	Constants.IMAGE_WIDTH = screenshot.getWidth();
        	Constants.IMAGE_HEIGHT = screenshot.getHeight();
        	
          	imgList.addAll(an.scanFolderForChanges(
          			trainingScreenShotFile.getParent(), 
           			exactScreenshotName));
          	
          	// load all images from the directory
            im = new ImageTransformation(imgList);
            imgList = null;
        }

        
        patternList = im.computeAverageMetric();
        
        // internal list in network for self testing and image confirmation   
        an.setInternalParameter(im.getAverageMetric());
        an.setInternalList(patternList);
                
    	PerceptronLearning pl = new PerceptronLearning(an);
    	pl.setLearningRate(Constants.LEARNING_RATE);	
    	
    	if (Constants.NETWORK_MODE)
    	{
			for (PatternHelper pattern : patternList)
			{
				pl.Run(pattern.getPatternList());
			}
    	}

    	ArrayList<FastBitmap> validationList = an.scanFolderForChanges(trainingDirectory_val.toString());		   	
    	ArrayList<PatternHelper> validationPatternList = new ArrayList<>();
		if (!validationList.isEmpty())
		{
			ImageTransformation imt = new ImageTransformation(validationList, an.getAverageMetric(), false);
			validationPatternList = imt.computeAverageMetric();
		}	
    	
		boolean selfTest = an.onSelfTest(validationPatternList, Constants.NETWORK_MODE);
		double result = 2.0;

		// ensure to get the last element in the list, which is always the current screenshot
		result = an.checkForRecognitionAsDouble(patternList.get(patternList.size() - 1).getPatternList());
		System.out.println("Recognition result: " + result);

		
		// console output
		if (selfTest)
		{
			System.out.println("Network not ready");
		}
			
		// Save the screenshot
		if (Constants.INTENDED_PERCENTAGE_MATCH > result && !Constants.NETWORK_MODE && !selfTest)
		{
			// Directory for the unrecognized images of the current test run
			final File unrecognizedInstanceDirectory = new File(new File(targetDirectory, RESULT_DIRECTORY_UNRECOGNIZED), screenshotName);
			// Path of the unrecognized image file
		    final File unrecognizedImageFile = new File(unrecognizedInstanceDirectory, exactScreenshotName);
		    unrecognizedImageFile.mkdirs();
			Helper.saveImage(screenshot.toBufferedImage(), unrecognizedImageFile);
			Assert.fail("Failure during visual image assertion:");
		}
		else if (Constants.INTENDED_PERCENTAGE_MATCH < result && !Constants.NETWORK_MODE && !selfTest)
		{
			final File recognizedInstanceDirectory = new File(new File(targetDirectory, RESULT_DIRECTORY_RECOGNIZED), screenshotName);
			final File recognizedImageFile = new File(recognizedInstanceDirectory, exactScreenshotName);
			recognizedInstanceDirectory.mkdirs();
			Helper.saveImage(screenshot.toBufferedImage(), recognizedImageFile);		
		}
		else
		{
			// Save the network
        	trainingDirectory.mkdirs();
        	trainingDirectory_uft.mkdir();
        	trainingDirectory_val.mkdir();
        	an.setInternalParameter(im.getAverageMetric());
			an.Save(networkFile.toString());
			Helper.saveImage(screenshot.toBufferedImage(), trainingScreenShotFile);				
		}
    }
    
    /**
     * Takes a screenshot if the underlying web driver instance is capable of doing it. Fails with a message only in
     * case the webdriver cannot take screenshots. Avoids issue when certain drivers are used.
     * 
     * @param webDriver
     *            the web driver to use
     * @return {@link BufferedImage} if the webdriver supports taking screenshots, null otherwise
     * @throws RuntimeException
     *             In case the files cannot be written
     */
    private BufferedImage takeScreenshot(final WebDriver webDriver)
    {
        if (webDriver instanceof TakesScreenshot)
        {
            final byte[] bytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            try
            {
                return ImageIO.read(new ByteArrayInputStream(bytes));
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns the browser name using Selenium methods
     * 
     * @param webDriver
     *            the WebDriver to query
     * @return the browser name
     */
    private String getBrowserName(final WebDriver webDriver)
    {
        final Capabilities capabilities = ((RemoteWebDriver) webDriver).getCapabilities();
        final String browserName = capabilities.getBrowserName();

        return browserName == null ? "unknown" : browserName;
    }
}
