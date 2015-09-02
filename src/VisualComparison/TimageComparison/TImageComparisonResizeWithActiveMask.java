package VisualComparison.TimageComparison;
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

import VisualComparison.ImageComparison;

/**
 * Tests whether an existent mask would be used if the screenshot image is smaller or bigger.
 * It should not be. A change in size means a new mask image will be created.
 * 
 * Includes separate methods for the following parameters:
 * PixelPerBlockX = 10, PixelPerBlockX = 10, Threshold = 0.1
 * PixelPerBlockX = 1, PixelPerBlockX = 1, Threshold = 0.01
 * PixelPerBlockX = 1, PixelPerBlockX = 1, Threshold = 0.00
 * 
 * @author damian
 */
public class TImageComparisonResizeWithActiveMask {
	
	private static BufferedImage smallBlackImg;
	private static BufferedImage bigWhiteImg;
	
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	
	private final static int rgbBlack = Color.BLACK.getRGB();
	private final static int rgbWhite = Color.WHITE.getRGB();
	
	@BeforeClass
	public static void initializeImagesAndMask() throws IOException {
//		Reference and bigWhiteImg and are equal except for the bottom rows 300-400, 
//		which are white in the bigWhiteImg and nonexistent in the smallBlackImg
		
		smallBlackImg = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<smallBlackImg.getWidth(); w++) { 
			for (int h=0; h<smallBlackImg.getHeight(); h++) {
				smallBlackImg.setRGB(w, h, rgbBlack);
			}
		}
	
		bigWhiteImg = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<bigWhiteImg.getWidth(); w++) { 
			for (int h=0; h<bigWhiteImg.getHeight(); h++) {
				bigWhiteImg.setRGB(w, h, rgbWhite);
			}
		}	
		
		
		BufferedImage img = initializeBlackMaskImage(smallBlackImg);
		ImageIO.write(img, "PNG", fileMask);
	}
	
	/**
	 * Tests what happens if the image to compare is bigger.
	 * ImageComparison Parameters: 10, 10, 0.1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void biggerScreenshotImage() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(10, 10, 0.1, false);
		boolean result = imagecomparison1.fuzzyEqual(smallBlackImg, bigWhiteImg, fileMask, fileOut); 
		Assert.assertFalse("Former maskImage shouldn't be used if the " +
				"screenshot has a bigger size - result: " + result, result);		
	}
	
	/**
	 * Tests what happens if the image to compare is smaller.
	 * ImageComparison Parameters: 10, 10, 0.1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void smallerScreenshotImage() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(10, 10, 0.1, false);
		boolean result = imagecomparison1.fuzzyEqual(bigWhiteImg, smallBlackImg, fileMask, fileOut);
		Assert.assertFalse("Former maskImage shouldn't be used if the " +
				"screenshot has a bigger size - result: " + result, result);
	}
	
	/**
	 * Tests what happens if the image to compare is bigger.
	 * ImageComparison Parameters: 1, 1, 0.1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void biggerScreenshotImagePixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(1, 1, 0.1, false);
		boolean result = imagecomparison1.fuzzyEqual(smallBlackImg, bigWhiteImg, fileMask, fileOut);
		Assert.assertFalse("Former maskImage shouldn't be used if the " +
				"screenshot has a bigger size - result: " + result, result);
	}
	
	/**
	 * Tests what happens if the image to compare is smaller.
	 * ImageComparison Parameters: 1, 1, 0.1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void smallerScreenshotImagePixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(1, 1, 0.1, false);
		boolean result = imagecomparison1.fuzzyEqual(bigWhiteImg, smallBlackImg, fileMask, fileOut);
		Assert.assertFalse("Former maskImage shouldn't be used if the " +
				"screenshot has a bigger size - result: " + result, result);
	}
	
	/**
	 * Tests what happens if the image to compare is bigger.
	 * ImageComparison Parameters: 1, 1, 0.00, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void biggerScreenshotImageExactlyEqual() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(1, 1, 0.00, false);
		boolean result = imagecomparison1.fuzzyEqual(smallBlackImg, bigWhiteImg, fileMask, fileOut); 
		Assert.assertFalse("Former maskImage shouldn't be used if the " +
				"screenshot has a bigger size - result: " + result, result);
	}
	
	
	/**
	 * Tests what happens if the image to compare is smaller.
	 * ImageComparison Parameters: 1, 1, 0.00, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void smallerScreenshotImageExactlyEqual() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(1, 1, 0.00, false);
		boolean result = imagecomparison1.fuzzyEqual(bigWhiteImg, smallBlackImg, fileMask, fileOut);
		Assert.assertFalse("Former maskImage shouldn't be used if the " +
				"screenshot has a bigger size - result: " + result, result);
	}	
	
	/**
	 * Deletes the temporary files which were created for this test
	 */
	@AfterClass
	public static void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
	}
	
	private static BufferedImage initializeBlackMaskImage(BufferedImage img) {
//		No relation to the code in ImageComparison
//		Creates the maskImage image, creates the array with its pixels and fills the array with zeroes
		
		BufferedImage maskImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int[] maskImgArray = ((DataBufferInt) maskImg.getRaster().getDataBuffer()).getData();
		Arrays.fill(maskImgArray, 0);
		return maskImg;
	}
}
