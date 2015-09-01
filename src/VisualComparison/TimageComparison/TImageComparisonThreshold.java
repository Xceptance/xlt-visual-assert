package VisualComparison.TimageComparison;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;


public class TImageComparisonThreshold {
//	Tests if the threshold is working as expected. 
//	Calculation: Reference Image with rgb of red, green and blue = 255 results in a maxDifference of 765 
//  Threshold of 0.2 = 20% -> Difference of 153
	
//	The Test initializes reference and screenshot image, then runs test methods with a threshold of exactly 0.2, just above 0.2
//																											and just below 0.2
//	Includes separate methods for pixelFuzzyEqual  

	
	private BufferedImage reference;
	private BufferedImage screenshot;

	private String pathHome = System.getProperty("user.home");
	private File fileMask = new File(pathHome + "/maskImage.png");
	private File fileOut = new File(pathHome + "/output.png");
	
	@Before
	public void initializeImages() {
		BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		int red = 255;
		int green = 255;
		int blue = 255;
		int rgb = (red << 16) | (green << 8) | blue;
		
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgb);
			}
		}
		this.reference = reference;
		
		BufferedImage screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		red = 255;
		green = 255;
		blue = 102;
		rgb = (red << 16) | (green << 8) | blue;
		
		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
				screenshot.setRGB(w, h, rgb);
			}
		}
		this.screenshot = screenshot;
	}
	
	
//	 	The following three methods test the normal fuzzyEqual function
	
	@Test	
	public void exactlyTenPercent() throws IOException {	
		ImageComparison imagecomparison = new ImageComparison(20, 20, 0.2, false);
		if (!imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("Threshold should be equal to difference-exactlyTreshold", false);
		}
	}
	
	@Test	
	public void barelyAboveTenPercent() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(20, 20, 0.200000000000000000000000000000000001, false);
		if (!imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("Threshold should be barely above difference-barelyAboveTresholdt", false);
		}
	}
	
	@Test	
	public void barelyBelowTenPercent() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(20, 20, 0.19999999999999999, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("Threshold should be barely below difference-barelyBelowThreshold", false);
		}
	}
	
//		The following three functions test the pixelFuzzyEqual method, ie pixel based comparison
	
	@Test	
	public void exactlyTenPercentPixelFuzzyEqual() throws IOException {	
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.2, false);
		if (!imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("An unexpected result in TImageComparisonThreshold.exactlyTenPercent", false);
		}
	}
	
	@Test	
	public void barelyAboveTenPercentPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.200000000000000000000000000000000001, false);
		if (!imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("An unexpected result in TImageComparisonThreshold.barelyAboveTenPercent", false);
		}
	}
	
	@Test	
	public void barelyBelowTenPercentPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.19999999999999999, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("An unexpected result in TImageComparisonThreshold.barelyBelowTenPercent", false);
		}
	}
	
//	Delete created files after tests
	@After 
	public void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
	}
	
}
