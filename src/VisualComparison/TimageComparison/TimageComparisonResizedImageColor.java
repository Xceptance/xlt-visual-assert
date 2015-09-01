package VisualComparison.TimageComparison;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;


public class TimageComparisonResizedImageColor {
	
//	If one image is smaller then the other, the resizeImage method will adapt their size and fill the formerly nonexistent pixels 
//	with transparent black. This class tests whether or not the methods detect the difference between transparent black and black
	
//  This Test is supplemented by the TimageComparisonInfluenceAlpha Test, this one tests both alpha detection and resizing, but it does
//	not check alpha detection as thoroughly; there are some redundancies
	
	private BufferedImage reference;
	private BufferedImage screenshot;
		
	private String pathHome = System.getProperty("user.home");
	private File fileMask = new File(pathHome + "/maskImage.png");
	private File fileOut = new File(pathHome + "/output.png");
	
//	Initializes two black images, one with a size of 300*300px, the other 1*1px
	@Before
	public void initializeImages() throws IOException {
		BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		Color black = new Color (0, false); 
		int rgbBlack = black.getRGB();
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
		this.reference = reference;

		BufferedImage screenshot = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
					screenshot.setRGB(w, h, rgbBlack);
			}	
		}
		this.screenshot = screenshot;
		
	}

//  	Tests the exactly Equal method 
	@Test
	public void testExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("Failure,  images of different size shoudn't be equal - testExactlyEqual", false);
		}
	}	
	
//		Tests the pixelFuzzyEqual method with a very high threshold.
//		No matter how high the threshold, the pixelFuzzyEqual method will always return false if it detects a transparency 
	@Test
	public void testPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.9, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("Failure,  images of different size shoudn't be equal - testPixelFuzzyEqual", false);
		}
	}	
	
//	Tests the fuzzyEqual method with a threshold barely below one
	@Test
	public void testFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(2, 2, 0.99999999, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("Failure,  images of different size shoudn't be equal - testFuzzyEqual", false);
		}
	}	
	
//	Tests the fuzzyEqual method with a threshold of one. This should return true.
	@Test
	public void testFuzzyEqualThresholdOfOne() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(2, 2, 1, false);
		if (!imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("Failure, a threshold of one should return true - testFuzzyEqualThresholdOfOne", false);
		}
	}
	
	@After
	public void deleteFile() {
		fileMask.delete();
		fileOut.delete();
	}
}


