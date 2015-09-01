package VisualComparison.TimageComparison;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;


//	Tests using the same methods as TimageComparisonThreshold, just different parameters
	public class TImageComparisonPixelFuzzyEqual {
		
		private BufferedImage reference;
		private BufferedImage screenshot;
		
		private String pathHome = System.getProperty("user.home");
		private File fileMask = new File(pathHome + "/maskImage.png");
		private File fileOut = new File(pathHome + "/output.png");
		

		@Before
		public void initializeImages() {
// The following is based on a calculation which says that a threshold of 0.2 is equivalent to a difference of 153 in red, green and/or blue		
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

		@Test	
		public void exactlyTenPercent() throws IOException {	
			ImageComparison imagecomparison = new ImageComparison(1, 1, 0.2, false);
			if (!imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
				Assert.assertTrue("An unexpected result in TImageComparisonThreshold.exactlyTenPercent", false);
			}
			
			fileMask.delete();
			fileOut.delete();
		}
		
		@Test	
		public void barelyAboveTenPercent() throws IOException {
			ImageComparison imagecomparison = new ImageComparison(1, 1, 0.200000000000000000000000000000000001, false);
			if (!imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
				Assert.assertTrue("An unexpected result in TImageComparisonThreshold.barelyAboveTenPercent", false);
			}
			
			fileMask.delete();
			fileOut.delete();
		}
		
		@Test	
		public void barelyBelowTenPercent() throws IOException {
			ImageComparison imagecomparison = new ImageComparison(1, 1, 0.19999999999999999, false);
			if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
				Assert.assertTrue("An unexpected result in TImageComparisonThreshold.barelyBelowTenPercent", false);
			}
			
			fileMask.delete();
			fileOut.delete();
		}
}
