package test.com.xceptance.xlt.visualassertion;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.visualassertion.ImageComparison;

/**
 * Tests if differences are marked correctly. Tests if differences in a white
 * image were marked red and if differences in a red image were marked green.
 * Also tests the shape of the marking. Also tests if the markingX and the markingY 
 * parameters are working as intended.
 * 
 * Since the marking is done outside of the comparison algorithm, it only tests
 * pixelFuzzyEqual.
 * 
 * @author damian
 * 
 */
public class TMarks {

	private static BufferedImage reference;
	private static BufferedImage screenshot;

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");

	private static int blackRgb = Color.BLACK.getRGB();
	private static int whiteRgb = Color.WHITE.getRGB();
	private static int redRgb = Color.RED.getRGB();
	private static int greenRgb = Color.GREEN.getRGB();

	@BeforeClass
	public static void initializeImages() throws IOException {

		// Two images are initialized, one black reference image and one black
		// screenshot.
		// The black screenshot has one red and one white rectangle inside it.
		// The red rectangle should be marked green, the white rectangle should
		// be marked red
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		final int[] referenceArray = ((DataBufferInt) reference.getRaster()
				.getDataBuffer()).getData();
		Arrays.fill(referenceArray, blackRgb);

		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		final int[] screenshotArray = ((DataBufferInt) screenshot.getRaster()
				.getDataBuffer()).getData();
		Arrays.fill(screenshotArray, blackRgb);

		for (int w = 100; w <= 200; w++) {
			for (int h = 100; h <= 150; h++) {
				if (h <= 125) {
					screenshot.setRGB(w, h, whiteRgb);
				} else {
					screenshot.setRGB(w, h, redRgb);
				}
			}
		}
	}

