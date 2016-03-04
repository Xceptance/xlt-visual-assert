package test.com.xceptance.xlt.visual.exact;

import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.visualassertion.algorithm.ComparisonAlgorithm;
import com.xceptance.xlt.visualassertion.algorithm.ExactMatch;
import com.xceptance.xlt.visualassertion.util.RectangleMask;

import test.com.xceptance.xlt.visual.ImageTest;
import test.com.xceptance.xlt.visual.TestCompare;

public class TExactMatch extends ImageTest
{
    ComparisonAlgorithm a = new ExactMatch();

    // how should a difference maSked during training
    RectangleMask m = new RectangleMask(10, 10);

    // how difference should maRked in difference file
    int mX = 10;

    int mY = 10;

    TestCompare T;

    @Before
    public void setup()
    {
        T = new TestCompare(a, m, mX, mY);
    }

    @Test
    public void blank()
    {
        T.match("exact/blank.png").to("exact/blank.png").isEqual().hasNoMarking();
    }

    @Test
    public void photo()
    {
        T.match("exact/photo.png").to("exact/photo.png").isEqual().hasNoMarking();
    }

    @Test
    public void photoSameButDifferentFile()
    {
        T.match("exact/photo.png").to("exact/photo2.png").isEqual().hasNoMarking();
    }

    @Test
    public void noMatchPixelDiff()
    {
        T.match("exact/blank.png").to("exact/oneblackpixel.png").isNotEqual().hasMarking("exact/noMatchPixelDiff.png");
    }

    @Test
    public void noMatchDifferentSize()
    {
        T.match("exact/photo.png").to("exact/photo-205x205.png").isNotEqual().hasMarking("exact/photo-205x205-MaskExpected.png");
    }

    @Test
    public void noMatchDifferentSizeReversed()
    {
        T.match("exact/photo-205x205.png").to("exact/photo.png").isNotEqual().hasMarking("exact/photo-205x205-ReversedMaskExpected.png");
    }

    @Test
    public void noMatchNegated()
    {
        T.match("exact/blank.png").to("exact/negated-blank.png").isNotEqual().hasMarking("exact/negated-MaskExpected.png");
    }
}
