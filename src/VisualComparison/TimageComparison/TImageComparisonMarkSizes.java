package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;

/**
 * Tests the correct behavior of ImageComparison for differently sized images
 * 
 * @author Lucas
 * 
 */
public class TImageComparisonMarkSizes {
	static BufferedImage reference, slim, wide, low, high, slimAndLow,
			wideAndHigh, switched;
	ImageComparison imgCompare = new ImageComparison(2, 2, 0.00, false);
	static File directory = org.apache.commons.lang3.SystemUtils
			.getJavaIoTmpDir();
	static File outPutfile = new File(directory + "/test.png");
	static File maskFile = new File(directory + "/mask.png");

	/**
	 * Sets up the images for the test
	 */
	@BeforeClass
	public static void setup() {
		reference = new BufferedImage(8, 10, BufferedImage.TYPE_INT_ARGB);
		paintBlack(reference);
		slim = new BufferedImage(6, 10, BufferedImage.TYPE_INT_ARGB);
		paintBlack(slim);
		wide = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		paintBlack(wide);
		low = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
		paintBlack(low);
		high = new BufferedImage(8, 12, BufferedImage.TYPE_INT_ARGB);
		paintBlack(high);
		slimAndLow = new BufferedImage(6, 8, BufferedImage.TYPE_INT_ARGB);
		paintBlack(slimAndLow);
		wideAndHigh = new BufferedImage(10, 12, BufferedImage.TYPE_INT_ARGB);
		paintBlack(wideAndHigh);
		switched = new BufferedImage(10, 8, BufferedImage.TYPE_INT_ARGB);
		paintBlack(switched);
	}

	/**
	 * Reference image is wider than the new screenshot
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceMoreWidth() throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, slim, maskFile,
				outPutfile));
	}

	/**
	 * Reference image is less wide than the new screenshot
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceLessWidth() throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, wide, maskFile,
				outPutfile));
	}

	/**
	 * Reference image is higher than the new screenshot
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceMoreHeight() throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, low, maskFile,
				outPutfile));
	}

	/**
	 * Reference image is less high than the new screenshot
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceLessHeight() throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, high, maskFile,
				outPutfile));
	}

	/**
	 * Reference picture is wider and higher
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceBothMore() throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, slimAndLow,
				maskFile, outPutfile));
	}

	/**
	 * New screenshot is wider and higher
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceBothLess() throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, wideAndHigh,
				maskFile, outPutfile));
	}

	/**
	 * Width and height of the pictures are switched
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceBroadFormat() throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, switched, maskFile,
				outPutfile));
	}

	@AfterClass
	public static void deleteFiles() {
		outPutfile.delete();
		maskFile.delete();
	}

	/**
	 * Method for painting the images black
	 * 
	 * @param img
	 */
	public static void paintBlack(BufferedImage img) {
		int rgb = Color.BLACK.getRGB();
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				img.setRGB(x, y, rgb);
			}
		}
	}

	/**
	 * Method for painting the images white
	 * 
	 * @param img
	 */
	public static void paintWhite(BufferedImage img) {
		int rgb = Color.WHITE.getRGB();
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				img.setRGB(x, y, rgb);
			}
		}
	}
}