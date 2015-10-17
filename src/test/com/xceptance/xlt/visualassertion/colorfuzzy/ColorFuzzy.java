package test.com.xceptance.xlt.visualassertion.colorfuzzy;

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
	 * Test default, no difference
	 * @throws IOException
	 */
	@Test
	public void onePixelDifferenceBlack() 
	{
		new TestCompare().
		colorFuzzy("white-35x35.png").to("white-35x35-1pixel-1x1.png").
		isNotEqual();
	}

}
