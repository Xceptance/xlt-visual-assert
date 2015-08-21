import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
// import org.openqa.selenium.Capabilities; 	Incompatible with Xlt
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
// import org.openqa.selenium.remote.RemoteWebDriver;	Incompatible with Xlt

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
        String browserName = "Firefox";
        
        //Get index for name of he image
    	String indexS = x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.index");
    	int    indexI = Integer.parseInt(indexS);
    	
    	//Get path to the directory
        String directory = x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.directoryToScreenshots");
        String referencePath = directory + "/" + browserName + "/" + currentActionName + indexS;
        File referenceFile = new File(referencePath + ".png");
        
        //Count up index and save it
        indexI++;
        indexS = Integer.toString(indexI);
        
        //If there's no other screenshot, just save the new one
        if (!referenceFile.isFile()) {
            try
            {
                takeScreenshot(webDriver, referenceFile);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        	System.out.println("Set new reference Screenshot");
  //  		System.out.println("referenceFile absolute path: " + referenceFile.getAbsolutePath()); 	//Debug
        }
        
        //If there is another screenshot ...
        else {
        	File screenshotFile = new File(referencePath + "-new-screenshot" + ".png");
            try
            {
                takeScreenshot(webDriver, screenshotFile);
                System.out.println("Found reference screenshot");
  //      		System.out.println("sceenshotFile absolute path: " + screenshotFile.getAbsolutePath());	//Debug
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            //Get fuzzyness properties and convert them from String to integer/double
            
            int pixelPerBlockX = Integer.parseInt(x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.pixelPerBlockX"));
            int pixelPerBlockY = Integer.parseInt(x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.pixelPerBlockY"));
            double threshold = Double.parseDouble(x.getProperty("com.xceptance.xlt.loadtests.TestCaseCP.threshold"));   
            //Compare screenshots, core algorithm
            try {
            	BufferedImage screenshot = ImageIO.read(screenshotFile);
            	BufferedImage reference = ImageIO.read(referenceFile);
            	reference = overwriteTransparentPixels(reference, screenshot);
            	ImageComparison imagecomparison = new ImageComparison(pixelPerBlockX, pixelPerBlockY, threshold);
            	if (imagecomparison.fuzzyEqual(reference, screenshot, referencePath + "marked" + ".png")) {						
            		System.out.println("Layout didn't change");
            	}
            	else {
            		//print marked Image
  //          		File fileMarkedImage = new File(referencePath + "marked" + ".png");							//Debug
 //           		System.out.println("fileMarkedImage absolute path: " + fileMarkedImage.getAbsolutePath());	//Debug
            		System.out.println("Layout did change");
            	}
            }
            catch (Exception e) {
				e.printStackTrace();
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
        	System.out.println("Webdriver isn't an instance of TakesScreenshot");
        }
    }    
    
    /**
     * Opens a File if he Desktop API is supported. Isn't used at the moment
     * 
     * @throws IOException
     */
//	private  void openFile(File file) throws IOException {
//		if (Desktop.isDesktopSupported()) {
//			Desktop desktop = Desktop.getDesktop();
//			if (desktop.isSupported(Desktop.Action.OPEN)); {
//				desktop.open(file);
//			}
//		}
//	}
//	
    /**
     * Overwrites all transparent pixels in the reference image with corresponding pixels of the screenshot image.
     * Pseudotransparency
     * 
     * @throws IOException
     */
	private BufferedImage overwriteTransparentPixels(BufferedImage reference, BufferedImage screenshot) {
		for (int w=0; w<reference.getWidth(); w++) {
			for (int h=0; h<reference.getHeight(); h++) {
				int alpha = (reference.getRGB(w, h) >> 24) & 0xFF;
				if (alpha == 0) {
					int rgb = screenshot.getRGB(w, h);
					reference.setRGB(w, h, rgb);
				}
			}
		}
		return reference;
	}
	
//	private String getBrowserName(WebDriver webdriver) {				Incombatability with Xceptance?
//		Capabilities capabilities = ((RemoteWebDriver) webdriver).getCapabilities();
//		String browserName = capabilities.getBrowserName();
//		return browserName;
//	}		
}









