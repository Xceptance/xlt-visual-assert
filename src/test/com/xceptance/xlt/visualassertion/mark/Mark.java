package test.com.xceptance.xlt.visualassertion.mark;

import java.io.IOException;

import org.junit.Test;

import test.com.xceptance.xlt.visualassertion.ImageTest;

/**
 * Tests the exact compare mode.
 * 
 * @author rschwietzke
 *
 */
public class Mark extends ImageTest
{
	/**
	 * Test default marking
	 * @throws IOException
	 */
	@Test
	public void pixel0x0() 
	{
		new TestCompare().
		match("white-35x35.png").to("white-35x35-1pixel-0x0.png").
		isNotEqual().
		hasMarking("pixel0x0.png");
	}

	/**
	 * Test default marking, one pixel in
	 * @throws IOException
	 */
	@Test
	public void pixel1x1()
	{
		new TestCompare().
		match("white-35x35.png").to("white-35x35-1pixel-1x1.png").
		isNotEqual().
		hasMarking("pixel1x1.png");
	}

	/**
	 * Test default marking, four pixels of all corners
	 * @throws IOException
	 */
	@Test
	public void pixels10x10() 
	{
		new TestCompare().
		match("white-35x35.png").to("white-35x35-4pixels-10x10.png").
		isNotEqual().
		hasMarking("pixels10x10.png");
	}	

	/**
	 * Pixels in corners and one in the middle, shows smaller rectangles due
	 * to image size not being a total of 10.
	 * @throws IOException
	 */
	@Test
	public void fivePixelsSmallerRect() 
	{
		new TestCompare().
		match("white-35x35.png").to("white-35x35-5pixels.png").
		isNotEqual().
		hasMarking("fivePixelsSmallerRect.png");
	}	

	/**
	 * Pixels in corners and one in the middle, shows smaller rectangles due
	 * to image size not being a total of 10.
	 * @throws IOException
	 */
	@Test
	public void fivePixelsMark5x5() 
	{
		new TestCompare().
		match("white-35x35.png").to("white-35x35-5pixels.png").mark(5,5).
		isNotEqual().
		hasMarking("fivePixelsMark5x5.png");
	}	

	/**
	 * Pixels in corners and one in the middle, mark every pixel
	 * @throws IOException
	 */
	@Test
	public void markPixelsOnly()
	{
		new TestCompare().
		match("white-35x35.png").to("white-35x35-5pixels.png").mark(1,1).
		isNotEqual().
		hasMarking("markPixelsOnly.png");
	}

	/**
	 * Pixels in corners and one in the middle, shows smaller rectangles due
	 * to image size not being a total of 10 and not square
	 * @throws IOException
	 */
	@Test
	public void markNonSquare() 
	{
		new TestCompare().
		match("white-35x35.png").to("white-35x35-5pixels.png").mark(5,3).
		isNotEqual().
		hasMarking("markNonSquare.png");
	}

	/**
	 * Pixels in corners and one in the middle, shows smaller rectangles due
	 * to image size not being a total of 10 and not square but very small
	 * @throws IOException
	 */
	@Test
	public void markNonSquareClosed() 
	{
		new TestCompare().
		match("white-35x35.png").to("white-35x35-5pixels.png").mark(2,2).
		isNotEqual().
		hasMarking("markNonSquareClosed.png");
	}

}
