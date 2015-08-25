import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.io.RuntimeIOException;
import org.junit.Assert;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;	

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.scripting.WebDriverCustomModule;
import com.xceptance.xlt.api.util.XltProperties;

/**
 *
 */
public class CompareScreenshots implements WebDriverCustomModule
{
    /**
     * {@inheritDoc}
     */

	@Override
	public void execute(WebDriver webDriver, String... args) 
    {
		XltProperties x = XltProperties.getInstance();
		
		//Get browsername for the name of the image
        String currentActionName = Session.getCurrent().getWebDriverActionName();
        String browserName = getBrowserName(webDriver);
        
		//Get browsername for the name of the image
        String currentTestCaseName = Session.getCurrent().getUserID();
        
        //Get index for name of he image
    	String indexS = x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.index");
    	int    indexI = Integer.parseInt(indexS);
    	
    	//Get path to the directory
        String directory = x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.directoryToScreenshots");
        directory = directory + "/" + currentTestCaseName + "/" + browserName;
        
        //Set name of the referenceImage
        String screenshotName = currentActionName + "-" + indexS;
        String referencePath = directory + "/" + screenshotName;
        
        File referenceFile = new File(referencePath + ".png");
             
        //If there's no other screenshot, just save the new one
        if (!referenceFile.isFile()) {
            try
            {
                takeScreenshot(webDriver, referenceFile);
            }
            catch (IOException e)
            {
                throw new RuntimeIOException();
            }
        	System.out.println("Set new reference Screenshot");
        }
        
        //If there is another screenshot ...
        else {
        	//Create temporary file for the new screenshot
        	File screenshotFile = new File(directory + "new-screenshot" + screenshotName + ".png");
        	screenshotFile.deleteOnExit(); 			
            try
            {
                takeScreenshot(webDriver, screenshotFile);
            }
            catch (IOException e)
            {
            	throw new RuntimeIOException();
            }
           
            //Get fuzzyness properties and convert them from String to integer/double
            int pixelPerBlockX = Integer.parseInt(x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.pixelPerBlockX"));
            int pixelPerBlockY = Integer.parseInt(x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.pixelPerBlockY"));
            double threshold = Double.parseDouble(x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.threshold"));   
            
            try {
            	//Initialize referenceImage, screenshotImage 
            	BufferedImage screenshot = ImageIO.read(screenshotFile);
            	BufferedImage reference = ImageIO.read(referenceFile);
            	
            	//Initialize markedImageFile and maskImageFile
            	new File(directory + "/marked/").mkdirs();
            	String markedImagePath = directory + "/marked/" + screenshotName + "-marked" + ".png"; 
            	File markedImageFile = new File(markedImagePath);
            	
              	new File(directory + "/mask/").mkdirs();
            	String maskImagePath = directory + "/mask/" + screenshotName + "-mask" + ".png"; 
            	File maskImageFile = new File(maskImagePath);
            	
            	//Initializes boolean variable for training mode
            	String trainingModeString = x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.trainingMode");
            	Boolean trainingMode = Boolean.parseBoolean(trainingModeString);
            	

            	ImageComparison imagecomparison = new ImageComparison(pixelPerBlockX, pixelPerBlockY, threshold, trainingMode);
            	if (!imagecomparison.fuzzyEqual(reference, screenshot, maskImageFile, markedImageFile)) {
            		
//            		Give an assertion. The marked Image was saved in the fuzzyEqual method
            		String assertMessage = "Layout changed" + currentActionName + "-i" + indexS;
            		Assert.assertTrue(assertMessage, false);			
            	}
            }
            catch (IOException e) {
            	throw new RuntimeIOException();
            }
        }
        //Count up index and save it
        indexI++;
        indexS = Integer.toString(indexI);
        x.setProperty("com.xceptance.xlt.loadtests.TestCaseCP.index", indexS);
    }

    /**
     * Takes a screenshot if the underlying web driver instance is capable of doing it.
     * 
     * @throws IOException
     */
    private void takeScreenshot(final WebDriver webDriver, final File pngFile) throws IOException
    {
        if (webDriver instanceof TakesScreenshot)
        {
            byte[] bytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            FileUtils.writeByteArrayToFile(pngFile, bytes);
        }
        else
        {
        	System.out.println("Webdriver isn't an instance of TakesScreenshot");
        }
    }    
    
	/**
	 * Gets and returns the browsername using Selenium methods 
	 * 
	 * 
	 * @param webdriver
	 * @return
	 */
	private String getBrowserName(WebDriver webdriver) {			
		Capabilities capabilities = ((RemoteWebDriver) webdriver).getCapabilities();
		String browserName = capabilities.getBrowserName();
		return browserName;
	}		
}