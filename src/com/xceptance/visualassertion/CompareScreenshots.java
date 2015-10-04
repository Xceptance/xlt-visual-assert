package com.xceptance.visualassertion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
 * is used. The ImageComparison class uses the ImageOperations class. <br>
 * The java module is written for use in an Xlt test case. Selenium APIs were
 * also used.
 * 
 * @author lucas & damian
 */
public class CompareScreenshots implements WebDriverCustomModule {
	final String PPREFIX = "com.xceptance.xlt.imageComparison.";
	final String DWAITTIME = "300";
	final String DMARKBLOCKX = "10";
	final String DMARKBLOCKY = "10";
	final String DPIXELPBLOCKXY = "20";
	final String DCOLTOLERANCE = "0.1";
	final String DPIXTOLERANCE = "0.2";
	final String DCLOSEWIDTH = "5";
	final String DCLOSEHEIGHT = "5";
	final String DALGORITHM = "FUZZY";
	final String DDIFFERENCEIMAGE = "true"; 

	/**
	 * No parameters beyond the webdriver are required. Necessary and possible
	 * properties are at the bottom.
	 * <p>
	 * Screenshots are saved in subfolders with the same name as the testcase.
	 * There are different subfolders for different browsers. The screenshots
	 * are named by the actionname and an index which counts up from one in
	 * every testcase. Thereby, screenshots are never compared to screenshots
	 * made within the same test run, only previously made screenshots made with
	 * the same browser, the same testcase, action and position.
	 * <p>
	 * In the first test run, folders for the test case and browser will be
	 * created and reference screenshots will be saved. In the second test run,
	 * sub folders for mask images and marked images will be created and
	 * comparisons will be made.
	 * <p>
	 * If a difference between an image and it's reference image was detected,
	 * the image will be marked and the resulting marked image will be saved in
	 * the marked image folder; the method will fail due to an assertion. <br>
	 * The change can be on the edge of the rectangle that was used to mark it.
	 * If that was the only change, the pixel was unfortunately colored red and
	 * it is impossible to make out what changed exactly. <br>
	 * If the screenshot and the comparison image do not have the same size,
	 * areas that were not existent in one or the other will be completely
	 * painted red. <br>
	 * Without manual intervention, reference images are never changed. The
	 * taken screenshots are also never saved as such.
	 * <p>
	 * If the trainingMode property is true, differences will not be marked, but
	 * the corresponding areas in the mask image will be painted black. So
	 * barring manual intervention, differences in these areas will not be
	 * detected in later runs. <br>
	 * If the closeImage property is true, small gaps in the maskImage will be
	 * closed after the normal training. This can be very performance heavy.
	 * <p>
	 * Properties and their default values:
	 * com.xceptance.xlt.imageComparison.directory: The directory in which the
	 * screenshots will be saved. <br>
	 * No default value, the property is required.
	 * <p>
	 * com.xceptance.xlt.imageComparison.waitTime: The program will wait that
	 * many miliseconds for the website to load. <br>
	 * Default: 300.
	 * <p>
	 * com.xceptance.xlt.imageComparison.markBlockX: The height of the blocks
	 * used for marking and masking. Has to be above 0.<br>
	 * Default: 10.
	 * <p>
	 * com.xceptance.xlt.imageComparison.markBlockY: The width of the blocks
	 * used for marking and masking <br>
	 * Default: 10.
	 * <p>
	 * com.xceptance.xlt.imageComparison.pixelPerBlockXY: Fuzzyness parameter,
	 * used if the specified algorithm is 'FUZZY'. That many different pixels
	 * (in percent) will be tolerated.<br>
	 * Default: 20
	 * <p>
	 * com.xceptance.xlt.imageComparison.colTolerance: Fuzzyness parameter, used
	 * if the specified algorithm is 'FUZZY' or 'PIXELFUZZYEQUAL'. If used, a
	 * color difference of up to that much (in percent) will be ignored.<br>
	 * Default: 0.1.
	 * <p>
	 * com.xceptance.xlt.imageComparison.pixTolerance: Fuzzyness parameter,
	 * used if the specified algorithm is 'FUZZY'. That many different pixels
	 * (in percent) will be tolerated.<br>
	 * Default: 0.2
	 * <p>
	 * com.xceptance.xlt.imageComparison.trainingMode: If true, differences will
	 * not be marked, but instead set black in the maskedImage and thereby
	 * masked in future tests. <br>
	 * Default: false.
	 * <p>
	 * com.xceptance.xlt.imageComparison.closeMask: If true, small gaps in the
	 * maskImage will be closed after a training run, making the masked parts
	 * more cohesive. Since this is quite performance heavy for large images,
	 * the maskImage will be scaled down beforehand and scaled back up again
	 * afterwards. It will be scaled by a factor of ten.<br>
	 * Default: false.
	 * <p>
	 * com.xceptance.xlt.imageComparison.closeWidth: <br>
	 * Decides how much should be closed if closeMask is true. Specifically, it
	 * decides the width of the structuring element. For example, if it's ten,
	 * then gaps with a width of up to ten will be closed. Very performance
	 * heavy if high. <br>
	 * The image is scaled down by a factor of 10 before the closing and scaled
	 * up again afterwards! Thus there will always be a certain fuzzyness and a
	 * width of ten translates close to 100 pixels on the image, not ten.
	 * Default: 5
	 * <p>
	 * com.xceptance.xlt.imageComparison.closeHeight: <br>
	 * Decides how much should be closed if closeMask is true. Specifically, it
	 * decides the height of the structuring element. For example, if it's ten,
	 * then gaps with a height of up to ten will be closed. Very performance
	 * heavy if high. <br>
	 * The image is scaled down by a factor of 10 before the closing and scaled
	 * up again afterwards! Thus there will always be a certain fuzzyness and a
	 * height of ten translates close to 100 pixels on the image, not ten.
	 * Default: 5
	 * <p>
	 * com.xceptance.xlt.imageComparison.differenceImage: If true, a greyscale
	 * image will be created in addition to the marked image. The image is
	 * lighter the higher the difference between the screenshot and the
	 * reference image. Default: true.
	 * <p>
	 * com.xceptance.xlt.imageComparison.algorithm: Decides which comparison
	 * algorithm should be used.
	 * <p>
	 * Options: <br>
	 * EXACTLY: A pixel wise comparison without any tolerance. If there are any
	 * difference, the exact comparison will return false. <br>
	 * PIXELFUZZY: A pixel wise comparison with tolerance (using the
	 * colTolerance property). Differences between two pixels will be ignored if
	 * they are under the defined tolerance level. <br>
	 * FUZZY: A more fuzzy comparison using the pixelPerBlockXY property, the
	 * colTolerance parameter and the pixTolerance property. The images are
	 * divided into blocks. Minor differences in color are ignored like in
	 * PIXELFUZZY. Additionally, the images are divided into squares with a
	 * width and height of pixelPerBlockXY. Whithin these blocks, differences
	 * will be ignored as long as there are less different pixels then
	 * pixTolerance.<br>
	 * Default: FUZZY
	 * 
	 * ImageComparison. {@inheritDoc}
	 */

