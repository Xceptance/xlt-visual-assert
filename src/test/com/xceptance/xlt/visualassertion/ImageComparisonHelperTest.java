package test.com.xceptance.xlt.visualassertion;

import java.awt.Color;

import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.visualassertion.ImageComparison;

/**
 * Test some of the utility methods in ImageComparison
 * @author rschwietzke
 *
 */
public class ImageComparisonHelperTest
{
	/**
	 * No difference
	 */
	@Test
	public final void colorDifference_Same()
	{
		Assert.assertTrue(0.0 == ImageComparison.calculatePixelRGBDiff(Color.BLACK.getRGB(), Color.BLACK.getRGB()));
	}
	/**
	 * Full difference
	 */
	@Test
	public final void colorDifference_FullDifference()
	{
		Assert.assertTrue(1.0 == ImageComparison.calculatePixelRGBDiff(Color.BLACK.getRGB(), Color.WHITE.getRGB()));
	}

}
