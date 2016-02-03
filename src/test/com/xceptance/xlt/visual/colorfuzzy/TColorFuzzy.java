package test.com.xceptance.xlt.visual.colorfuzzy;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.xceptance.xlt.visual.algorithm.ColorFuzzy;
import com.xceptance.xlt.visual.algorithm.ComparisonAlgorithm;
import com.xceptance.xlt.visual.mask.RectangleMask;

import test.com.xceptance.xlt.visual.ImageTest;
import test.com.xceptance.xlt.visual.TestCompare;

public class TColorFuzzy extends ImageTest
{

    ComparisonAlgorithm a = new ColorFuzzy(0.1);

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

    /**
     * Test default, no difference
     * 
     * @throws IOException
     */
    @Test
    public void sameSimple()
    {
        T.match("colorfuzzy/blank.png").to("colorfuzzy/blank.png").isEqual();
    }

    /**
     * Test default, no difference
     * 
     * @throws IOException
     */
    @Test
    public void samePhoto()
    {
        T.match("colorfuzzy/photo.png").to("colorfuzzy/photo.png").isEqual();
    }

    /**
     * Test default, one pixel diff
     * 
     * @throws IOException
     */
    @Test
    public void onePixelDifferenceBlack()
    {
        T.match("colorfuzzy/white-35x35.png").to("colorfuzzy/white-35x35-1pixel-1x1.png").isNotEqual()
         .hasMarking("colorfuzzy/onePixelDifferenceBlack.png");
    }

    /**
     * Test default, difference close to being a problem s
     * 
     * @throws IOException
     */
    @Test
    public void grayIncreasingDefaultEqual() throws IOException
    {
        BufferedImage b = createTestImageGradient(Color.BLACK, 5, 5, 5);
        BufferedImage c = createTestImageGradient(new Color(25, 25, 25), 5, 5, 5);
        T.match(b).to(c).isEqual();
    }

    /**
     * Test color difference 1 that should not trigger anything
     *
     * @throws IOException
     */
    @Test
    public void grayIncreasingDefaultEqual_Color10() throws IOException
    {
        BufferedImage b = createTestImageGradient(Color.BLACK, 5, 5, 5);
        BufferedImage c = createTestImageGradient(new Color(50, 50, 91), 5, 5, 5);

        T = new TestCompare(new ColorFuzzy(1.0), m, mX, mY);

        T.match(b).to(c).isEqual();
    }

    /**
     * Test with color difference 0, hence it is always different enough
     *
     * @throws IOException
     */
    @Test
    public void grayIncreasingDefaultNotEqual_Color00() throws IOException
    {
        BufferedImage b = createTestImageGradient(Color.BLACK, 5, 5, 5);
        BufferedImage c = createTestImageGradient(new Color(25, 25, 25), 5, 5, 5);

        T = new TestCompare(new ColorFuzzy(0.0), m, 3, 3);

        T.match(b).to(c).isNotEqual().hasMarking("colorfuzzy/grayIncreasingDefaultNotEqual_Color00.png");
    }

    /**
     * Test default, we have a difference that is enough to trigger something
     *
     * @throws IOException
     */
    @Test
    public void grayIncreasingDefaultNotEqual() throws IOException
    {
        BufferedImage b = createTestImageGradient(Color.BLACK, 5, 5, 5);
        BufferedImage c = createTestImageGradient(new Color(30, 30, 30), 5, 5, 5);

        T = new TestCompare(a, m, 3, 3);

        T.match(b).to(c).isNotEqual().hasMarking("colorfuzzy/grayIncreasingDefaultNotEqual.png");
    }

    /**
     * Test default, we have a difference that is enough to trigger something
     *
     * @throws IOException
     */
    @Test
    public void gradient2DBlack_Color01() throws IOException
    {
        BufferedImage b = createTestImage2DGradient(Color.BLACK, Color.BLUE);

        T = new TestCompare(a, m, 1, 1);

        T.match(b).to("colorfuzzy/black-256x256.png").isNotEqual().hasMarking("colorfuzzy/gradient2DBlack_Color01.png");
    }

    /**
     * Test default, we have a difference that is enough to trigger something
     *
     * @throws IOException
     */
    @Test
    public void gradient2DBlack_Color05() throws IOException
    {
        BufferedImage b = createTestImage2DGradient(Color.BLACK, Color.BLUE);

        T = new TestCompare(new ColorFuzzy(0.5), m, 1, 1);

        T.match(b).to("colorfuzzy/black-256x256.png").isNotEqual().hasMarking("colorfuzzy/gradient2DBlack_Color05.png");
    }

    /**
     * Test default, ok, change the color spectrum
     *
     * @throws IOException
     */
    @Test
    public void gradient2DRed_Color05() throws IOException
    {
        BufferedImage b = createTestImage2DGradient(Color.BLACK, Color.GREEN);

        T = new TestCompare(new ColorFuzzy(0.5), m, 1, 1);

        T.match(b).to("colorfuzzy/red-256x256.png").isNotEqual().hasMarking("colorfuzzy/gradient2DRed_Color05.png", Color.BLUE);
    }

    /**
     * Test default, ok, change the color spectrum
     *
     * @throws IOException
     */
    @Test
    public void gradient2DRed_2_Color05() throws IOException
    {
        BufferedImage b = createTestImage2DGradient(Color.BLACK, Color.GREEN);

        T = new TestCompare(new ColorFuzzy(0.2), m, 1, 1);

        T.match("colorfuzzy/red-256x256.png").to(b).isNotEqual().hasMarking("colorfuzzy/gradient2DRed_2_Color05.png", Color.BLUE);
    }

    /**
     * Test default, ok, change the color spectrum. make sure pixel diff does not change a thing
     *
     * @throws IOException
     */
    @Test
    public void gradient2DRed_2_Color05_PixelDiffSet() throws IOException
    {
        BufferedImage b = createTestImage2DGradient(Color.BLACK, Color.GREEN);

        T = new TestCompare(new ColorFuzzy(0.2), m, 1, 1);

        T.match("colorfuzzy/red-256x256.png").to(b).isNotEqual().hasMarking("colorfuzzy/gradient2DRed_2_Color05.png", Color.BLUE);
    }
}
