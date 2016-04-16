package com.xceptance.xlt.visualassertion;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.xceptance.xlt.api.engine.Session;
import com.xceptance.xlt.api.engine.scripting.WebDriverCustomModule;
import com.xceptance.xlt.api.util.XltProperties;
import com.xceptance.xlt.visualassertion.algorithm.ColorFuzzy;
import com.xceptance.xlt.visualassertion.algorithm.ComparisonAlgorithm;
import com.xceptance.xlt.visualassertion.algorithm.ExactMatch;
import com.xceptance.xlt.visualassertion.algorithm.PixelFuzzy;
import com.xceptance.xlt.visualassertion.util.ImageComparison;
import com.xceptance.xlt.visualassertion.util.MaskImage;
import com.xceptance.xlt.visualassertion.util.RectangleMask;

/**
 * Module for the visual assertion of changes in a browser page. The module is called in an
 * action and takes a screenshot of the current page. This screenshot is then compared to already taken
 * reference images of the same page, or stored as reference image.
 *
 * The configurations for this module are done in the visualassertion.properties under /config
 * There are different algorithms that can be used for the comparison of the images and different ways to
 * visualize those differences for the evaluation.
 */
public class VisualAssertion implements WebDriverCustomModule
{

    private final String PREFIX = "com.xceptance.xlt.visualassertion.";

    /**
     * Counter for the current screenshots
     */
    private static ThreadLocal<Integer> indexCounter = new ThreadLocal<>();

    // the property defaults
    private final int WAITINGTIME = 300;

    private final String ALL = "all";

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

    private final String RESULT_DIRECTORY_RESULTS = "results";  // all live screenshots go here

    // the property names
    public final String PROPERTY_ENABLED = PREFIX + "enabled";
    public final String PROPERTY_RESULT_DIRECTORY = PREFIX + "resultDirectory";
    public final String PROPERTY_ID = PREFIX + "ID";

    public final String PROPERTY_WAITING_TIME = PREFIX + "waitingTime";

    public final String PROPERTY_MARK_BLOCKSIZE_X = PREFIX + "mark.blocksize.x";
    public final String PROPERTY_MARK_BLOCKSIZE_Y = PREFIX + "mark.blocksize.y";
    public final String PROPERTY_MARK_TYPE = PREFIX + "mark.type";

    public final String MARK_WITH_BOXES = "box";
    public final String MARK_WITH_A_MARKER = "marker";

    public final String PROPERTY_ALGORITHM = PREFIX + "algorithm";
    public final String PROPERTY_ALGORITHM_FUZZY = "FUZZY";
    public final String PROPERTY_ALGORITHM_COLORFUZZY = "COLORFUZZY";
    public final String PROPERTY_ALGORITHM_EXACTMATCH = "EXACT";

    public final String PROPERTY_COLOR_TOLERANCE = PREFIX + "tolerance.colors";

    public final String PROPERTY_PIXEL_TOLERANCE = PREFIX + "tolerance.pixels";

    public final String PROPERTY_FUZZY_BLOCKSIZE_XY = PREFIX + "fuzzy.blocksize.xy";

    public final String PROPERTY_CREATE_DIFFERENCEIMAGE = PREFIX + "onFailure.createDifferenceImage";

    public final String PROPERTY_TRAININGSMODE = PREFIX + "trainingsMode";

    public final String PROPERTY_MASK_CLOSE = PREFIX + "mask.close";

    public final String PROPERTY_MASK_CLOSE_GAP_WIDTH = PREFIX + "mask.close.width";

    public final String PROPERTY_MASK_CLOSE_GAP_HEIGHT = PREFIX + "mask.close.height";

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

        // Parent directory of the visual assertion results
        final String resultDirectory = props.getProperty(PROPERTY_RESULT_DIRECTORY, RESULT_DIRECTORY);

        // Wait time for the page to load completely
        final int waitTime = props.getProperty(PROPERTY_WAITING_TIME, WAITINGTIME);

        // Block size for the visual marking of differences in the snapshot
        final int markBlockSizeX = props.getProperty(PROPERTY_MARK_BLOCKSIZE_X, MARK_BLOCKSIZE_X);
        final int markBlockSizeY = props.getProperty(PROPERTY_MARK_BLOCKSIZE_Y, MARK_BLOCKSIZE_Y);
        // Marking type that is used for the test
        final String markType = props.getProperty(PROPERTY_MARK_TYPE, MARK_WITH_BOXES);

