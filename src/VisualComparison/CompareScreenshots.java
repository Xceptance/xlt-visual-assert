package VisualComparison;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
 * A java module for the visual comparison of websites implementing the
 * WebDriverCustomModule. For the comparison itself, the ImageComparison class
 * is used. The java module is written for use in an Xlt test case. Selenium
 * APIs were also used.
 * 
 * @author lucas & damian
 */
public class CompareScreenshots implements WebDriverCustomModule {
	final String PPREFIX = "com.xceptance.xlt.imageComparison";
	final String DWAITTIME = "100";
	final String DPIXELPBLOCKX = "20";
	final String DPIXELPBLOCKY = "20";
	final String TDIFFERENCES = "0.05";

	/**
	 * No parameters beyond the webdriver are required. For properties, look at
	 * the bottom.
	 * <p>
	 * Screenshots are saved in subfolders with the same name as the testcase.
	 * There are different sub- folders for different browsers. The screenshots
	 * are named by the actionname and an index which counts up from one in
	 * every testcase. Thereby, screenshots are never compared to screenshots
	 * made within the same test run, only previously made screenshots made with
	 * the same browser, in the same testcase, action and position.
	 * <p>
	 * In the first test run, folders for the test case and browser will be
	 * created and reference screenshots will be saved. In the second test run,
	 * sub folders for mask images and marked images will be created and
	 * comparisons will be made.
	 * <p>
	 * If difference between an image and it's reference image are detected,
	 * they will be marked and the resulting marked image will be saved in the
	 * marked image folder; the method will fail due to an assertion. Without
	 * manual intervention, reference images are never changed. The taken
	 * screenshots are also never saved as such.
	 * <p>
	 * If the trainingMode property is true, differences will not be marked, but
	 * the corresponding areas in the mask image will be painted black. So
	 * barring manual intervention, differences in these areas will not be
	 * detected in later runs.
	 * <p>
	 * Necessary properties are as follows:
	 * com.xceptance.xlt.imageComparison.directoryToScreenshots - the directory
	 * in which the screenshots will be saved
	 * com.xceptance.xlt.imageComparison.pixelPerBlockX - fuzzyness parameter,
	 * see ImageComparison. com.xceptance.xlt.imageComparison.pixelPerBlockY -
	 * fuzzyness parameter, see ImageComparison.
	 * com.xceptance.xlt.imageComparison.toleratedDifferences - fuzzyness parameter, see
	 * ImageComparison. com.xceptance.xlt.imageComparison.toleratedDifferences.trainingMode
	 * - See ImageComparison
	 * com.xceptance.xlt.imageComparison.toleratedDifferences.trainingMode.currentID
	 * <p>
	 * {@inheritDoc}
	 */

