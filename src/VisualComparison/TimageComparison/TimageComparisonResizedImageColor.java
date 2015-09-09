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
 * If one image is smaller then the other, the resizeImage method will adapt
 * their size and fill the formerly nonexistent pixels with transparent black.
 * This class tests whether or not the relevant methods detect the difference
 * between transparent and opaque black.
 * 
 * This Test is supplemented by the TimageComparisonInfluenceAlpha Test. This
 * tests both alpha detection and resizing, but it does not check alpha
 * detection as thoroughly; there are some redundancies between both tests.
 * 
 * @author damian
 * 
 */
public class TimageComparisonResizedImageColor {

	private static BufferedImage reference;
	private static BufferedImage screenshot;

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");

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
	 * despite a threshold barely below 1. 
	 * ImageComparison parameters: 1, 1, 0.9999999, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void testFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10,
				0.99999999, false, "FUZZYEQUAL");
		boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut);
		Assert.assertFalse(
				"Failure,  images of different size shoudn't be equal - testFuzzyEqual",
				result);
	}

	/**
	 * Tests if two black images of different sizes are recognized as different
	 * with a threshold of 1. They should NOT be. A threshold of 1 should always
	 * return true. ImageComparison parameters: 10, 10, 1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void testFuzzyEqualThresholdOfOne() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 1, false, "FUZZYEQUAL");
		boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut);
		Assert.assertTrue(
				"Failure, a threshold of one should return true - testFuzzyEqualThresholdOfOne",
				result);
	}

	/**
	 * Tests if two black images of different sizes are recognized as different
	 * despite a threshold barely below 1.
	 * ImageComparison parameters: 1, 1, 0.9999999, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.999,
				false, "PIXELFUZZYEQUAL");
		boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut);
		Assert.assertFalse(
				"Failure,  images of different size shoudn't be equal - testPixelFuzzyEqual",
				result);
	}

	/**
	 * Tests if two black images of different sizes are recognized as different
	 * despite a threshold barely below 1 They should NOT be. A threshold of 1
	 * should always return true. 
	 * ImageComparison parameters: 1, 1, 1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void testPixelFuzzyEqualThresholdOfOne() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, false, "PIXELFUZZYEQUAL");
		boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut);
		Assert.assertTrue(
				"A threshold of 1 should always return true - testPixelFuzzyEqual",
				result);
	}

	/**
	 * Tests if two black images of different sizes are recognized as different.
	 * ImageComparison parameters: 1, 1, 0.00, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void testExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.00, false, "EXACTLYEQUAL");
		boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut);
		Assert.assertFalse(
				"Failure,  images of different size shoudn't be equal - testExactlyEqual",
				result);
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
