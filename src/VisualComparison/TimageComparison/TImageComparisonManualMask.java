package VisualComparison.TimageComparison;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;


public class TImageComparisonManualMask {
	
//	This class tests whether or not you can manually manipulate the mask image, 
//	ie if it does what it should when you paint it black/ white with a image editor
//	It is not for automated testing
//	The initialized Images are completely different at the start
	
//	Includes a method that automatically deletes the maskImages in case this test is used in batch tests
	
	private static BufferedImage reference;
	private static BufferedImage screenshot;
	
	private static String pathHome = System.getProperty("user.home");
	private static File fileMask = new File(pathHome + "/maskImage.png");
	private static File fileOut = new File(pathHome + "/output.png");
	
	private final static int rgbWhite = Color.WHITE.getRGB();
	private final static int rgbBlack = Color.BLACK.getRGB();
	
	@BeforeClass
	public static void initializeImages() throws IOException {
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
		
		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<(screenshot.getHeight()/2); h++) {
				screenshot.setRGB(w, h, rgbWhite);
			}	
		}	
	}
	
//	This method has to be changed manually to do any work, set traininMode = false after you've initialized the mask image
	@Test	
	public void test() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 10, 0.1, true);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			System.out.println("Images are equal");
		}
		else {
			System.out.println("Images arn't equal");
		}			
	}
	
//	This method deletes the maskFile after is was used, so there won't be difficulties if this test
//	is part of a batch test. In that case, it will be ineffective.
	@AfterClass
	public static void deleteFiles() {
		fileMask.delete();
	}
}