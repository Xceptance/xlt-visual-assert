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
 * Tests the return value of ImageComparison for differently sized images.
 * It should return false for images of different sizes.
 * 
 * @author Lucas
 * 
 */
public class TMarkSizes {
	static BufferedImage reference, slim, wide, low, high, slimAndLow,
			wideAndHigh, switched;
	ImageComparison imgCompare = new ImageComparison(2, 0.00, 0.01, false, false, 3, 3, true, "FUZZY");
	static File directory = org.apache.commons.lang3.SystemUtils
			.getJavaIoTmpDir();
	static File outPutfile = new File(directory + "/test.png");
	static File maskFile = new File(directory + "/mask.png");
	private static File differenceFile = new File(directory + "/difference.png");

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
		Assert.assertFalse(imgCompare.isEqual(reference, slim, maskFile,
				outPutfile, differenceFile));
	}

	/**
	 * Reference image is less wide than the new screenshot
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceLessWidth() throws IOException {
		Assert.assertFalse(imgCompare.isEqual(reference, wide, maskFile,
				outPutfile, differenceFile));
	}

	/**
	 * Reference image is higher than the new screenshot
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceMoreHeight() throws IOException {
		Assert.assertFalse(imgCompare.isEqual(reference, low, maskFile,
				outPutfile, differenceFile));
	}

	/**
	 * Reference image is less high than the new screenshot
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceLessHeight() throws IOException {
		Assert.assertFalse(imgCompare.isEqual(reference, high, maskFile,
				outPutfile, differenceFile));
	}

	/**
	 * Reference picture is wider and higher
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceBothMore() throws IOException {
		Assert.assertFalse(imgCompare.isEqual(reference, slimAndLow,
				maskFile, outPutfile, differenceFile));
	}

	/**
	 * New screenshot is wider and higher
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceBothLess() throws IOException {
		Assert.assertFalse(imgCompare.isEqual(reference, wideAndHigh,
				maskFile, outPutfile, differenceFile));
	}

	/**
	 * Width and height of the pictures are switched
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceBroadFormat() throws IOException {
		Assert.assertFalse(imgCompare.isEqual(reference, switched, maskFile,
				outPutfile, differenceFile));
	}

	@AfterClass
	public static void deleteFiles() {
		outPutfile.delete();
		maskFile.delete();
		differenceFile.delete();
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