	@Override
	public void execute(final WebDriver webDriver, final String... args) {

		final XltProperties x = XltProperties.getInstance();

		// Get Properties and convert them from String if necessary

		// waitTime
		final String waitTimeS = x.getProperty(PPREFIX + "waitTime", DWAITTIME);
		final int waitTime = Integer.parseInt(waitTimeS);

		// markBlockX, markBlockY
		final String markBlockXS = x.getProperty(PPREFIX + "MARKBLOCKX", DMARKBLOCKX);
		final int markBlockX = Integer.parseInt(markBlockXS);
		final String markBlockYS = x.getProperty(PPREFIX + "MARKBLOCKX", DMARKBLOCKY);
		final int markBlockY = Integer.parseInt(markBlockYS);

		// pixelPerBlockXY, fuzzyness parameter
		final String pixelPerBlockXYS = x.getProperty(PPREFIX + "pixelPerBlockXY",
				DPIXELPBLOCKXY);
		final int pixelPerBlockXY = Integer.parseInt(pixelPerBlockXYS);

		// colTolerance
		final String colToleranceS = x.getProperty(PPREFIX + "colTolerance",
				DCOLTOLERANCE);
		final double colTolerance = Double.parseDouble(colToleranceS);

		// pixTolerance
		final String pixToleranceS = x.getProperty(PPREFIX + "pixTolerance",
				DCOLTOLERANCE);
		final double pixTolerance = Double.parseDouble(pixToleranceS);

		// trainingMode
		final String trainingModeString = x.getProperty(PPREFIX + "trainingMode");
		final Boolean trainingMode = Boolean.parseBoolean(trainingModeString);

		// closeMask
		final String closeMaskString = x.getProperty(PPREFIX + "closeMask");
		final Boolean closeMask = Boolean.parseBoolean(closeMaskString);

		// closeWidth
		final String closeWidthString = x.getProperty(PPREFIX + "closeWidth",
				DCLOSEWIDTH);
		final int closeWidth = Integer.parseInt(closeWidthString);

		// closeHeight
		final String closeHeightString = x.getProperty(PPREFIX + "closeHeight",
				DCLOSEHEIGHT);
		final int closeHeight = Integer.parseInt(closeHeightString);

		// differenceImage
		final String differenceImageS = x.getProperty(PPREFIX + "differenceImage", DDIFFERENCEIMAGE);
		final Boolean differenceImage = Boolean.parseBoolean(differenceImageS);

		// algorithm
		final String algorithm = x.getProperty(PPREFIX + "algorithm", DALGORITHM);

		// Wait a few miliseconds so the website is fully loaded
		try {
			TimeUnit.MILLISECONDS.sleep(waitTime);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		// Checks if this is a new testcase. If yes, (re)sets index. If not.
		// increments index
		if ((x.getProperty(PPREFIX + "currentID") == Session.getCurrent()
				.getID())) {
			String indexString = x.getProperty(PPREFIX + "index");
			int indexInt = Integer.parseInt(indexString);
			indexInt++;
			indexString = String.valueOf(indexInt);
			x.setProperty(PPREFIX + "index", indexString);
		}

		else {
			final String currentID = Session.getCurrent().getID();
			x.setProperty(PPREFIX + "currentID", currentID);
			x.setProperty(PPREFIX + "index", "1");
		}

		// Get testcasename for the correct folder
		final String currentTestCaseName = Session.getCurrent().getUserID();

		// Get browsername for the correct subfolder
		final String currentActionName = Session.getCurrent()
				.getWebDriverActionName();
		final String browserName = getBrowserName(webDriver);

		// Get path to the directory
		String directory = x.getProperty(PPREFIX + "directory");
		directory = directory + "/" + currentTestCaseName + "/" + browserName;

		// Set name of the referenceImage
		final String indexS = x.getProperty(PPREFIX + "index");
		final String screenshotName = indexS + "-" + currentActionName;
		final String referencePath = directory + "/" + screenshotName;

		final File referenceFile = new File(referencePath + ".png");

		// If there's no other screenshot, just save the new one
		if (!referenceFile.isFile()) {
			try {
				takeScreenshot(webDriver, referenceFile);
			} catch (final IOException e) {
				throw new RuntimeIOException();
			}
		}

		// If there is another screenshot ...
		else {
			// Create file for the new screenshot
			final File screenshotFile = new File(directory + "new-screenshot"
					+ screenshotName + ".png");
			try {
				takeScreenshot(webDriver, screenshotFile);
			} catch (final IOException e) {
				throw new RuntimeIOException();
			}

			try {
				// Initialize referenceImage, screenshotImage, delete
				// screenshotImageFile
				final BufferedImage screenshot = ImageIO.read(screenshotFile);
				final BufferedImage reference = ImageIO.read(referenceFile);

				// Initialize markedImageFile and maskImageFile
				new File(directory + "/marked/").mkdirs();
				final String markedImagePath = directory + "/marked/"
						+ screenshotName + "-marked" + ".png";
				final File markedImageFile = new File(markedImagePath);

				new File(directory + "/mask/").mkdirs();
				final String maskImagePath = directory + "/mask/" + screenshotName
						+ "-mask" + ".png";
				final File maskImageFile = new File(maskImagePath);

				new File(directory + "/difference/").mkdirs();
				final String differenceImagePath = directory + "/difference/"
						+ screenshotName + "-difference" + ".png";
				final File differenceImageFile = new File(differenceImagePath);

				// Initializes ImageComparison and calls isEqual
				final ImageComparison imagecomparison = new ImageComparison(
						markBlockX, markBlockY, pixelPerBlockXY, colTolerance,
						pixTolerance, trainingMode, closeMask, closeWidth,
						closeHeight, differenceImage, algorithm);
				final boolean result = imagecomparison.isEqual(reference, screenshot,
						maskImageFile, markedImageFile, differenceImageFile);

				// Delete the new screenshot if there was no difference
				if (result) {
					screenshotFile.delete();
				}
				// Place it into he markedImage folder otherwise
				else {
					final Path source = screenshotFile.toPath();
					final String newScreenshotPath = directory + "/marked/"
							+ screenshotName + "-new" + ".png";
					final Path destination = Paths.get(newScreenshotPath);
					Files.copy(source, destination,
							StandardCopyOption.REPLACE_EXISTING);
					screenshotFile.delete();
				}

				final String assertMessage = "Website does not match the reference screenshot: "
						+ currentActionName;
				Assert.assertTrue(assertMessage, result);

			} catch (final IOException e) {
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
			final byte[] bytes = ((TakesScreenshot) webDriver)
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
	private String getBrowserName(final WebDriver webdriver) {
		final Capabilities capabilities = ((RemoteWebDriver) webdriver)
				.getCapabilities();
		final String browserName = capabilities.getBrowserName();
		return browserName;
	}
}