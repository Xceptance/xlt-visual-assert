package test.com.xceptance.xlt.visualassertion;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.visualassertion.ImageComparison;
import com.xceptance.xlt.visualassertion.ImageComparison.Algorithm;

/**
 * Tests if the training mode marks what it should. Includes one test for PIXELFUZZY in case markingX or markingY are 1.
 * 
 * @author daniel
 */
public class TTraining
{
    static BufferedImage reference, newImage;

    ImageComparison fuzzyTraining = new ImageComparison(Algorithm.FUZZY, 10, 10, 10, 0.00, 0.01, true, false, 3, 3, false);

    ImageComparison fuzzyImgCompare = new ImageComparison(Algorithm.FUZZY, 10, 10, 10, 0.00, 0.01, false, false, 3, 3, false);

    ImageComparison fuzzyDifference = new ImageComparison(Algorithm.FUZZY, 10, 10, 10, 0.00, 0.01, false, false, 3, 3, true);

    ImageComparison exactlyTraining = new ImageComparison(Algorithm.MATCH, 10, 10, 1, 0.00, 0.01, true, false, 3, 3, false);

    ImageComparison exactlyCompare = new ImageComparison(Algorithm.MATCH, 10, 10, 1, 0.00, 0.01, false, false, 3, 3, false);

    ImageComparison exactlyDifference = new ImageComparison(Algorithm.MATCH, 10, 10, 1, 0.00, 0.01, false, false, 3, 3, true);

    ImageComparison pixelFuzzyTraining = new ImageComparison(Algorithm.COLORFUZZY, 10, 10, 1, 0.01, 0.01, true, false, 3, 3, false);

    ImageComparison pixelFuzzyTrainingMarkingXYOne = new ImageComparison(Algorithm.COLORFUZZY, 1, 1, 1, 0.01, 0.01, true, false, 3, 3,
                                                                         false);

    ImageComparison pixelFuzzyCompare = new ImageComparison(Algorithm.COLORFUZZY, 10, 10, 1, 0.01, 0.01, false, false, 3, 3, false);

    ImageComparison pixelFuzzyDifference = new ImageComparison(Algorithm.COLORFUZZY, 10, 10, 1, 0.01, 0.01, false, false, 3, 3, true);

    static File directory = org.apache.commons.lang3.SystemUtils.getJavaIoTmpDir();

    static File outPutfile = new File(directory + "/test.png");

    static File maskFile = new File(directory + "/mask.png");

    static File differenceFile = new File(directory + "/difference.png");

    private static int rgbMasked = Color.BLACK.getRGB();

    private static int rgbNotMasked = new Color(255, 255, 255, 0).getRGB();

    @BeforeClass
    public static void setUp() throws IOException
    {
        reference = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        paintWhite(reference);
        newImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
    }

    // Deletes the remnants of previous tests
    @Before
    public void setUpFileAndPicture() throws IOException
    {
        paintWhite(newImage);
        maskFile.delete();
    }

