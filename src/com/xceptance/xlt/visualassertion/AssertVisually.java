package com.xceptance.xlt.visualassertion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
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
public class AssertVisually implements WebDriverCustomModule
{
	private final String PREFIX = "com.xceptance.xlt.visualassertion.";

	/**
	 * Counter for the current screenshots
	 */
	private static ThreadLocal<Integer> indexCounter = new ThreadLocal<Integer>();

	// the property defaults
	private final int WAITINGTIME = 300;

	private final int MARK_BLOCKSIZE_X = 10;
	private final int MARK_BLOCKSIZE_Y = 10;

	private final int FUZZY_BLOCKSIZE_XY = 10;

	private final String COLOR_TOLERANCE = "0.1";
	private final String PIXEL_TOLERANCE = "0.2";

	private final boolean ATTEMPT_TO_CLOSE_MASK = false;
	private final int MASK_CLOSE_GAP_WIDTH = 5;
	private final int MASK_CLOSE_GAP_HEIGHT = 5;

	private final String ALGORITHM = "FUZZY";

	private final boolean CREATE_DIFFERENCE_IMAGE = true;
	private final boolean TRAININGSMODE = false;

	private final String RESULT_DIRECTORY = "results" + File.separator + "visualassertion";

	// subdirectories
	private final String RESULT_DIRECTORY_BASELINE = "baseline";	
	private final String RESULT_DIRECTORY_MASKS = "masks";	
	private final String RESULT_DIRECTORY_RESULTS = "results";	// all live screenshots go here

	// the property names
	public final String PROPERTY_RESULT_DIRECTORY = PREFIX + "resultDirectory";
	public final String PROPERTY_WAITING_TIME = PREFIX + "waitingTime";

	public final String PROPERTY_MARK_BLOCKSIZE_X = PREFIX + "mark.blocksize.x";
	public final String PROPERTY_MARK_BLOCKSIZE_Y = PREFIX + "mark.blocksize.y";

	public final String PROPERTY_COLOR_TOLERANCE = PREFIX + "tolerance.colors";
	public final String PROPERTY_PIXEL_TOLERANCE = PREFIX + "tolerance.pixels";

	public final String PROPERTY_FUZZY_BLOCKSIZE_XY = PREFIX + "fuzzy.blocksize.xy";

	public final String PROPERTY_CREATE_DIFFERENCEIMAGE = PREFIX + "onFailure.createDifferenceImage";

	public final String PROPERTY_TRAININGSMODE = PREFIX + "trainingsMode";

	public final String PROPERTY_MASK_CLOSE = PREFIX + "mask.close";
	public final String PROPERTY_MASK_CLOSE_GAP_WIDTH = PREFIX + "mask.close.width";
	public final String PROPERTY_MASK_CLOSE_GAP_HEIGHT = PREFIX + "mask.close.height";

	public final String PROPERTY_ALGORITHM = PREFIX + "algorithm";
	public final String PROPERTY_ALGORITHM_FUZZY = "FUZZY";
	public final String PROPERTY_ALGORITHM_COLORFUZZY = "COLORFUZZY";
	public final String PROPERTY_ALGORITHM_MATCH = "MATCH";


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
	 * com.xceptance.xlt.imageComparison.pixTolerance: Fuzzyness parameter, used
	 * if the specified algorithm is 'FUZZY'. That many different pixels (in
	 * percent) will be tolerated.<br>
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
	 * COLORFUZZY: A pixel wise comparison with tolerance (using the
	 * colTolerance property). Differences between two pixels will be ignored if
	 * they are under the defined tolerance level. <br>
	 * FUZZY: A more fuzzy comparison using the pixelPerBlockXY property, the
	 * colTolerance parameter and the pixTolerance property. The images are
	 * divided into blocks. Minor differences in color are ignored like in
	 * COLORFUZZY. Additionally, the images are divided into squares with a
	 * width and height of pixelPerBlockXY. Whithin these blocks, differences
	 * will be ignored as long as there are less different pixels then
	 * pixTolerance.<br>
	 * Default: FUZZY
	 * 
	 * ImageComparison. {@inheritDoc}
	 */

