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
 * Tests if the smallest possible difference would be detected
 * 
 * @author damian
 * 
 */
public class TImageComparisonExactlyEqual {
	private static BufferedImage reference;
	private static BufferedImage screenshot;

	private final static int rgbBlack = Color.BLACK.getRGB();

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");

	@BeforeClass
	public static void initializeImages() {
		// Initializes the two images. The only difference between them is that
		// the rgb value of the top left pixel is smaller by one
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}

		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w = 0; w < screenshot.getWidth(); w++) {
			for (int h = 0; h < screenshot.getHeight(); h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}
		screenshot.setRGB(0, 0, rgbBlack - 1);
	}

	/**
	 * Checks whether or not the fuzzyEqual method catches the smallest
	 * difference possible as long as the parameters of ImageComparison are (1,
	 * 1, 0.0, false)
	 * 
	 * @throws IOException
	 */
	@Test
	public void almostEqualButNotQuite() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.00, false,"EXACTLYEQUAL");
		boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut);
		Assert.assertFalse(
				"The images arn't quite equal,  he apparently missed something "
						+ "- almostEqualButNotQuite", result);
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