    // test a run with the training mode without any differences between the
    // images
    @Test
    public void trainWithNoDifference() throws IOException
    {
        setUpFileAndPicture();
        Assert.assertTrue(fuzzyImgCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(fuzzyTraining.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(fuzzyImgCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
    }

    // a single block is colored different and should be recognized by the
    // training mode
    @Test
    public void trainWithSingleDifference() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 0, 0, 10, 10);
        Assert.assertFalse(fuzzyImgCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(fuzzyTraining.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(fuzzyImgCompare.isEqual(reference, newImage, maskFile, new File("/home/daniel/Pictures/ouput.png"),
                                                  differenceFile));
    }

    // two areas are colored different and should be recognized by the training
    // mode
    @Test
    public void trainWithMultipleDifferences() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 50, 30, 23, 27);
        paintArea(newImage, 78, 83, 10, 45);
        fuzzyAssertBlock();
    }

    // after a first round in training mode, additional differences are created
    // to see if the training works properly over multiple runs
    @Test
    public void trainOverMultipleRounds() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 1, 1, 10, 10);
        fuzzyAssertBlock();
        paintArea(newImage, 50, 50, 25, 25);
        fuzzyAssertBlock();
    }

    // test if training mode works properly for the exact pixel comparison
    @Test
    public void trainExactlyEqual() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 80, 80, 1, 1);
        exactlyAssertBlock();
        paintArea(newImage, 74, 154, 7, 1);
        exactlyAssertBlock();
    }

    // test if training mode works properly for the fuzzy pixel comparison
    @Test
    public void trainPixelFuzzyEqual() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 48, 97, 10, 10);
        pixelFuzzyAssertBlock();
    }

    // test the interaction between training mode and difference image for EXACTLY
    @Test
    public void trainDifferenceExactly() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 48, 97, 10, 10);
        Assert.assertFalse(exactlyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        differenceDrawn();
        differenceFile.delete();
        Assert.assertTrue(exactlyTraining.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(exactlyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        paintArea(newImage, 0, 0, 9, 9);
        Assert.assertFalse(exactlyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        differenceEmpty();
    }

    // test the interaction between training mode and difference image for FUZZY
    @Test
    public void trainDifferenceFuzzy() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 48, 97, 10, 10);
        Assert.assertFalse(fuzzyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        differenceDrawn();
        differenceFile.delete();
        Assert.assertTrue(fuzzyTraining.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(fuzzyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        paintArea(newImage, 0, 0, 9, 9);
        Assert.assertFalse(fuzzyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        differenceEmpty();
    }

    /**
     * Test is the trainingmode masks pixels, not blocks when markingX or markingY are 1. Tests if differences are
     * masked and nothing else is.
     * 
     * @throws IOException
     */
    @Test
    public void trainPixelFuzzyMarkingXYOne() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 48, 97, 31, 31);
        newImage.setRGB(0, 0, Color.CYAN.getRGB());
        pixelFuzzyTrainingMarkingXYOne.isEqual(reference, newImage, maskFile, outPutfile, differenceFile);

        // Go through every pixel of the maskImage, check if the ones where there were differences
        // are masked and the others are not.
        final BufferedImage maskImage = ImageIO.read(maskFile);
        for (int w = 0; w < maskImage.getWidth(); w++)
        {
            for (int h = 0; h < maskImage.getHeight(); h++)
            {
                if (reference.getRGB(w, h) == newImage.getRGB(w, h))
                {
                    Assert.assertEquals(rgbNotMasked, maskImage.getRGB(w, h));
                }
                else
                {
                    Assert.assertEquals(rgbMasked, maskImage.getRGB(w, h));
                }
            }
        }
    }

    // test the interaction between training mode and difference image for PIXELFUZZY
    @Test
    public void trainDifferencePixelFuzzy() throws IOException
    {
        setUpFileAndPicture();
        paintArea(newImage, 48, 97, 10, 10);
        Assert.assertFalse(pixelFuzzyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        differenceDrawn();
        differenceFile.delete();
        Assert.assertTrue(pixelFuzzyTraining.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(pixelFuzzyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        paintArea(newImage, 0, 0, 9, 9);
        Assert.assertFalse(pixelFuzzyDifference.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        differenceEmpty();
    }

    @AfterClass
    public static void deleteFiles()
    {
        outPutfile.delete();
        maskFile.delete();
        differenceFile.delete();
    }

    // method for painting the images white
    public static void paintWhite(final BufferedImage img)
    {
        final int rgb = Color.WHITE.getRGB();
        for (int x = 0; x < img.getWidth(); x++)
        {
            for (int y = 0; y < img.getHeight(); y++)
            {
                img.setRGB(x, y, rgb);
            }
        }
    }

    // method for coloring specific areas of an image to create differences
    public void paintArea(final BufferedImage img, final int x, final int y, final int width, final int height)
    {
        final int rgb = Color.BLUE.getRGB();
        for (int a = 0; a < width; a++)
        {
            for (int b = 0; b < height; b++)
            {
                img.setRGB(x + a, y + b, rgb);
            }
        }
    }

    // this assertion block checks if the fuzzy training was completed
    // succesfully
    public void fuzzyAssertBlock() throws IOException
    {
        Assert.assertFalse(fuzzyImgCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(fuzzyTraining.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(fuzzyImgCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
    }

    // this assertion block checks if the exact training was completed
    // succesfully
    public void exactlyAssertBlock() throws IOException
    {
        Assert.assertFalse(exactlyCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(exactlyTraining.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(exactlyCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
    }

    // this assertion block checks if the pixel fuzzy training was completed
    // succesfully
    public void pixelFuzzyAssertBlock() throws IOException
    {
        Assert.assertFalse(pixelFuzzyCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(pixelFuzzyTraining.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
        Assert.assertTrue(pixelFuzzyCompare.isEqual(reference, newImage, maskFile, outPutfile, differenceFile));
    }

    // this assertion checks the correctness of the difference image
    public void differenceDrawn() throws IOException
    {
        boolean correct = false;
        final BufferedImage difference = ImageIO.read(differenceFile);
        for (int i = 10; i < difference.getWidth(); i++)
        {
            for (int j = 10; j < difference.getHeight(); j++)
            {
                if (difference.getRGB(i, j) != Color.BLACK.getRGB())
                {
                    correct = true;
                }
            }
        }
        Assert.assertTrue(correct);
    }

    // this assertion checks correctness of the difference image after a training run
    public void differenceEmpty() throws IOException
    {
        boolean correct = true;
        final BufferedImage difference = ImageIO.read(differenceFile);
        for (int i = 10; i < difference.getWidth(); i++)
        {
            for (int j = 10; j < difference.getHeight(); j++)
            {
                if (difference.getRGB(i, j) != Color.BLACK.getRGB())
                {
                    correct = false;
                }
            }
        }
        Assert.assertTrue(correct);
    }
}
