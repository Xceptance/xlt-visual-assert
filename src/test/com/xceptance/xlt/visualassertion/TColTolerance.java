package test.com.xceptance.xlt.visualassertion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import com.xceptance.xlt.visualassertion.ImageComparison;

/**
 * Tests if the colTolerance is working as expected. 
 * Since the color difference is calculated using the weighted red, green and 
 * blue values, and the weight of red and blue depends on the red level, it is complicated to test.
 * <p>
 * The formula is as follows:
 * 
 * rDiff, gDiff and bDiff: Difference in red, green and blue values.
 * rWeight, gWeight and bWeight: The weight the color differences get.
 * r1, r2: The red values of color 1 and two.
 * <p>
 * rWeight = 2 + ((r1 + r2) / 512)
 * gWeight = 4
 * bWeigth = 2 + ((r1 + r2 - 255) / 256)
 * <p>
 * maxDiff = sqrt(rWeight * rDiff² + gWeight * gDiff² + bWeight * bDiff²)
 * <p>
 * The formula is tested against black and white, green and blue and two mixes including red.
 * Each time it is tested with colTolerance just below the actual difference, exactly the difference
 * and barely above the difference. The first should be false, the other two true.
 * <br>
 * The whole array of tests are run against the fuzzyEqual algorithm afterwards.
 * 
 * @author damian
 *
 */
public class TColTolerance {
	private static BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
	private static BufferedImage screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);


	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");

	/**
	 * Tests a black image against a white image with a colTolerance value of 0
	 * 
	 * @throws IOException
	 */
	@Test	
	public void backWhiteTZero() throws IOException {	
		paintItBlackWhite();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.0, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}

	/**
	 * Tests a black image against a white image with a colTolerance value of almost 1
	 * 
	 * @throws IOException
	 */
	@Test	
	public void backWhiteTAlmostOne() throws IOException {
		paintItBlackWhite();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.9999999999999, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}

	/**
	 * Tests a black image against a white image with a colTolerance value of 1
	 * 
	 * @throws IOException
	 */
	@Test	
	public void backWhiteTOne() throws IOException {	
		paintItBlackWhite();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests a green image against a blue image with a colTolerance value of exactly the expected difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void greenBlueTExactly() throws IOException {	
		paintItGreenBlue();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.8660254037844386, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests a green image against a blue image with a colTolerance value barely below the difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void greenBlueTBelow() throws IOException {
		paintItGreenBlue();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.8660254037844385, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}

	/**
	 * Tests a green image against a blue image with a colTolerance value barely above the difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void greenBlueTAbove() throws IOException {		
		paintItGreenBlue();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.86602540378443861, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests an image with r, b, g = 100 against an image with r, b, g = 200 
	 * with a colTolerance value of exactly the expected difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void differentTExactly() throws IOException {	
		paintItDifferent();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.39215686274509803, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests an image with r, b, g = 100 against an image with r, b, g = 200 
	 * with a colTolerance value barely below the difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void differentTBelow() throws IOException {
		paintItDifferent();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.3921568627, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}

	/**
	 * Tests an image with r, b, g = 100 against an image with r, b, g = 200 
	 * with a colTolerance value barely above the difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void differentTAbove() throws IOException {		
		paintItDifferent();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.3921568628, 0.01, false, false, 3, 3, false, "PIXELFUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests a black image against a white image with a colTolerance value of 0
	 * 
	 * @throws IOException
	 */
	@Test	
	public void backWhiteTZeroFuzzy() throws IOException {	
		paintItBlackWhite();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.0, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}

	/**
	 * Tests a black image against a white image with a colTolerance value of almost 1
	 * 
	 * @throws IOException
	 */
	@Test	
	public void backWhiteTAlmostOneFuzzy() throws IOException {
		paintItBlackWhite();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.9999999999999, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}

	/**
	 * Tests a black image against a white image with a colTolerance value of 1
	 * 
	 * @throws IOException
	 */
	@Test	
	public void backWhiteTOneFuzzy() throws IOException {	
		paintItBlackWhite();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 1, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests a green image against a blue image with a colTolerance value of exactly the expected difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void greenBlueTExactlyFuzzy() throws IOException {	
		paintItGreenBlue();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.8660254037844386, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests a green image against a blue image with a colTolerance value barely below the difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void greenBlueTBelowFuzzy() throws IOException {
		paintItGreenBlue();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.8660254037844385, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}

	/**
	 * Tests a green image against a blue image with a colTolerance value barely above the difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void greenBlueTAboveFuzzy() throws IOException {		
		paintItGreenBlue();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.86602540378443861, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests an image with r, b, g = 100 against an image with r, b, g = 200 
	 * with a colTolerance value of exactly the expected difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void differentTExactlyFuzzy() throws IOException {	
		paintItDifferent();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.39215686274509803, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
	}

	/**
	 * Tests an image with r, b, g = 100 against an image with r, b, g = 200 
	 * with a colTolerance value barely below the difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void differentTBelowFuzzy() throws IOException {
		paintItDifferent();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.3921568627, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse(result);
	}

	/**
	 * Tests an image with r, b, g = 100 against an image with r, b, g = 200 
	 * with a colTolerance value barely above the difference
	 * 
	 * @throws IOException
	 */
	@Test	
	public void differentTAboveFuzzy() throws IOException {		
		paintItDifferent();
		final ImageComparison imagecomparison = new ImageComparison(10, 10, 1, 0.3921568628, 0.01, false, false, 3, 3, false, "FUZZY");
		final boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertTrue(result);
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

	// Here come the setup methods
	private void paintItDifferent() {
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		int red = 100;
		int green = 100;
		int blue = 100;
		int rgb = (red << 16) | (green << 8) | blue;

		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgb);
			}
		}

		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		red = 200;
		green = 200;
		blue = 200;
		rgb = (red << 16) | (green << 8) | blue;

		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
				screenshot.setRGB(w, h, rgb);
			}
		}
	}

	private void paintItGreenBlue() {
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		int red = 0;
		int green = 255;
		int blue = 0;
		int rgb = (red << 16) | (green << 8) | blue;

		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgb);
			}
		}

		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		red = 0;
		green = 0;
		blue = 255;
		rgb = (red << 16) | (green << 8) | blue;

		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
				screenshot.setRGB(w, h, rgb);
			}
		}
	}

	public void paintItBlackWhite() {
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		int red = 255;
		int green = 255;
		int blue = 255;
		int rgb = (red << 16) | (green << 8) | blue;

		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgb);
			}
		}

		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		red = 0;
		green = 0;
		blue = 0;
		rgb = (red << 16) | (green << 8) | blue;

		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
				screenshot.setRGB(w, h, rgb);
			}
		}
	}
}