	/**
	 * Tests if a difference, a white rectangle where there was none in the
	 * reference image was marked red. Only tests if they were marked red, not
	 * how they were marked, ie if it was a rectangle of the correct size.
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedRed() throws IOException {
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1,
				0.1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		final BufferedImage output = ImageIO.read(fileOut);
		boolean hasRed = false;
		final int redRgb = Color.RED.getRGB();
		for (int w = 100; w <= 200; w++) {
			for (int h = 100; h <= 125; h++) {
				if (output.getRGB(w, h) == redRgb) {
					hasRed = true;
				}
			}
		}
		Assert.assertTrue(
				"The difference wasn't marked red - correctlyMarkedRed", hasRed);
	}

	/**
	 * Tests if a difference, a red rectangle where there was none in the
	 * reference image was marked green. Only tests if they were marked green,
	 * not how they were marked, ie if it was a rectangle of the correct size.
	 * 
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedGreen() throws IOException {
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1,
				0.1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		final BufferedImage output = ImageIO.read(fileOut);
		boolean hasGreen = false;
		for (int w = 100; w <= 200; w++) {
			for (int h = 125; h <= 149; h++) {
				if (output.getRGB(w, h) == greenRgb) {
					hasGreen = true;
				}
			}
		}
		Assert.assertTrue("The difference wasn't marked green - "
				+ "correctlyMarkedGreen", hasGreen);
	}

	/**
	 * Tests the markingX and the markingY parameters.
	 * Tests if parameters of 10, 10 result in a 10 * 10 square.
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctShapeTenTen() throws IOException {
		final int markingX = 10;
		final int markingY = 10;
		final ImageComparison imagecomparison = new ImageComparison(markingX, markingY, 1,
				0.1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		final BufferedImage output = ImageIO.read(fileOut);

		// Create temporary BufferedImage, where the markings that were found
		// are saved
		final BufferedImage marking = new BufferedImage(output.getWidth(),
				output.getHeight(), BufferedImage.TYPE_INT_ARGB);

		// go through every pixel
		for (int w = 0; w < output.getWidth(); w++) {
			for (int h = 0; h < output.getHeight(); h++) {

				// Check if the pixel is marked
				if (output.getRGB(w, h) != blackRgb) {

					// Check if the corresponding pixel on the marking image is
					// marked
					if (marking.getRGB(w, h) != Color.RED.getRGB()) {

						// Paint the corresponding pixel red.
						// Also paint the corresponding rectangle red,
						// Without going over the border.
						marking.setRGB(w, h, redRgb);

						if (w < 300) {
							for (int x = 1; x < markingY; x++) {
								marking.setRGB(w + x, h, redRgb);
								marking.setRGB(w + x, h + 9, redRgb);
							}
						}
						if (h < 300) {
							for (int y = 1; y < markingX; y++) {
								marking.setRGB(w, h + y, redRgb);
								marking.setRGB(w + 9, h + y, redRgb);
							}
						}
					}
				}
			}
		}

		//Check that everywhere where the marking image was not marked is black
		for (int w = 0; w < output.getWidth(); w++) {
			for (int h = 0; h < output.getHeight(); h++) {
				if (marking.getRGB(w, h) != redRgb) {
					Assert.assertEquals(blackRgb, output.getRGB(w, h));
				}
			}
		}

		//Check that everywhere where it's not black, the marking image marked
		for (int w = 0; w < output.getWidth(); w++) {
			for (int h = 0; h < output.getHeight(); h++) {
				if (output.getRGB(w, h) != blackRgb) {
					Assert.assertEquals(redRgb, marking.getRGB(w, h));
				}
			}
		}
	}

	/**
	 * Tests the markingX and the markingY parameter.
	 * Tests if parameters of 5, 10 result in a rectangle with
	 * a height of five pixels and a width of ten.
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctShapeFiveTen() throws IOException {
		final int markingX = 5;
		final int markingY = 10;

		final ImageComparison imagecomparison = new ImageComparison(markingX, markingY, 1,
				0.1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		final BufferedImage output = ImageIO.read(fileOut);

		// Create temporary BufferedImage, where the markings that were found
		// are saved
		final BufferedImage marking = new BufferedImage(output.getWidth(),
				output.getHeight(), BufferedImage.TYPE_INT_ARGB);

		// go through every pixel
		for (int w = 0; w < output.getWidth(); w++) {
			for (int h = 0; h < output.getHeight(); h++) {

				// Check if the pixel is marked
				if (output.getRGB(w, h) != blackRgb) {

					// Check if the corresponding pixel on the marking image is
					// marked
					if (marking.getRGB(w, h) != Color.RED.getRGB()) {

						// Paint the corresponding pixel red.
						// Also paint the corresponding rectangle red,
						// Without going over the border.
						marking.setRGB(w, h, redRgb);

						if (w < 300) {
							for (int x = 1; x < markingY; x++) {
								marking.setRGB(w + x, h, redRgb);
								marking.setRGB(w + x, h + 9, redRgb);
							}
						}
						if (h < 300) {
							for (int y = 1; y < markingX; y++) {
								marking.setRGB(w, h + y, redRgb);
								marking.setRGB(w + 9, h + y, redRgb);
							}
						}
					}
				}
			}
		}

		//Check that everywhere where the marking image was not marked is black
		for (int w = 0; w < output.getWidth(); w++) {
			for (int h = 0; h < output.getHeight(); h++) {
				if (marking.getRGB(w, h) != redRgb) {
					Assert.assertEquals(blackRgb, output.getRGB(w, h));
				}
			}
		}

		//Check that everywhere where it's not black, the marking image marked
		for (int w = 0; w < output.getWidth(); w++) {
			for (int h = 0; h < output.getHeight(); h++) {
				if (output.getRGB(w, h) != blackRgb) {
					Assert.assertEquals(redRgb, marking.getRGB(w, h));
				}
			}
		}
	}

	/**
	 * Tests the markingX and the markingY parameter.
	 * Checks if everything gets marked with parameters 
	 * of 1, 1, ie rectangles with a width and height of 1.
	 * 
	 * Also tests if exactly the same happens if only one of them is 1, 
	 * because it would make no sense to draw a rectangle with a width or height
	 * of one.
	 * 
	 * This DOES NOT WORK at the moment.
	 * Areas that should be marked red are marked green, 
	 * areas that should be marked green are marked red.
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctShapeOneOne() throws IOException {
		int markingX = 1;
		int markingY = 1;

		ImageComparison imagecomparison = new ImageComparison(markingX, markingY, 1,
				0.1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		BufferedImage output = ImageIO.read(fileOut);

		for (int w = 100; w <= 200; w++) {
			for (int h = 100; h <= 150; h++) {
				if (h <= 125) {
					Assert.assertEquals(redRgb, output.getRGB(w, h)); 
				} else {
					Assert.assertEquals(greenRgb, output.getRGB(w, h)); 
				}
			}
		}

		//And the same except with markingX = 10
		markingX = 10;
		markingY = 1;

		imagecomparison = new ImageComparison(markingX, markingY, 1,
				0.1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		output = ImageIO.read(fileOut);

		for (int w = 100; w <= 200; w++) {
			for (int h = 100; h <= 150; h++) {
				if (h <= 125) {
					Assert.assertEquals(redRgb, output.getRGB(w, h)); 
				} else {
					Assert.assertEquals(greenRgb, output.getRGB(w, h)); 
				}
			}
		}

		//And the same except with markingY = 10
		markingX = 1;
		markingY = 10;

		imagecomparison = new ImageComparison(markingX, markingY, 1,
				0.1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		output = ImageIO.read(fileOut);

		for (int w = 100; w <= 200; w++) {
			for (int h = 100; h <= 150; h++) {
				if (h <= 125) {
					Assert.assertEquals(redRgb, output.getRGB(w, h)); 
				} else {
					Assert.assertEquals(greenRgb, output.getRGB(w, h)); 
				}
			}
		}
	}

	/**
	 * Deletes the temporary files which were created for this test
	 */
	@AfterClass
	public static void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
		differenceFile.delete();
	}

}
