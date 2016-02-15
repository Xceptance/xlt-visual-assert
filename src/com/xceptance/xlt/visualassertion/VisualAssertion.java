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
import com.xceptance.xlt.visualassertion.mask.RectangleMask;

public class VisualAssertion implements WebDriverCustomModule
{

    private final String PREFIX = "com.xceptance.xlt.visualassertion.";

    /**
     * Counter for the current screenshots
     */
    private static ThreadLocal<Integer> indexCounter = new ThreadLocal<Integer>();

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
    public final String PROPERTY_RESULT_DIRECTORY = PREFIX + "resultDirectory";
    public final String PROPERTY_ID = PREFIX + "ID";

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

    public final String PROPERTY_ALGORITHM_EXACTMATCH = "EXACT";

    @Override
    public void execute(final WebDriver webdriver, final String... arguments)
    {
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

        // closeHeight
        final int closeMaskHeight = props.getProperty(PROPERTY_MASK_CLOSE_GAP_HEIGHT, MASK_CLOSE_GAP_HEIGHT);

        // differenceImage
        final boolean differenceImage = props.getProperty(PROPERTY_CREATE_DIFFERENCEIMAGE, CREATE_DIFFERENCE_IMAGE);

        // algorithm
        final String algorithmString = props.getProperty(PROPERTY_ALGORITHM, ALGORITHM).trim().toUpperCase();

        // something to distinguish environments
        final String id = props.getProperty(PROPERTY_ID, ALL);

        // Get testcasename for the correct folder
        final String currentTestCaseName = Session.getCurrent().getUserName();

        // Get browsername for the correct subfolder
        final String browserName = getBrowserName(webdriver);
        final String browserVersion = getBrowserVersion(webdriver);

        // get the current action for naming
        final String currentActionName = Session.getCurrent().getCurrentActionName();

        // Get path to the directory
        final File targetDirectory = new File(new File(new File(new File(resultDirectory, id), currentTestCaseName), browserVersion), browserName);
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
        final File currentScreenShotPath = new File(new File(targetDirectory, RESULT_DIRECTORY_RESULTS),
                Session.getCurrent().getID());
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
            final BufferedImage screenshot = takeScreenshot(webdriver);
            if (screenshot == null)
            {
                // webdriver cannot take screenshots, so leave here
                return;
            }
            writeImage(screenshot, currentScreenShotFile);

            // If there's no other screenshot, just copy this as baseline
            if (!referenceScreenShotFile.isFile())
            {
                writeImage(screenshot, referenceScreenShotFile);

                // because we have not done anything before, we leave and hope of
                // another round where we can compare stuff
                return;
            }

            // ok, get serious about comparing and so on

            // Initialize referenceImage, screenshotImage, delete
            // screenshotImageFile
            final BufferedImage reference = ImageIO.read(referenceScreenShotFile);

            // Initialize markedImageFile and maskImageFile
            final File markedImageFile = new File(currentScreenShotPath, screenshotName + "-marked" + ".png");

            final File maskDirectoryPath = new File(targetDirectory, RESULT_DIRECTORY_MASKS);
            maskDirectoryPath.mkdirs();

            // the mask image file
            final File maskImageFile = new File(maskDirectoryPath, screenshotName + ".png");

            // the difference image file
            final File differenceImageFile = new File(currentScreenShotPath, screenshotName + "-difference" + ".png");

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
                algorithm = new PixelFuzzy(pixelTolerance, colorTolerance, pixelPerBlockXY);
                break;
            }

            ImageMask mask;
            // if a mask already exists load it else create a new one
            if (maskImageFile.exists())
            {
                mask = new ImageMask(reference, ImageIO.read(maskImageFile));
            }
            else
            {
                mask = new ImageMask(reference);
                writeImage(mask.getMask(), maskImageFile);
            }

            if (trainingsModeEnabled)
            {
                // train the mask and save it
                mask.train(screenshot, algorithm, new RectangleMask(markBlockSizeX, markBlockSizeY));

                if (closeMask)
                {
                    mask.closeMask(closeMaskWidth, closeMaskHeight);
                }

                writeImage(mask.getMask(), maskImageFile);
            }

            final ImageComparison comperator = new ImageComparison(reference);

            final boolean result = comperator.isEqual(screenshot, mask, algorithm);

            if (!result)
            {
                if (differenceImage)
                {
                    writeImage(comperator.getDifferenceImage(), differenceImageFile);
                }

                writeImage(comperator.getMarkedDifferencesImage(markBlockSizeX, markBlockSizeY, null), markedImageFile);
            }

            final String assertMessage = "Website does not match the reference screenshot: " + currentActionName;
            Assert.assertTrue(assertMessage, result);
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
     * @param pngFile
     *            the file to write to
     * @return {@link BufferedImage} if webdriver support taking screenshotss, null otherwise
     * @throws IOException
     *             in case the files cannot be written
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
     * Returns the browsername using Selenium methods
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
