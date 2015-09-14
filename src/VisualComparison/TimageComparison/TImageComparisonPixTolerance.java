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
 * Tests if the pixTolerance parameter for the fuzzy algorithm is working as expected.
 * Two images are initialized, 10*10 blocks and a difference every tenth pixel. 
 * It tests what happens with pixTolerance exactly 10%, barely below and barely above 10%.
 * 
 * @author daniel
 *
 */
public class TImageComparisonPixTolerance {
	
	private static BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
	private static BufferedImage screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);

	
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");
	
	private static int rgbBlack = Color.BLACK.getRGB();
	private static int rgbWhite = Color.WHITE.getRGB();
	
	@BeforeClass
	public static void initializeImages() {
		reference = new BufferedImage(301, 301, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
		
		screenshot = new BufferedImage(301, 301, BufferedImage.TYPE_INT_RGB);
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
	 * Tests what happens if the tolerance is equal to the differences.
	 * 
	 * @throws IOException
	 */
	@Test
	public void exactlyTen() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 0.1, 0.1, false, false, false, "FUZZY");
		boolean result = imagecomparison.isEqual(screenshot, reference, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}
	
	/**
	 * Tests what happens if the tolerance is below the differences.
	 * 
	 * @throws IOException
	 */
	@Test
	public void belowTen() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 0.1, 0.0999999999999, false, false, false, "FUZZY");
		boolean result = imagecomparison.isEqual(screenshot, reference, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}
	
	/**
	 * Tests what happens if the tolerance is above the differences.
	 * 
	 * @throws IOException
	 */
	@Test
	public void aboveTen() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 0.1, 0.10000000000000001, false, false, false, "FUZZY");
		boolean result = imagecomparison.isEqual(screenshot, reference, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
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
