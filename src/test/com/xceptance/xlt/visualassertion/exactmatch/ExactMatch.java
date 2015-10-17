package test.com.xceptance.xlt.visualassertion.exactmatch;

import org.junit.Test;

import test.com.xceptance.xlt.visualassertion.ImageTest;

/**
 * Tests the exact compare mode.
 * 
 * @author rschwietzke
 *
 */
public class ExactMatch extends ImageTest
{
	@Test
	public void blank() 
	{
		new TestCompare().
		match("blank.png").to("blank.png").
		isEqual().hasNoMarking();
	}

	@Test
	public void photo() 
	{
		new TestCompare().
		match("photo.png").to("photo.png").
		isEqual().hasNoMarking();
	}

	@Test
	public void photoSameButDifferentFile() 
	{
		new TestCompare().
		match("photo.png").to("photo2.png").
		isEqual().hasNoMarking();
	}	

	@Test
	public void noMatchPixelDiff() 
	{
		new TestCompare().
		match("blank.png").to("oneblackpixel.png").
		isNotEqual().
		hasMarking("noMatchPixelDiff.png");
	}		

	@Test
	public void noMatchDifferentSize() 
	{
		new TestCompare().
		match("photo.png").to("photo-205x205.png").
		isNotEqual().
		hasMarking("photo-205x205-MaskExpected.png");
	}	

	@Test
	public void noMatchDifferentSizeReversed() 
	{
		new TestCompare().
		match("photo-205x205.png").to("photo.png").
		isNotEqual().
		hasMarking("photo-205x205-ReversedMaskExpected.png");
	}		

	@Test
	public void noMatchNegated() 
	{
		new TestCompare().
		match("blank.png").to("negated-blank.png").
		isNotEqual().
		hasMarking("negated-MaskExpected.png");
	}		
}
