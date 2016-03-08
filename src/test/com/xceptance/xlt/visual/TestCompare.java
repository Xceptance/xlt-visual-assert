package test.com.xceptance.xlt.visual;

import java.awt.image.BufferedImage;
import java.text.MessageFormat;

import org.junit.Assert;

import com.xceptance.xlt.visualassertion.util.ImageComparison;
import com.xceptance.xlt.visualassertion.util.MaskImage;
import com.xceptance.xlt.visualassertion.algorithm.ComparisonAlgorithm;
import com.xceptance.xlt.visualassertion.algorithm.ExactMatch;
import com.xceptance.xlt.visualassertion.util.RectangleMask;

public class TestCompare extends ImageTest
{
    private final RectangleMask differenceMarker;

    private final ComparisonAlgorithm algorithm;

    private ImageComparison comperator;

    private MaskImage masker;

    private BufferedImage referenceImage;

    private BufferedImage comparisonImage;

    private int markingSizeY;

    private int markingSizeX;

    public TestCompare()
    {
        this.algorithm = new ExactMatch();
        this.differenceMarker = new RectangleMask(10, 10);
    }

    public TestCompare(final ComparisonAlgorithm algorithm, final RectangleMask differenceMarker, final int markingSizeX, final int markingSizeY)
    {
        this.algorithm = algorithm;
        this.differenceMarker = differenceMarker;
        this.markingSizeX = markingSizeX;
        this.markingSizeY = markingSizeY;
    }

    public TestCompare match(final String referenceImageFile)
    {
        return match(load(referenceImageFile));
    }

    public TestCompare match(final BufferedImage referenceImage)
    {
        this.referenceImage = referenceImage;
        masker = new MaskImage(this.referenceImage);

        return this;
    }

    public TestCompare to(final String comparisonImageFile)
    {
        return to(load(comparisonImageFile));
    }

    public TestCompare to(final BufferedImage comparisonImage)
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

    public TestCompare train(final String trainImageFile)
    {
        return train(load(trainImageFile));
    }

    public TestCompare train(final BufferedImage trainImage)
    {
        masker.train(trainImage, algorithm, differenceMarker);
        return this;
    }

    public TestCompare hasMarking(final String markingFile)
    {
        return hasMarking(load(markingFile));
    }

    public TestCompare hasMarking(final BufferedImage marking)
    {
        final BufferedImage comperatorDifference = comperator.getMarkedImageWithBoxes(markingSizeX, markingSizeY);

        final long now = System.currentTimeMillis();
        writeToTmp(comperatorDifference, MessageFormat.format("actual.{0}.png", String.valueOf(now)));
        writeToTmp(marking, MessageFormat.format("expected.{0}.png", String.valueOf(now)));
        Assert.assertTrue(imageEqual(comperatorDifference, marking));

        return this;
    }

    public TestCompare hasNoMarking()
    {
        final BufferedImage comperatorDifference = comperator.getMarkedImageWithBoxes(markingSizeX, markingSizeY);
        Assert.assertNull(comperatorDifference);

        return this;
    }

    public MaskImage getMasker()
    {
        return masker;
    }

    public ImageComparison getComperator()
    {
        return comperator;
    }
}
