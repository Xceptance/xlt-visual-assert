import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.scripting.WebDriverCustomModule;

/**
 *
 */
public class CompareScreenshots implements WebDriverCustomModule
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(WebDriver webDriver, String... parameters)
    {
        String currentActionName = Session.getCurrent().getWebDriverActionName();
    	String browserName = getBrowserName(webDriver);
    	String referencePath = "./" + browserName + "/" + currentActionName;  
        File referenceFile = new File(referencePath + ".png");
        if (!referenceFile.isFile()) {
            try
            {
                takeScreenshot(webDriver, referenceFile);
            }
            catch (IOException e)
            {
                // TODO:
            }
        	System.out.println("Set new reference Screenshot");
        }
        else {
        	File screenshotFile = new File(referencePath + "-new-screenshot" + ".png");
            try
            {
                takeScreenshot(webDriver, screenshotFile);
                System.out.println("Found reference screenshot");
            }
            catch (IOException e)
            {
                // TODO:
            }
            //Convert fuzzyness Parameters from String to integer/ double
            int pixelPerBlockX = Integer.parseInt(parameters[0]);
            int pixelPerBlockY = Integer.parseInt(parameters[1]);
            double threshold = Double.parseDouble(parameters[2]);   
            try {
            	BufferedImage screenshot = ImageIO.read(screenshotFile);
            	BufferedImage reference = ImageIO.read(referenceFile);
            	reference = overwriteTransparentPixels(reference, screenshot);
            	//The following is rudimentary, the ImageComparison class should be integrated
            	ImageComparison imagecomparison = new ImageComparison(pixelPerBlockX, pixelPerBlockY, threshold);
            	if (imagecomparison.fuzzyEqual(reference, screenshot, referencePath + "marked" + ".png")) {						
            		System.out.println("Layout didn't change");
            	}
            	else {
            		//print marked Image
            		File fileMarkedImage = new File(referencePath + "marked" + ".png");
            		openFile(fileMarkedImage);
            		System.out.println("Layout did change");
            	}
            }
            catch (Exception e) {
				// TODO: handle exception
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
            // TODO: What to do here?
        }
    }    
    
    /**
     * Opens a File if he Desktop API is supported.
     * 
     * @throws IOException
     */
	private  void openFile(File file) throws IOException {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.OPEN)); {
				desktop.open(file);
			}
		}
	}
	
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
	private String getBrowserName(WebDriver webdriver) {				//New
		Capabilities capabilities = ((RemoteWebDriver) webdriver).getCapabilities();
		String browserName = capabilities.getBrowserName();
		return browserName;
	}
}