        // fuzzyBlockLength, fuzzyness parameter
        final int fuzzyBlockLength = props.getProperty(PROPERTY_FUZZY_BLOCKSIZE_XY, FUZZY_BLOCKSIZE_XY);

        // Tolerance value for differences in color
        final String colorToleranceValue = props.getProperty(PROPERTY_COLOR_TOLERANCE, COLOR_TOLERANCE);
        final double colorTolerance = Double.parseDouble(colorToleranceValue);

        // Tolerance value for differences between specific pixels
        final String pixelToleranceValue = props.getProperty(PROPERTY_PIXEL_TOLERANCE, PIXEL_TOLERANCE);
        final double pixelTolerance = Double.parseDouble(pixelToleranceValue);

        // Flag whether the training mode is enabled
        final boolean trainingsModeEnabled = props.getProperty(PROPERTY_TRAININGSMODE, TRAININGSMODE);

        // Flag whether masks should be closed to make the covered area larger
        final boolean closeMask = props.getProperty(PROPERTY_MASK_CLOSE, ATTEMPT_TO_CLOSE_MASK);

        // Width of the mask close
        final int closeMaskWidth = props.getProperty(PROPERTY_MASK_CLOSE_GAP_WIDTH, MASK_CLOSE_GAP_WIDTH);

        // Height of the mask close
        final int closeMaskHeight = props.getProperty(PROPERTY_MASK_CLOSE_GAP_HEIGHT, MASK_CLOSE_GAP_HEIGHT);

        // Flag whether a pixel difference image should be created
        final boolean createDifferenceImage = props.getProperty(PROPERTY_CREATE_DIFFERENCEIMAGE, CREATE_DIFFERENCE_IMAGE);

        // Selector for the algorithm that shall be used
        final String algorithmString = props.getProperty(PROPERTY_ALGORITHM, ALGORITHM).trim().toUpperCase();

        // Identification of the current environment for this test
        final String id = props.getProperty(PROPERTY_ID, ALL);


        //--------------------------------------------------------------------------------
        // Get the current environment
        //--------------------------------------------------------------------------------

        // Get the name of the test case for the correct folder identifier
        final String currentTestCaseName = Session.getCurrent().getUserName();

        // Get browsername and browserversion for the subfolders
        final String browserName = getBrowserName(webdriver);
        final String browserVersion = getBrowserVersion(webdriver);

        // Get the name of the action that called the visual assertion
        final String currentActionName = Session.getCurrent().getCurrentActionName();


        //--------------------------------------------------------------------------------
        // Initialize the directory and file paths, create the directories if necessary
        //--------------------------------------------------------------------------------

        // Generate the child directories for the current environment in the parent result folder
        final File targetDirectory = new File(new File(new File(new File(resultDirectory, id),
                currentTestCaseName),
                browserName),
                browserVersion);
        targetDirectory.mkdirs();

        // Retrieve current index counter for the image file names
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

        // Name of the image file for the screenshot
        final String screenshotName = String.format("%03d", index) + "-" + currentActionName;

        // Directory for the reference images
        final File baselineDirectory = new File(targetDirectory, RESULT_DIRECTORY_BASELINE);
        baselineDirectory.mkdirs();
        // Path of the reference image for this assertion
        final File referenceImageFile = new File(baselineDirectory, screenshotName + ".png");


        // Directory for the results of the current test run
        final File testInstanceDirectory = new File(new File(targetDirectory, RESULT_DIRECTORY_RESULTS),
                Session.getCurrent().getID());
        testInstanceDirectory.mkdirs();
        // Path of the screenshot image file
        final File currentScreenShotFile = new File(testInstanceDirectory, screenshotName + ".png");
        // Path of the marked image file
        final File markedImageFile = new File(testInstanceDirectory, screenshotName + "-marked" + ".png");
        // Path of the difference image file
        final File differenceImageFile = new File(testInstanceDirectory, screenshotName + "-difference" + ".png");


        // Directory of the mask images
        final File maskDirectoryPath = new File(targetDirectory, RESULT_DIRECTORY_MASKS);
        maskDirectoryPath.mkdirs();
        // Path of the mask image file
        final File maskImageFile = new File(maskDirectoryPath, screenshotName + ".png");


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
        // Make the screenshot and load the reference image
        //--------------------------------------------------------------------------------

