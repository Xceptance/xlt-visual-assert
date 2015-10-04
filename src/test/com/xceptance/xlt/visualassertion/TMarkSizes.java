package test.com.xceptance.xlt.visualassertion;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.visualassertion.ImageComparison;

/**
 * Tests the return value of ImageComparison for differently sized images. It
 * should return false for images of different sizes.
 * <p>
 * Also tests if the correct parts are marked in the correct color. <br>
 * And if a difference in size will return false if the training mode is on. It
 * should.
 * <p>
 * Also tests if the borders were somehow marked in the difference image or the mask image.
 * 
 * @author Lucas
 * 
 */
public class TMarkSizes {
	static BufferedImage reference, slim, wide, low, high, slimAndLow,
	wideAndHigh, switched;
	ImageComparison imgCompare = new ImageComparison(10, 10, 2, 0.00, 0.01,
			false, false, 3, 3, true, "FUZZY");
	ImageComparison imgCompareTrain = new ImageComparison(10, 10, 2, 0.00,
			0.01, true, false, 3, 3, true, "FUZZY");

	static File directory = org.apache.commons.lang3.SystemUtils
			.getJavaIoTmpDir();
	static File outPutfile = new File(directory + "/test.png");
	static File maskFile = new File(directory + "/mask.png");
	private static File differenceFile = new File(directory + "/difference.png");

