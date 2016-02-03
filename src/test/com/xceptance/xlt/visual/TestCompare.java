package test.com.xceptance.xlt.visual;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.Assert;

import com.xceptance.xlt.visual.ImageComparison;
import com.xceptance.xlt.visual.ImageMask;
import com.xceptance.xlt.visual.algorithm.ComparisonAlgorithm;
import com.xceptance.xlt.visual.algorithm.ExactMatch;
import com.xceptance.xlt.visual.mask.RectangleMask;

public class TestCompare extends ImageTest
{
    private RectangleMask differenceMarker;

    private ComparisonAlgorithm algorithm;

    private ImageComparison comperator;

    private ImageMask masker;

    private BufferedImage referenceImage;

    private BufferedImage comparisonImage;

    private int markingSizeY;

    private int markingSizeX;

    public TestCompare()
    {
        this.algorithm = new ExactMatch();
        this.differenceMarker = new RectangleMask(10, 10);
    }

    public TestCompare(ComparisonAlgorithm algorithm, RectangleMask differenceMarker, int markingSizeX, int markingSizeY)
    {
        this.algorithm = algorithm;
        this.differenceMarker = differenceMarker;
        this.markingSizeX = markingSizeX;
        this.markingSizeY = markingSizeY;
    }

    public TestCompare match(String referenceImageFile)
    {
        return match(load(referenceImageFile));
    }

    public TestCompare match(BufferedImage referenceImage)
    {
        this.referenceImage = referenceImage;
        masker = new ImageMask(this.referenceImage);

        return this;
    }

    public TestCompare to(String comparisonImageFile)
    {
        return to(load(comparisonImageFile));
    }

    public TestCompare to(BufferedImage comparisonImage)
    {
        this.comparisonImage = comparisonImage;
        comperator = new ImageComparison(referenceImage);

        return this;
    }

    public TestCompare isEqual()
    {
        Assert.assertTrue(comperator.isEqual(comparisonImage, masker, algorithm));
        return this;
    }

    public TestCompare isNotEqual()
    {
        Assert.assertFalse(comperator.isEqual(comparisonImage, masker, algorithm));
        return this;
    }

    public TestCompare train(String trainImageFile)
    {
        return train(load(trainImageFile));
    }

    public TestCompare train(BufferedImage trainImage)
    {
        masker.train(trainImage, algorithm, differenceMarker);
        return this;
    }

    public TestCompare hasMarking(String markingFile)
    {
        return hasMarking(load(markingFile));
    }

    public TestCompare hasMarking(String markingFile, Color c)
    {
        return hasMarking(load(markingFile), c);
    }

    public TestCompare hasMarking(BufferedImage marking)
    {
        return hasMarking(marking, null);
    }

    public TestCompare hasMarking(BufferedImage marking, Color c)
    {
        BufferedImage comperatorDifference = comperator.getMarkedDifferencesImage(markingSizeX, markingSizeY, c);
        Assert.assertTrue(imageEqual(comperatorDifference, marking));

        return this;
    }

    public TestCompare hasNoMarking()
    {
        BufferedImage comperatorDifference = comperator.getMarkedDifferencesImage(markingSizeX, markingSizeY, null);
        Assert.assertNull(comperatorDifference);

        return this;
    }

    public ImageMask getMasker()
    {
        return masker;
    }

    public ImageComparison getComperator()
    {
        return comperator;
    }
}
