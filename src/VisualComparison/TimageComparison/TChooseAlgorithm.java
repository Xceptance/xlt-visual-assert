package VisualComparison.TimageComparison;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;

/**
 * Tests if the specified algorithm is chosen and if an exception is 
 * thrown in case the parameter for algorithm does not match any existing
 * algorithm. Like in case of typos.
 * 
 * The ImageComparison object is always initialized with the same
 * parameters for pixelPerBlockXY, threshold and trainingMode, 
 * only the parameter for the alorithm differs. 
 * 
 * @author daniel
 *
 */
public class TChooseAlgorithm {

		private static BufferedImage reference;
		private static BufferedImage screenshot;
		
		private final static int rgbBlack = new Color(0, 0, 0).getRGB();
		private final static int rgbAlmostBlack = new Color(0, 0, 1).getRGB();
		private final static int rgbWhite = Color.WHITE.getRGB();
		
		private final static File directory = SystemUtils.getJavaIoTmpDir();
		private static File fileMask = new File(directory, "/fileMask.png");
		private static File fileOut = new File(directory, "/fileOut.png");
		private static File differenceFile = new File(directory + "/difference.png");
		
		private ImageComparison exactComp = new ImageComparison(50, 0.2, 0.01, false, false, 3, 3, false, "EXACTLY");
		private final ImageComparison pixelFuzzyComp = new ImageComparison(50, 0.2, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		private final ImageComparison fuzzyComp = new ImageComparison(50, 0.2, 0.01, false, false, 3, 3, false, "FUZZY");


		@BeforeClass
		public static void initializeImages() {
			reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
			screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
			for (int w=0; w<reference.getWidth(); w++) { 
				for (int h=0; h<reference.getHeight(); h++) {
					reference.setRGB(w, h, rgbBlack);
					screenshot.setRGB(w, h, rgbBlack);
				}
			}
		}	
		
		@Test
		public void correctAlgorithm() throws IOException {
			initializeImages();
			
			//Change the screenshot image so one pixel differs a tiny bit.
			screenshot.setRGB(0, 0, rgbAlmostBlack);
			
			Assert.assertFalse(exactComp.isEqual(screenshot, reference, fileMask, fileOut, differenceFile));
			Assert.assertTrue(pixelFuzzyComp.isEqual(screenshot, reference, fileMask, fileOut, differenceFile));
			Assert.assertTrue(fuzzyComp.isEqual(screenshot, reference, fileMask, fileOut, differenceFile));
			
			//Change the screenshot image so one pixel differs a lot.
			screenshot.setRGB(0, 0, rgbWhite);
			Assert.assertFalse(pixelFuzzyComp.isEqual(screenshot, reference, fileMask, fileOut, differenceFile));
			Assert.assertTrue(fuzzyComp.isEqual(screenshot, reference, fileMask, fileOut, differenceFile));
			
			//For completions sake, change the screenshot so many pixels differ a lot, then try fuzzyEqual
			for (int x = 0; x < 26; x++) {
				for (int y = 0; y < 26; y++) {
					screenshot.setRGB(x, y, rgbWhite);
				}
			}
			Assert.assertFalse(fuzzyComp.isEqual(screenshot, reference, fileMask, fileOut, differenceFile));
		}
		
		@Test (expected = IllegalArgumentException.class)
		public void noAlgorithmFound() {
			initializeImages();
			exactComp = new ImageComparison(20, 0.2, 10, false, false, 3, 3, false, "exactlyEQUAL");
		
		}
		
		/**
		 * Deletes the temporary files which were created for this test
		 */
		@AfterClass
		public static void deleteFile() {
			fileMask.delete();
			fileOut.delete();
			differenceFile.delete();
		}
}
