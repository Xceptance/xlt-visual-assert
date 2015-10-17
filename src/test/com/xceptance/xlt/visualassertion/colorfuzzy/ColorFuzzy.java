package test.com.xceptance.xlt.visualassertion.colorfuzzy;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.junit.Test;

import test.com.xceptance.xlt.visualassertion.ImageTest;

/**
 * Tests the exact compare mode.
 * 
 * @author rschwietzke
 *
 */
public class ColorFuzzy extends ImageTest
{
	/**
	 * Test default, no difference
	 * @throws IOException
	 */
	@Test
	public void sameSimple() 
	{
		new TestCompare().
		colorFuzzy("blank.png").to("blank.png").
		isEqual();
	}
	/**
	 * Test default, no difference
	 * @throws IOException
	 */
	@Test
	public void samePhoto() 
	{
		new TestCompare().
		colorFuzzy("photo.png").to("photo.png").
		isEqual();
	}

	/**
	 * Test default, one pixel diff
	 * @throws IOException
	 */
	@Test
	public void onePixelDifferenceBlack() 
	{
		new TestCompare().
		colorFuzzy("white-35x35.png").to("white-35x35-1pixel-1x1.png").
		isNotEqual().hasMarking("onePixelDifferenceBlack.png");
	}

	/**
	 * Test default, difference close to being a problem
	 * @throws IOException
	 */
	@Test
	public void grayIncreasingDefaultEqual() throws IOException 
	{
		final File b = createTestImageGradient(Color.BLACK, 5, 5, 5);
		final File c = createTestImageGradient(new Color(25,25,25), 5, 5, 5);
		new TestCompare().colorFuzzy(b).to(c).isEqual();
	}

	/**
	 * Test color difference 1 that should not trigger anything
	 * @throws IOException
	 */
	@Test
	public void grayIncreasingDefaultEqual_Color10() throws IOException 
	{
		final File b = createTestImageGradient(Color.BLACK, 5, 5, 5);
		final File c = createTestImageGradient(new Color(50,50,91), 5, 5, 5);
		new TestCompare().colorFuzzy(b).to(c).colorDifference(1.0).isEqual();
	}

	/**
	 * Test with color difference 0, hence it is always different enough
	 * @throws IOException
	 */
	@Test
	public void grayIncreasingDefaultNotEqual_Color00() throws IOException 
	{
		final File b = createTestImageGradient(Color.BLACK, 5, 5, 5);
		final File c = createTestImageGradient(new Color(25,25,25), 5, 5, 5);
		new TestCompare().colorFuzzy(b).to(c).mark(3, 3).colorDifference(0.0).isNotEqual().hasMarking("grayIncreasingDefaultNotEqual_Color00.png");
	}

	/**
	 * Test default, we have a difference that is enough to trigger something
	 * @throws IOException
	 */
	@Test
	public void grayIncreasingDefaultNotEqual() throws IOException 
	{
		final File b = createTestImageGradient(Color.BLACK, 5, 5, 5);
		final File c = createTestImageGradient(new Color(30,30,30), 5, 5, 5);
		new TestCompare().colorFuzzy(b).to(c).mark(3, 3).isNotEqual().hasMarking("grayIncreasingDefaultNotEqual.png");
	}

	/**
	 * Test default, we have a difference that is enough to trigger something
	 * @throws IOException
	 */
	@Test
	public void gradient2DBlack_Color01() throws IOException 
	{
		final File b = createTestImage2DGradient(Color.BLACK);
		new TestCompare().colorFuzzy(b).to("black-256x256.png").mark(1, 1).isNotEqual().hasMarking("gradient2DBlack_Color01.png");
	}

	/**
	 * Test default, we have a difference that is enough to trigger something
	 * @throws IOException
	 */
	@Test
	public void gradient2DBlack_Color05() throws IOException 
	{
		final File b = createTestImage2DGradient(Color.BLACK);
		new TestCompare().colorFuzzy(b).to("black-256x256.png").mark(1, 1).colorDifference(0.5).isNotEqual().hasMarking("gradient2DBlack_Color05.png");
	}	
}
