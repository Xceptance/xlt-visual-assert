package test.com.xceptance.xlt.visualassertion.fuzzy;

import java.io.IOException;

import org.junit.Test;

import test.com.xceptance.xlt.visualassertion.ImageTest;

/**
 * Tests the exact compare mode.
 * 
 * @author rschwietzke
 *
 */
public class Fuzzy extends ImageTest
{
	/**
	 * Test default, no difference
	 * @throws IOException
	 */
	@Test
	public void sameSimple() 
	{
		new TestCompare().
		fuzzy("blank.png").to("blank.png").
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
		fuzzy("photo.png").to("photo.png").
		isEqual();
	}

	/**
	 * Test default, no difference limit reached
	 * @throws IOException
	 */
	@Test
	public void diffPixel_01_10of100() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-10diff.png").
		isEqual();
	}

	/**
	 * Test default, difference limit reached
	 * @throws IOException
	 */
	@Test
	public void diffPixel_01_11of100() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-11diff.png").
		isNotEqual().hasMarking("diffPixel_01_11of100.png");
	}

	/**
	 * Test default, color difference limit not reached
	 * @throws IOException
	 */
	@Test
	public void diffPixel_01_10of100_colorUnderLimit() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-10diff-colorUnderLimit.png").
		isEqual();
	}

	/**
	 * Test default, no difference limit reached
	 * @throws IOException
	 */
	@Test
	public void diffPixel_01_11of100_colorUnderLimit() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-11diff-colorUnderLimit.png").
		isEqual();
	}

	/**
	 * Test default, no difference limit reached
	 * @throws IOException
	 */
	@Test
	public void diffPixel_01_100of100_colorUnderLimit() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-100diff-colorUnderLimit.png").
		isEqual();
	}

	/**
	 * Test default, no difference limit reached
	 * @throws IOException
	 */
	@Test
	public void diffPixel_01_100of100_colorUnderLimit_10of100_over() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-100of100_colorUnderLimit_10of100_over.png").
		isEqual();
	}

	/**
	 * Test default, no difference limit reached
	 * @throws IOException
	 */
	@Test
	public void diffPixel_01_100of100_colorUnderLimit_11of100_over() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-100of100_colorUnderLimit_11of100_over.png").
		isNotEqual();
	}

	/**
	 * Test no tolerance
	 * @throws IOException
	 */
	@Test
	public void pixel_00_color_00() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10.png").
		colorDifference(0.0).
		pixelDifference(0.0).
		isEqual();
	}

	/**
	 * Test no tolerance, one pixel diff
	 * @throws IOException
	 */
	@Test
	public void pixel_00_color_00_1_pixeldiff() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-1diff-coloroverlimit.png").
		colorDifference(0.0).
		pixelDifference(0.0).
		isNotEqual();
	}

	/**
	 * Test no tolerance, one pixel diff, very low diff
	 * @throws IOException
	 */
	@Test
	public void pixel_00_color_00_1_pixeldiff_low_color() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10-1diff-colordiffverylowlimit.png").
		colorDifference(0.0).
		pixelDifference(0.0).
		isNotEqual();
	}	

	/**
	 * Test more tolerance but on a smaller area
	 * @throws IOException
	 */
	@Test
	public void pixel_02_color_02_dimension_3_noDifference() 
	{
		new TestCompare().
		fuzzy("white-10x10.png").to("white-10x10.png").
		colorDifference(0.1).
		pixelDifference(0.1).
		fuzzyBlockDimension(3).
		isEqual();
	}	

	/**
	 * Test more tolerance but on a smaller area
	 * @throws IOException
	 */
	@Test
	public void pixel_02_color_02_dimension_3_difference_1_pixel() 
	{
		new TestCompare().fuzzy("white-10x10.png").to("white-10x10-1pixel.png").colorDifference(0.1).pixelDifference(0.1).fuzzyBlockDimension(3).isNotEqual(); // 0.9 pixels diff
		new TestCompare().fuzzy("white-10x10.png").to("white-10x10-1pixel.png").colorDifference(0.1).pixelDifference(0.11).fuzzyBlockDimension(3).isNotEqual(); // 1/9 pixels diff
		new TestCompare().fuzzy("white-10x10.png").to("white-10x10-1pixel.png").colorDifference(0.1).pixelDifference(0.12).fuzzyBlockDimension(3).isEqual(); // > 1/9 pixels diff
	}	


}

