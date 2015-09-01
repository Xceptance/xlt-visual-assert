package VisualComparison.TimageComparison;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;


public class TImageComparisonExactlyEqual {
//					The class tests if the exactlyEqual method catches the smallest possible difference
	
	private BufferedImage reference;
	private BufferedImage screenshot;
	
	private String pathHome = System.getProperty("user.home");
	private File fileMask = new File(pathHome + "/maskImage.png");
	private File fileOut = new File(pathHome + "/output.png");
	
// initializes two images, the only difference is that the rgb value of the top left pixel is smaller by one
	@Before
	public void initializeImages() {
		BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		Color black = Color.BLACK;
		int rgbBlack = black.getRGB();
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
		this.reference = reference;
		
		BufferedImage screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}	
		}
		screenshot.setRGB(0, 0, rgbBlack-1);
		this.screenshot = screenshot;
	}

//	Checks whether or not the exactlyEqual method catches the smallest difference possible 
	@Test
	public void almostEqualButNotQuite() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("The images arn't quite equal,  he apparently missed something - almostEqualButNotQuite", false);
		}
	}
}
