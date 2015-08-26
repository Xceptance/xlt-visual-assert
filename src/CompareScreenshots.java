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
		
		//Checks if this is a new testcase. If yes, (re)sets index. If not. increments index
		if ( (x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.currentID") == Session.getCurrent().getID()) ) {
			String indexString = x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.index");
			int indexInt = Integer.parseInt(indexString);
			indexInt++;
			indexString = String.valueOf(indexInt);
			x.setProperty("com.xceptance.xlt.loadtests.TestCaseCP.index", indexString);
		}
		
		else { 
			String currentID = Session.getCurrent().getID(); 
			x.setProperty("com.xceptance.xlt.loadtests.TestCaseCP.currentID", currentID);
			x.setProperty("com.xceptance.xlt.loadtests.TestCaseCP.index", "1");
		}
		
		
		//Get testcasename for the correct folder
		String currentTestCaseName = Session.getCurrent().getUserID();
		
		//Get browsername for the correct subfolder
        String currentActionName = Session.getCurrent().getWebDriverActionName();
        String browserName = getBrowserName(webDriver);
                	
    	//Get path to the directory
        String directory = x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.directoryToScreenshots");
        directory = directory + "/" + currentTestCaseName + "/" + browserName;
        

		//Set name of the referenceImage       
        String indexS = x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.index");
        String screenshotName = indexS + "-" + currentActionName;
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
        }
        
        //If there is another screenshot ...
        else {
        	//Create temporary file for the new screenshot
        	File screenshotFile = new File(directory + "new-screenshot" + screenshotName + ".png");
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
            	//Initialize referenceImage, screenshotImage, delete screenshotImageFile
            	BufferedImage screenshot = ImageIO.read(screenshotFile);
            	BufferedImage reference = ImageIO.read(referenceFile);
            	screenshotFile.delete();
            	
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
        	throw new RuntimeIOException();
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