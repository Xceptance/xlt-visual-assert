package test.com.xceptance.visualassertion;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.visualassertion.ImageComparison;

/**
 * If one image is smaller then the other, the resizeImage method will adapt
 * their size and fill the formerly nonexistent pixels with transparent black.
 * 
 * This test tests if the previously nonexistent parts in the reference image
 * or the screenshot were marked.
 * 
 * @author damian
 * 
 */
public class TResizedImageColor {

	private static BufferedImage reference;
	private static BufferedImage screenshot;

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");

	private final static int rgbBlack = Color.BLACK.getRGB();

	@BeforeClass
	public static void initializeImages() throws IOException {

		// Initializes two black images, one with a size of 300*300px, the other
		// with a size of 30*30
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}

		screenshot = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < screenshot.getWidth(); w++) {
			for (int h = 0; h < screenshot.getHeight(); h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}

	}

	/**
	 * Tests if two black images of different sizes are recognized as different
	 * despite colTolerance significantly above 1. They should be.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDifferentSizeT() throws IOException {
		final ImageComparison imagecomparison = new ImageComparison(10,
				10, 10, 10, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut, differenceFile);
		Assert.assertFalse(
				"Images of different size shoudn't be equal",
				result);
	}

	/**
	 * Tests if two black images of different sizes are recognized as different 
	 * with a colTolerance value of 0.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testDifferentSize() throws IOException {
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.00, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut, differenceFile);
		Assert.assertFalse(
				"Images of different size shoudn't be equal",
				result);
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
