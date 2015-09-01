package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;

public class TimageComparisonMask {
//	Tests if the mask is working as expected. Specifically, it tests if a difference in the images is marked and detected if 
//	The mask image is black and if a difference in the images is detected if the mask image is white.
//	Tests fuzzyEqual, exactlyFuzzyEqual and exactlyEqual
	
		private BufferedImage reference;
		private BufferedImage screenshot;
		
		private int rgbBlack = Color.BLACK.getRGB();
		private int rgbWhite = Color.WHITE.getRGB();
		private int rgbMarked = Color.RED.getRGB();
		
		
		private String pathHome = System.getProperty("user.home");
		private File fileMask = new File(pathHome + "/maskImage.png");
		private File fileOut = new File(pathHome + "/output.png");
		
		
//		Initializes the reference, screenshot and maskimages;
		@Before
		public void initializeImages() throws IOException {
			BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

			for (int w=0; w<reference.getWidth(); w++) { 
				for (int h=0; h<reference.getHeight(); h++) {
					reference.setRGB(w, h, rgbBlack);
				}
			}
			this.reference = reference;
			
			BufferedImage screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
			for (int w=0; w<screenshot.getWidth(); w++) { 
				for (int h=0; h<screenshot.getHeight(); h++) {
					if (h >= 200) { 
						screenshot.setRGB(w, h, rgbWhite);
					}
					else {
						screenshot.setRGB(w, h, rgbBlack);
					}
				}	
			}
			this.screenshot = screenshot;
			
			BufferedImage maskImage = new BufferedImage(300, 300,BufferedImage.TYPE_INT_BGR);
			for (int w=0; w<screenshot.getWidth(); w++) { 
				for (int h=0; h<screenshot.getHeight(); h++) {
					if (h >= 250) { 
						maskImage.setRGB(w, h, rgbBlack);
					}
					else {
						maskImage.setRGB(w, h, rgbWhite);
					}
				}	
			}
			ImageIO.write(maskImage, "PNG", fileMask);
		}	
		
//		Checks if the masked parts where NOT marked
		@Test
		public void changesCorrectlyHidden() throws IOException {
			ImageComparison imagecomparison = new ImageComparison(10, 10, 0.1, false);
			imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
			BufferedImage output = ImageIO.read(fileOut);
			
			for (int w=0; w<reference.getWidth(); w++) {
				for (int h=250; h<reference.getHeight(); h++) {
					Assert.assertEquals(rgbWhite, output.getRGB(w, h));
				}
			}
		}
		
//		Checks if the parts that wern't masked are marked
		@Test
		public void changesCorrectlyNotHidden() throws IOException {
			ImageComparison imagecomparison = new ImageComparison(10, 10, 0.1, false);
			imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
			BufferedImage output = ImageIO.read(fileOut);
			boolean hasRed = false;
			
			for (int w=0; w<reference.getWidth(); w++) {
				for (int h=200; h<250; h++) {
					if (rgbMarked == output.getRGB(w, h)) {
						hasRed = true;
					}
				}
			}
			Assert.assertTrue("Unmasked part with changes should be marked", hasRed);
		}
		
//		Checks if the masked parts where NOT marked
		@Test
		public void changesCorrectlyHiddenExactlyEqual() throws IOException {
			ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
			imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
			BufferedImage output = ImageIO.read(fileOut);
			
			for (int w=0; w<reference.getWidth(); w++) {
				for (int h=250; h<reference.getHeight(); h++) {
					Assert.assertEquals(rgbWhite, output.getRGB(w, h));
				}
			}
		}
		
//		Checks if the parts that wern't masked are marked
		@Test
		public void changesCorrectlyNotHiddenExactlyEqual() throws IOException {
			ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
			imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
			BufferedImage output = ImageIO.read(fileOut);
			
			for (int w=0; w<reference.getWidth(); w++) {
				for (int h=200; h<250; h++) {
					Assert.assertEquals(rgbMarked, output.getRGB(w, h));
				}
			}
		}
		
//		Checks if the masked parts where NOT marked
		@Test
		public void changesCorrectlyPixelFuzzyEqual() throws IOException {
			ImageComparison imagecomparison = new ImageComparison(1, 1, 0.1, false);
			imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
			BufferedImage output = ImageIO.read(fileOut);
			
			for (int w=0; w<reference.getWidth(); w++) {
				for (int h=250; h<reference.getHeight(); h++) {
					Assert.assertEquals(rgbWhite, output.getRGB(w, h));
				}
			}
		}
		
//		Checks if the parts that wern't masked are marked
		@Test
		public void changesCorrectlyNotHiddenPixelFuzzyEqual() throws IOException {
			ImageComparison imagecomparison = new ImageComparison(1, 1, 0.1, false);
			imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
			BufferedImage output = ImageIO.read(fileOut);
			
			for (int w=0; w<reference.getWidth(); w++) {
				for (int h=200; h<250; h++) {
					Assert.assertEquals(rgbMarked, output.getRGB(w, h));
				}
			}
		}
		
		@After
		public void deleteFile() {
			fileMask.delete();
			fileOut.delete();
		}
}