	@Override
	public void execute(WebDriver webDriver, String... args) {

		XltProperties x = XltProperties.getInstance();

		// Wait a few miliseconds so the website is fully loaded
		String waitTimeS = x.getProperty(PPREFIX + ".waitTime", DWAITTIME);
		int waitTime = Integer.parseInt(waitTimeS);
		try {
			TimeUnit.MILLISECONDS.sleep(waitTime);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Checks if this is a new testcase. If yes, (re)sets index. If not.
		// increments index
		if ((x.getProperty(PPREFIX + ".currentID") == Session.getCurrent()
				.getID())) {
			String indexString = x.getProperty(PPREFIX + ".index");
			int indexInt = Integer.parseInt(indexString);
			indexInt++;
			indexString = String.valueOf(indexInt);
			x.setProperty(PPREFIX + ".index", indexString);
		}

		else {
			String currentID = Session.getCurrent().getID();
			x.setProperty(PPREFIX + ".currentID", currentID);
			x.setProperty(PPREFIX + ".index", "1");
		}

		// Get testcasename for the correct folder
		String currentTestCaseName = Session.getCurrent().getUserID();

		// Get browsername for the correct subfolder
		String currentActionName = Session.getCurrent()
				.getWebDriverActionName();
		String browserName = getBrowserName(webDriver);

		// Get path to the directory
		String directory = x.getProperty(PPREFIX + ".directoryToScreenshots");
		directory = directory + "/" + currentTestCaseName + "/" + browserName;

		// Set name of the referenceImage
		String indexS = x.getProperty(PPREFIX + ".index");
		String screenshotName = indexS + "-" + currentActionName;
		String referencePath = directory + "/" + screenshotName;

		File referenceFile = new File(referencePath + ".png");

		// If there's no other screenshot, just save the new one
		if (!referenceFile.isFile()) {
			try {
				takeScreenshot(webDriver, referenceFile);
			} catch (IOException e) {
				throw new RuntimeIOException();
			}
		}

		// If there is another screenshot ...
		else {
			// Create temporary file for the new screenshot
			File screenshotFile = new File(directory + "new-screenshot"
					+ screenshotName + ".png");
			try {
				takeScreenshot(webDriver, screenshotFile);
			} catch (IOException e) {
				throw new RuntimeIOException();
			}

			// Get fuzzyness properties and convert them from String to
			// integer/double
			String pixelPerBlockXS = x.getProperty(PPREFIX + ".pixelPerBlockX", DPIXELPBLOCKX);
			int pixelPerBlockX = Integer.parseInt(pixelPerBlockXS);
			String pixelPerBlockYS = x.getProperty(PPREFIX + ".pixelPerBlockY", DPIXELPBLOCKY);
			int pixelPerBlockY = Integer.parseInt(pixelPerBlockYS);
			
			String toleratedDifferencesS = x.getProperty(PPREFIX + ".toleratedDifferences", TDIFFERENCES);
			double toleratedDifferences = Double.parseDouble(toleratedDifferencesS);

			try {
				// Initialize referenceImage, screenshotImage, delete
				// screenshotImageFile
				BufferedImage screenshot = ImageIO.read(screenshotFile);
				BufferedImage reference = ImageIO.read(referenceFile);
				screenshotFile.delete();

				// Initialize markedImageFile and maskImageFile
				new File(directory + "/marked/").mkdirs();
				String markedImagePath = directory + "/marked/"
						+ screenshotName + "-marked" + ".png";
				File markedImageFile = new File(markedImagePath);

				new File(directory + "/mask/").mkdirs();
				String maskImagePath = directory + "/mask/" + screenshotName
						+ "-mask" + ".png";
				File maskImageFile = new File(maskImagePath);

				// Initializes boolean variable for training mode
				String trainingModeString = x.getProperty(PPREFIX
						+ ".trainingMode");
				Boolean trainingMode = Boolean.parseBoolean(trainingModeString);

				ImageComparison imagecomparison = new ImageComparison(
						pixelPerBlockX, pixelPerBlockY, toleratedDifferences, trainingMode, "exactComparison");
				if (!imagecomparison.isEqual(reference, screenshot, maskImageFile, markedImageFile)); {

					// Give an assertion. The marked Image was saved in the
					// fuzzyEqual method
					String assertMessage = "Website does not match the reference screenshot: "
							+ currentActionName;
					Assert.assertTrue(assertMessage, false);
				}
			}
			catch (IOException e) {
				throw new RuntimeIOException();
			}
		}
	}

	/**
	 * Takes a screenshot if the underlying web driver instance is capable of
	 * doing it.
	 * 
	 * @throws IOException
	 */
	private void takeScreenshot(final WebDriver webDriver, final File pngFile)
			throws IOException {
		if (webDriver instanceof TakesScreenshot) {
			byte[] bytes = ((TakesScreenshot) webDriver)
					.getScreenshotAs(OutputType.BYTES);
			FileUtils.writeByteArrayToFile(pngFile, bytes);
		} else {
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
		Capabilities capabilities = ((RemoteWebDriver) webdriver)
				.getCapabilities();
		String browserName = capabilities.getBrowserName();
		return browserName;
	}
}