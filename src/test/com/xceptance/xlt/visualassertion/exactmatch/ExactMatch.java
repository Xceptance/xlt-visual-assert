package test.com.xceptance.xlt.visualassertion.exactmatch;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import test.com.xceptance.xlt.visualassertion.ImageTest;

import com.xceptance.xlt.visualassertion.ImageComparison.Algorithm;

/**
 * Tests the exact compare mode.
 * 
 * @author rschwietzke
 *
 */
public class ExactMatch extends ImageTest
{
	@Test
	public void blank() throws IOException
	{
		new TestCompare().
		match("blank.png").to("blank.png").
		isEqual().hasNoMarking();
	}

	@Test
	public void photo() throws IOException
	{
		new TestCompare().
		match("photo.png").to("photo.png").
		isEqual().hasNoMarking();
	}

	@Test
	public void photoSameButDifferentFile() throws IOException
	{
		new TestCompare().
		match("photo.png").to("photo2.png").
		isEqual().hasNoMarking();
	}	

	@Test
	public void noMatchPixelDiff() throws IOException
	{
		new TestCompare().
		match("blank.png").to("oneblackpixel.png").
		isNotEqual().
		hasMarking("fivePixelsSmallerRect.png");

		final TestCompare tc = new TestCompare().compare(Algorithm.MATCH, "blank.png", "oneblackpixel.png");
		Assert.assertFalse(tc.result);
		Assert.assertTrue(compareFiles("noMatchPixelMaskExpected.png", tc.markedFileAsResult));
	}		

	@Test
	public void noMatchDifferentSize() throws IOException
	{
		final TestCompare tc = new TestCompare().compare(Algorithm.MATCH, "photo.png", "photo-205x205.png");
		Assert.assertFalse(tc.result);
		Assert.assertTrue(compareFiles("photo-205x205-MaskExpected.png", tc.markedFileAsResult));
	}	

	@Test
	public void noMatchDifferentSizeReversed() throws IOException
	{
		final TestCompare tc = new TestCompare().compare(Algorithm.MATCH, "photo-205x205.png", "photo.png");
		Assert.assertFalse(tc.result);
		Assert.assertTrue(compareFiles("photo-205x205-ReversedMaskExpected.png", tc.markedFileAsResult));
	}		

	@Test
	public void noMatchNegated() throws IOException
	{
		final TestCompare tc = new TestCompare().compare(Algorithm.MATCH, "blank.png", "negated-blank.png");
		Assert.assertFalse(tc.result);
		Assert.assertTrue(compareFiles("negated-MaskExpected.png", tc.markedFileAsResult));
	}		
}