	private static Color transparentWhite = new Color(255, 255, 255, 0);
	private static Color transparentBlack = new Color(0, 0, 0, 0);

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
		Assert.assertFalse(imgCompare.isEqual(reference, slimAndLow, maskFile,
				outPutfile, differenceFile));
	}

	/**
	 * New screenshot is wider and higher
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceBothLess() throws IOException {
		Assert.assertFalse(imgCompare.isEqual(reference, wideAndHigh, maskFile,
				outPutfile, differenceFile));
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

	/**
	 * Width and height of the pictures are switched
	 * 
	 * @throws IOException
	 */
	@Test
	public void referenceBroadFormatWhileTraining() throws IOException {
		Assert.assertTrue(imgCompareTrain.isEqual(reference, switched,
				maskFile, outPutfile, differenceFile));
	}

	/**
	 * Width and height of the pictures are switched. Check if the previously
	 * not existent areas are marked. Checks again after it was done the other
	 * way round.
	 * 
	 * @throws IOException
	 */
	@Test
	public void checkMarkedAreaBroadFormat() throws IOException {

		final int minWidth = Math.min(reference.getHeight(), switched.getHeight());
		final int minHeight = Math.min(reference.getHeight(), switched.getHeight());
		final int maxWidth = Math.max(reference.getHeight(), switched.getHeight());
		final int maxHeight = Math.max(reference.getHeight(), switched.getHeight());

		Assert.assertFalse(imgCompare.isEqual(reference, switched, maskFile,
				outPutfile, differenceFile));
		BufferedImage marked = ImageIO.read(outPutfile);

		for (int w = 0; w < maxWidth; w++) {
			for (int h = 0; h < maxHeight; h++) {

				if (w < minWidth && h < minHeight) {
					Assert.assertEquals(Color.BLACK.getRGB(),
							marked.getRGB(w, h));
				} else {
					Assert.assertEquals(transparentBlack.getRGB(),
							marked.getRGB(w, h));
				}
			}
		}

		// And test again with the images switched around
		Assert.assertFalse(imgCompare.isEqual(reference, switched, maskFile,
				outPutfile, differenceFile));

		marked = ImageIO.read(outPutfile);
		for (int w = 0; w < maxWidth; w++) {
			for (int h = 0; h < maxHeight; h++) {

				if (w < minWidth && h < minHeight) {
					Assert.assertEquals(Color.BLACK.getRGB(),
							marked.getRGB(w, h));
				} else {
					Assert.assertEquals(transparentBlack.getRGB(),
							marked.getRGB(w, h));
				}
			}
		}
	}

	/**
	 * Width and height of the pictures are switched. Check if the previously
	 * not existent areas are marked. Checks again after it was done the other
	 * way round.
	 * 
	 * @throws IOException
	 */
	@Test
	public void checkMaskAreaBroadFormat() throws IOException {

		final int minWidth = Math.min(reference.getHeight(), switched.getHeight());
		final int minHeight = Math.min(reference.getHeight(), switched.getHeight());
		final int maxWidth = Math.max(reference.getHeight(), switched.getHeight());
		final int maxHeight = Math.max(reference.getHeight(), switched.getHeight());

		BufferedImage mask = new BufferedImage(10, 10,
				BufferedImage.TYPE_INT_ARGB);
		final int[] maskArray = ((DataBufferInt) mask.getRaster().getDataBuffer())
				.getData();
		Arrays.fill(maskArray, transparentBlack.getRGB());

		paintWhite(switched);
		imgCompareTrain.isEqual(reference, switched, maskFile, outPutfile,
				differenceFile);
		mask = ImageIO.read(maskFile);

		// The borders should be set to transparent black, since they were
		for (int w = minWidth; w < maxWidth; w++) {
			for (int h = minHeight; h < maxHeight; h++) {
				Assert.assertEquals(Color.BLACK.getRGB(), mask.getRGB(w, h));
			}
		}

		// And test again with the images switched around
		Assert.assertFalse(imgCompare.isEqual(reference, switched, maskFile,
				outPutfile, differenceFile));
		mask = ImageIO.read(outPutfile);

		// The borders should be set to transparent black, since they were
		for (int w = minWidth; w < maxWidth; w++) {
			for (int h = minHeight; h < maxHeight; h++) {
				Assert.assertEquals(transparentBlack.getRGB(),
						mask.getRGB(w, h));
			}
		}

		// Reset the maskFile so the integrity of the other tests
		// isn't compromised.
		maskFile = new File(directory + "/mask.png");
		// Also reset the switched image
		paintBlack(switched);
	}

	/**
	 * Width and height of the images are equal. Checks that it returns true and
	 * more importantly, that nothing was, masked or changed in the difference
	 * image.
	 * 
	 * @throws IOException
	 */
	@Test
	public void checkMarkedAreaSameSize() throws IOException {

		differenceFile.delete();
		final BufferedImage imgSameSize = new BufferedImage(reference.getWidth(),
				reference.getHeight(), BufferedImage.TYPE_INT_ARGB);
		paintBlack(imgSameSize);

		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				Assert.assertEquals(Color.BLACK.getRGB(),
						reference.getRGB(w, h));
			}
		}
		for (int w = 0; w < imgSameSize.getWidth(); w++) {
			for (int h = 0; h < imgSameSize.getHeight(); h++) {
				Assert.assertEquals(Color.BLACK.getRGB(),
						imgSameSize.getRGB(w, h));
			}
		}

		Assert.assertTrue(imgCompare.isEqual(reference, imgSameSize, maskFile,
				outPutfile, differenceFile));

		// Check if a difference file was made. It shoudn't have been
		Assert.assertFalse(differenceFile.exists());

		imgCompareTrain.isEqual(reference, imgSameSize, maskFile, outPutfile,
				differenceFile);

		final BufferedImage mask = ImageIO.read(maskFile);
		for (int w = 0; w < mask.getWidth(); w++) {
			for (int h = 0; h < mask.getHeight(); h++) {
				Assert.assertEquals(transparentWhite.getRGB(),
						mask.getRGB(w, h));
			}
		}
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
	public static void paintBlack(final BufferedImage img) {
		final int rgb = Color.BLACK.getRGB();
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
	public static void paintWhite(final BufferedImage img) {
		final int rgb = Color.WHITE.getRGB();
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				img.setRGB(x, y, rgb);
			}
		}
	}
}