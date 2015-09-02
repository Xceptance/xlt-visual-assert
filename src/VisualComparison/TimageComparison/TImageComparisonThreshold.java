package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;


public class TImageComparisonThreshold {
//	Tests if the threshold is working as expected. 
//	Calculation: Reference Image with rgb of red, green and blue = 255 results in a maxDifference of 765 
//  Threshold of 0.2 = 20% -> Difference of 153
	
//	The Test initializes reference and screenshot image, then runs test methods with a threshold of exactly 0.2, just above 0.2
//																											and just below 0.2
//	Includes separate methods for pixelFuzzyEqual  

	
	private static BufferedImage reference;
	private static BufferedImage screenshot;

	
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	
	@BeforeClass
	public static void initializeImages() {
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		int red = 255;
		int green = 255;
		int blue = 255;
		int rgb = (red << 16) | (green << 8) | blue;
		
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgb);
			}
		}
		
		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		red = 255;
		green = 255;
		blue = 102;
		rgb = (red << 16) | (green << 8) | blue;
		
		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
				screenshot.setRGB(w, h, rgb);
			}
		}
	}
	
	
//	 	The following three methods test the normal fuzzyEqual function
	
	@Test	
	public void exactlyTwentyPercent() throws IOException {	
		ImageComparison imagecomparison = new ImageComparison(20, 20, 0.2, false);
		boolean result = imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertTrue("Threshold should be exactly equal to the difference - result: " + result, result);

	}
	
	@Test	
	public void barelyAboveTwentyPercent() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(20, 20, 0.200000000000000000000000000000000001, false);
		boolean result = imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertTrue("Threshold should be barely above difference - result: " + result, result);
	}
	
	
	@Test	
	public void barelyBelowTwentyPercent() throws IOException {										//Changes image value
		ImageComparison imagecomparison = new ImageComparison(20, 20, 0.1999999999999, false);
		boolean result = imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertFalse("Threshold should be barely below difference - result: " + result, result);
	}
	
//		The following three functions test the pixelFuzzyEqual method, ie pixel based comparison
	
	@Test	
	public void exactlyTwentyPercentPixelFuzzyEqual() throws IOException {	
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.2, false);
		boolean result = imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut); 
		Assert.assertTrue("The threshold should be exactly twenty percent - result: " + result, result);
	}
	
	@Test	
	public void barelyAboveTwentyPercentPixelFuzzyEqual() throws IOException { 					
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.200000000000000000000000000000000001, false);
		boolean result = imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertTrue("The threshold should be barely above twenty percent - result: " + result, result);	
	}
	
	@Test	
	public void barelyBelowTwentyPercentPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.19999999999, false);
		boolean result = imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertFalse("Threshold should be barely below twenty percent - result: " + result, result);
	}
	
//	Delete created files after tests
	@AfterClass 
	public static void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
	}
	
}