        try
        {
            final BufferedImage screenshot = takeScreenshot(webdriver);
            if (screenshot == null)
            {
                // TODO Has this to be handled in a different way?
                // webdriver cannot take the screenshot -> RETURN
                return;
            }
            // Save the screenshot
            writeImage(screenshot, currentScreenShotFile);

            // If there's no reference screenshot yet -> save screenshot as reference image in baseline
            if (!referenceImageFile.isFile())
            {
                writeImage(screenshot, referenceImageFile);
                // There is no reference for the comparison -> RETURN
                return;
            }

            // Load the reference image
            final BufferedImage reference = ImageIO.read(referenceImageFile);

            // Mask for the image comparison
            MaskImage mask;
            // If a mask already exists load it, else create a new one
            if (maskImageFile.exists())
            {
                mask = new MaskImage(reference, ImageIO.read(maskImageFile));
            }
            else
            {
                mask = new MaskImage(reference);
                writeImage(mask.getMask(), maskImageFile);
            }


            //--------------------------------------------------------------------------------
            // Initialize the configured algorithm
            //--------------------------------------------------------------------------------

            ComparisonAlgorithm algorithm = null;
            switch (algorithmString)
            {
            case PROPERTY_ALGORITHM_COLORFUZZY:
                algorithm = new ColorFuzzy(colorTolerance);
                break;
            case PROPERTY_ALGORITHM_EXACTMATCH:
                algorithm = new ExactMatch();
                break;
            case PROPERTY_ALGORITHM_FUZZY:
                algorithm = new PixelFuzzy(pixelTolerance, colorTolerance, fuzzyBlockLength);
                break;
            }


            //--------------------------------------------------------------------------------
            // If training is enabled adjust the mask, else compare the screenshot to the
            // reference image
            //--------------------------------------------------------------------------------

            if (trainingsModeEnabled)
            {
                // Train the mask to take the current difference between the reference image and screenshot into account
                mask.train(screenshot, algorithm, new RectangleMask(markBlockSizeX, markBlockSizeY));

                // Close the mask to cover a bigger area
                if (closeMask)
                {
                    mask.closeMask(closeMaskWidth, closeMaskHeight);
                }

                // Save the trained mask
                writeImage(mask.getMask(), maskImageFile);
            }
            else
            {
                // Initialize the comparator
                final ImageComparison comparator = new ImageComparison(reference);

                // Result of the comparison whether the images are similar
                final boolean result = comparator.isEqual(screenshot, mask, algorithm);

                // If the two images don't match..
                if (!result)
                {
                    if (createDifferenceImage)
                    {
                        // Create a image of the pixel differences and save it
                        writeImage(comparator.getDifferenceImage(), differenceImageFile);
                    }

                    BufferedImage markedImage = null;
                    switch (markType) {
                    case MARK_WITH_A_MARKER:
                        // Highlight the differences in the image with red and yellow
                        markedImage = comparator.getMarkedImageWithAMarker(markBlockSizeX, markBlockSizeY);
                        break;
                    case MARK_WITH_BOXES:
                        // Surround the differences with red boxes
                        markedImage = comparator.getMarkedImageWithBoxes(markBlockSizeX, markBlockSizeY);
                        break;
                    default:
                        // break
                        Assert.fail(MessageFormat.format("Mark type '{0}' is not supported.", markType));
                        break;
                    }

                    // Save the marked image
                    writeImage(markedImage, markedImageFile);
                }

                // Assert the result of the comparison
                Assert.assertTrue(MessageFormat.format("Website does not match the reference screenshot: {0} ", currentActionName), result);
            }
        }
        catch (final IOException e)
        {
            Assert.fail(MessageFormat.format("Failure during visual image assertion: {0}", e.getMessage()));
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

    /**
     * Returns the browser version
     * 
     * @param webDriver
     *            the WebDriver to query
     * @return the browser name
     */
    private String getBrowserVersion(final WebDriver webDriver)
    {
        final Capabilities capabilities = ((RemoteWebDriver) webDriver).getCapabilities();
        final String browserVersion = capabilities.getVersion();

        return browserVersion == null ? "unknown" : browserVersion;
    }

    /**
     * Write the image into the filepath given by file
     * @param image that should be saved
     * @param file path where the image shall be saved
     */
    private void writeImage(final BufferedImage image, final File file)
    {
        try
        {
            ImageIO.write(image, "PNG", file);
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
