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

/**
 * Tests whether or not the pixelPerBlock parameters are working as expected. 
 * Also tests if the method can handle it if the blocks would go over the border
 *
 * @author damian
 *
 */
public class TImageComparisonBlocks {
//	The initialized images have a difference every tenth pixel. 
//	With a threshold of 0.1, the methods should return true if there is one pixel per block.

	private static BufferedImage reference;
	private static BufferedImage screenshot;
	
	private final static int rgbBlack = Color.BLACK.getRGB();
	private final static int rgbWhite = Color.WHITE.getRGB();
	
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	
	@BeforeClass
	public static void initializeImages() {
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
		
		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
				if ((w % 10) == 0) { 
					screenshot.setRGB(w, h, rgbWhite);
				}	
				else {
					screenshot.setRGB(w, h, rgbBlack);
				}
			}	
		}
	}	
	
	/**
	 * Tests if the block calculations are working as expected 
	 * @throws IOException
	 */
	@Test	
	public void blocksCalculations() throws IOException {
//		The blocks are exactly ten pixels wide, there should be one difference every tenth pixel
		ImageComparison imagecomparison1 = new ImageComparison(10, 1, 0.1, false);
		boolean result = imagecomparison1.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertTrue("With these parameters, fuzzyEqual should be true - result: " + result, result);
		
//		The blocks are slightly wider then ten pixels and should shift relative to the image 
//		until there are two differences in one block
		ImageComparison imagecomparison2 = new ImageComparison(11, 1, 0.1, false);
		result = imagecomparison2.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertFalse("With these parameters, fuzzyEqual should be false - result: " + result, result);
		
//		The blocks are slightly smaller then ten pixels and should shift relative to he image
//		until there are two differences in one block
		ImageComparison imagecomparison3 = new ImageComparison(9, 1, 0.1, false);
		result = imagecomparison3.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertFalse("With these parameters, fuzzyEqual should be false - result: " + result, result);
	}
	
	/**
	 * Tests what happens if the blocks go over the border
	 * @throws IOException
	 */
	@Test				
	public void blocksGoOverBorderLeft() throws IOException {
//		This test what happens if the borders go over the edge on the right
//		It should also check the drawBorders method, since the treshold is so low, it should find differences everywhere
		ImageComparison imagecomparison1 = new ImageComparison(14, 1, 0.0000000000000001, false);
		boolean result = imagecomparison1.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertFalse("the drawBorders function could not be tested, " +
				"blocksGoOverBorderLeft didn't find a difference to mark", result);
		
//		This test tests what happens if the borders go over the edge on the bottom
//		It should also check the drawBorders method, since the treshold is so low, it should find differences everywhere
		ImageComparison imagecomparison2 = new ImageComparison(1, 28, 0.0000000000000001, false);
		result = imagecomparison2.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		Assert.assertFalse("the drawBorders function could not be tested, " +
				"blocksGoOverBorderLeft didn't find a difference to mark", result);
	}
	
	/**
	 * Deletes the temporary files which were created for this test
	 */
	@AfterClass
	public static void deleteFile() {
		fileMask.delete();
		fileOut.delete();
	}
}
