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
	private void assertDoubleRounded(final double expected, final double actual)
	{
		final int rounded = (int) (actual * 1000);
		Assert.assertEquals((int) (expected * 1000), rounded);
	}

	/**
	 * No difference
	 */
	@Test
	public final void colorDifference_Same()
	{
		assertDoubleRounded(0.0, ImageComparison.calculatePixelRGBDiff(Color.BLACK.getRGB(), Color.BLACK.getRGB()));
	}
	/**
	 * Full difference
	 */
	@Test
	public final void colorDifference_FullDifference()
	{
		assertDoubleRounded(1.0, ImageComparison.calculatePixelRGBDiff(Color.BLACK.getRGB(), Color.WHITE.getRGB()));
	}
	/**
	 * Full difference
	 */
	@Test
	public final void colorDifference_SmallDifference()
	{
		assertDoubleRounded(0.960, ImageComparison.calculatePixelRGBDiff(Color.WHITE.getRGB(), new Color(10,10,10).getRGB()));
	}
}
