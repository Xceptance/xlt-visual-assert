package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;

public class TimageComparisonMask {
//	Tests if the mask is working as expected. Specifically, it tests if a difference in the images is marked and detected 
//	if the mask image is black and if a difference in the images is detected if the mask image is white.
//	Separate tests for fuzzyEqual, pixelFuzzyEqual and exactlyEqual
	
//	For that, the reference image is fully black while the screenshot image is black up to row 200, then white between
//	rows 200 and 300. The maskimage is black from row 250 to row 300.
	
//	The tests test if the resulting markedImage is marked between rows 250 and 300 (it shoudn't be)
//	and if the resulting markedImage is marked between rows 200 and 250 (it should be)
	
		private static BufferedImage reference;
		private static BufferedImage screenshot;
		
		private final static File directory = SystemUtils.getJavaIoTmpDir();
		private static File fileMask = new File(directory, "/fileMask.png");
		private static File fileOut = new File(directory, "/fileOut.png");
		
		private final static int rgbBlack = Color.BLACK.getRGB();
		private final static int rgbWhite = Color.WHITE.getRGB();
		private final int rgbMarked = Color.RED.getRGB();
				
//		Initializes the reference, screenshot and the maskImage;
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
				for (int h=0; h<screenshot.getHeight(); h++) {
					if (h >= 200) { 
						screenshot.setRGB(w, h, rgbWhite);
					}
					else {
						screenshot.setRGB(w, h, rgbBlack);
					}
				}	
			}
			
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
//		Tests the normal fuzzyEqual method
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
//		Tests the normal fuzzyEqual method
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
//		Tests the exactlyEqual method
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
//		Tests the exactlyEqual method
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
//		Tests the pixelFuzzyEqual method
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
//		Tests the pixelFuzzyEqual method
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
		
//		Deletes the created files after the test.
		@AfterClass
		public static void deleteFile() {
			fileMask.delete();
			fileOut.delete();
		}
}