	@Override
	public void execute(final WebDriver webDriver, final String... args)
	{
		//		// see of we got any property passed and have to use these
		//		final Properties arguments = new Properties();
		//		if (args.length > 0)
		//		{
		//			for (String arg : args)
		//			{
		//				// to be done!!!!
		//			}
		//		}

		final XltProperties props = XltProperties.getInstance();

		// Get Properties and convert them from String if necessary
		final String resultDirectory = props.getProperty(PROPERTY_RESULT_DIRECTORY, RESULT_DIRECTORY);

		// waitTime
		final int waitTime = props.getProperty(PROPERTY_WAITING_TIME, WAITINGTIME);

		// markBlockX, markBlockY
		final int markBlockSizeX = props.getProperty(PROPERTY_MARK_BLOCKSIZE_X, MARK_BLOCKSIZE_X);
		final int markBlockSizeY = props.getProperty(PROPERTY_MARK_BLOCKSIZE_Y, MARK_BLOCKSIZE_Y);

		// pixelPerBlockXY, fuzzyness parameter
		final int pixelPerBlockXY = props.getProperty(PROPERTY_FUZZY_BLOCKSIZE_XY, FUZZY_BLOCKSIZE_XY);

		// colTolerance
		final String colorToleranceValue = props.getProperty(PROPERTY_COLOR_TOLERANCE, COLOR_TOLERANCE);
		final double colorTolerance = Double.parseDouble(colorToleranceValue);

		// pixTolerance
		final String pixelToleranceValue = props.getProperty(PROPERTY_PIXEL_TOLERANCE, PIXEL_TOLERANCE);
		final double pixelTolerance = Double.parseDouble(pixelToleranceValue);

		// trainingMode
		final boolean trainingsModeEnabled = props.getProperty(PROPERTY_TRAININGSMODE, TRAININGSMODE);

		// closeMask
		final boolean closeMask = props.getProperty(PROPERTY_MASK_CLOSE, ATTEMPT_TO_CLOSE_MASK);

		// closeWidth
		final int closeMaskWidth = props.getProperty(PROPERTY_MASK_CLOSE_GAP_WIDTH, MASK_CLOSE_GAP_WIDTH);

		// closeWidth
		final int closeMaskHeight = props.getProperty(PROPERTY_MASK_CLOSE_GAP_HEIGHT, MASK_CLOSE_GAP_HEIGHT);

		// differenceImage
		final boolean differenceImage = props.getProperty(PROPERTY_CREATE_DIFFERENCEIMAGE, CREATE_DIFFERENCE_IMAGE);

		// algorithm
		final String algorithmString = props.getProperty(PROPERTY_ALGORITHM, ALGORITHM).trim().toUpperCase();
		final ImageComparison.Algorithm algorithm;

		switch (algorithmString)
		{
		case PROPERTY_ALGORITHM_COLORFUZZY: 
			algorithm = ImageComparison.Algorithm.COLORFUZZY;
			break;
		case PROPERTY_ALGORITHM_MATCH:
			algorithm = ImageComparison.Algorithm.MATCH;
			break;
		case PROPERTY_ALGORITHM_FUZZY: 
			algorithm = ImageComparison.Algorithm.FUZZY;
			break;
		default:
			algorithm = ImageComparison.Algorithm.FUZZY;
		}

		// Get testcasename for the correct folder
		final String currentTestCaseName = Session.getCurrent().getUserID();

		// Get browsername for the correct subfolder
		final String browserName = getBrowserName(webDriver);

		// get the current action for naming	
		final String currentActionName = Session.getCurrent().getWebDriverActionName();

		// Get path to the directory
		final File targetDirectory = new File(new File(resultDirectory, currentTestCaseName), browserName);
		targetDirectory.mkdirs();

		// retrieve current index counter
		Integer index = indexCounter.get();
		if (index == null)
		{
			index = 1;
		}
		else
		{
			index = index + 1;
		}
		indexCounter.set(index);

		// Set name of the screenshot
		final String screenshotName = String.format("%03d", index) + "-" + currentActionName;

		// where the reference is
		final File resultDirectoryBaselinePath = new File(targetDirectory, RESULT_DIRECTORY_BASELINE);
		resultDirectoryBaselinePath.mkdirs();

		final File referenceScreenShotFile = new File(resultDirectoryBaselinePath, screenshotName + ".png");

		// where the current screenshot goes
		final File currentScreenShotPath = new File(new File(resultDirectoryBaselinePath, RESULT_DIRECTORY_RESULTS), Session.getCurrent().getID());
		currentScreenShotPath.mkdirs();

		final File currentScreenShotFile = new File(currentScreenShotPath, screenshotName + ".png");

		// Wait a few miliseconds so the website is fully loaded
		try
		{
			TimeUnit.MILLISECONDS.sleep(waitTime);
		}
		catch (final InterruptedException e)
		{
			Thread.currentThread().interrupt();
		}

		try
		{
			if (!takeScreenshot(webDriver, currentScreenShotFile))
			{
				// webdriver cannot take screenshots, so leave here
				return;
			}

			// If there's no other screenshot, just copy this as baseline
			if (!referenceScreenShotFile.isFile())
			{
				FileUtils.copyFile(currentScreenShotFile, referenceScreenShotFile);

				// because we have not done anything before, we leave and hope of
				// another round where we can compare stuff
				return;
			}

			// ok, get serious about comparing and so on

			// Initialize referenceImage, screenshotImage, delete
			// screenshotImageFile
			final BufferedImage screenshot = ImageIO.read(currentScreenShotFile);
			final BufferedImage reference = ImageIO.read(referenceScreenShotFile);

			// Initialize markedImageFile and maskImageFile
			final File markedImageFile = new File(currentScreenShotPath, screenshotName + "-marked" + ".png");

			final File maskDirectoryPath = new File(targetDirectory, RESULT_DIRECTORY_MASKS);
			maskDirectoryPath.mkdirs();

			// the mask image file
			final File maskImageFile = new File(maskDirectoryPath, screenshotName + ".png");

			// the difference image file
			final File differenceImageFile = new File(currentScreenShotPath, screenshotName + "-difference" + ".png");

			// Initializes ImageComparison and calls isEqual
			final ImageComparison imagecomparison = new ImageComparison(
					algorithm, markBlockSizeX, 
					markBlockSizeY, 
					pixelPerBlockXY,
					colorTolerance, 
					pixelTolerance,
					trainingsModeEnabled, 
					closeMask, closeMaskWidth, 
					closeMaskHeight, differenceImage);

			final boolean result = imagecomparison.isEqual(reference, screenshot, maskImageFile, markedImageFile, differenceImageFile);

			final String assertMessage = "Website does not match the reference screenshot: " + currentActionName;
			Assert.assertTrue(assertMessage, result);
		}
		catch (final IOException e)
		{
			Assert.fail(MessageFormat.format("Failure during visual image assertion: {0}", e.getMessage()));
		}

	}

	/**
	 * Takes a screenshot if the underlying web driver instance is capable of doing it. Fails with 
	 * a message only in case the webdriver cannot take screenshots. Avoids issue when certain
	 * drivers are used.
	 * 
	 * @param webDriver the web driver to use
	 * @param pngFile the file to write to
	 * @return true if we could take a screenshot, false otherwise
	 * @throws IOException in case the files cannot be written
	 */
	private boolean takeScreenshot(final WebDriver webDriver, final File pngFile) throws IOException
	{
		if (webDriver instanceof TakesScreenshot)
		{
			final byte[] bytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
			FileUtils.writeByteArrayToFile(pngFile, bytes);

			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns the browsername using Selenium methods
	 * 
	 * @param webDriver the WebDriver to query
	 * @return the browser name
	 */
	private String getBrowserName(final WebDriver webDriver)
	{
		final Capabilities capabilities = ((RemoteWebDriver) webDriver).getCapabilities();
		final String browserName = capabilities.getBrowserName();

		return browserName;
	}
}