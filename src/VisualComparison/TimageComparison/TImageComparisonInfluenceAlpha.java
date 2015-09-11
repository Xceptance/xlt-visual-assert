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
 * Tests if a difference between fully transparent and fully opaque images would
 * be detected. They should only be detected with exactly equal.
 * 
 * Since transparency can only come from manual intervention, this test isn't
 * very useful.
 * 
 * @author damian
 * 
 */
public class TImageComparisonInfluenceAlpha {

	// Tests if the methods detect it if alpha is zero. In exactlyEqual they do,
	// in fuzzy they don't.
	private static BufferedImage notTransparentImg;
	private static BufferedImage transparentImg;

	private final static int rgbBlack = new Color(0, false).getRGB();
	private final static int rgbTransparentBlack = new Color(0, true).getRGB();

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");

	@BeforeClass
	public static void initializeImages() {

		// Initializes the images; the notTransparentImg Image is black, the
		// transparentImg image transparent black
		notTransparentImg = new BufferedImage(300, 300,
				BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < notTransparentImg.getWidth(); w++) {
			for (int h = 0; h < notTransparentImg.getHeight(); h++) {
				notTransparentImg.setRGB(w, h, rgbBlack);
			}
		}

		transparentImg = new BufferedImage(300, 300,
				BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < transparentImg.getWidth(); w++) {
			for (int h = 0; h < transparentImg.getHeight(); h++) {
				transparentImg.setRGB(w, h, rgbTransparentBlack);
			}
		}
	}

	/**
	 * Tests with colTolerance = 0.01. Tests once for a transparent reference image
	 * and once for a transparent comparison image, both times against an opaque
	 * image
	 * 
	 * @throws IOException
	 */
	@Test
	public void influenceAlphaPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.01, false,
				false, "PIXELFUZZY");
		boolean result = imagecomparison.isEqual(notTransparentImg,
				transparentImg, fileMask, fileOut);
		Assert.assertTrue(
				"A transparent screenshot was unexpectedly detected",
				result);

		result = imagecomparison.isEqual(transparentImg, notTransparentImg,
				fileMask, fileOut);
		Assert.assertTrue(
				"A transparent reference Image was detected unexpectedly",
				result);
	}

	/**
	 * Tests with parameters of pixelPerBlockXY = 10 and colTolerance = 0.00. Tests
	 * once for a transparent reference image and once for a transparent
	 * comparison image, both times against an opaque image
	 * 
	 * @throws IOException
	 */
	@Test
	public void influenceAlphaExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.0, false,
				false, "EXACTLY");
		boolean result = imagecomparison.isEqual(notTransparentImg,
				transparentImg, fileMask, fileOut);
		Assert.assertFalse(
				"A transparent screenshot went undetected",
				result);
		result = imagecomparison.isEqual(transparentImg, notTransparentImg,
				fileMask, fileOut);
		Assert.assertFalse(
				"A transparent reference Image went undetected",
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
