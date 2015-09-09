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
 * Tests if differences are marked correctly. 
 * Tests if differences in a white image were marked red and if differences in a red image were marked green.
 * Includes separate tests for the following parameters: 
 * PixelPerBlockX = 5, PixelPerBlockX = 5, Threshold = 0.1
 * PixelPerBlockX = 1, PixelPerBlockX = 1, Threshold = 0.01
 * PixelPerBlockX = 1, PixelPerBlockX = 1, Threshold = 0.0
 *  
 * @author damian
 *
 */
public class TImageComparisonMarks {

	private static BufferedImage reference;
	private static BufferedImage screenshot;
		
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	
	private static int blackRgb = Color.BLACK.getRGB();
	private static int whiteRgb = Color.WHITE.getRGB();
	private static int redRgb = Color.RED.getRGB();
	
	@BeforeClass
	public static void initializeImages() throws IOException {
//		Two images are initialized, one black reference image and one black screenshot.
//		The black screenshot has one red and one white rectangle inside it.
//		The red rectangle should be marked green, the white rectangle should be marked red
		
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		int[] referenceArray = ((DataBufferInt) reference.getRaster().getDataBuffer()).getData();
		Arrays.fill(referenceArray, blackRgb);

		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		int[] screenshotArray = ((DataBufferInt) screenshot.getRaster().getDataBuffer()).getData();
		Arrays.fill(screenshotArray, blackRgb);

		for (int w=100; w<=200; w++) {
			for (int h=100; h<=150; h++) {
				if (h<=125) {
					screenshot.setRGB(w, h, whiteRgb);
				}
				else {
					screenshot.setRGB(w, h, redRgb);
				}
			}
		}
	}
	
	/**
	 * Tests if a difference, a white rectangle where there was none in the reference image
	 * was correctly marked red. ImageComparison parameters: 5, 5, 0.1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedRed() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(5, 0.1, false, "FUZZYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage output = ImageIO.read(fileOut);
		boolean hasRed = false;
		int redRgb = Color.RED.getRGB();
		for (int w=100; w<=200; w++) {
			for (int h=100; h<=125; h++) {
				if (output.getRGB(w, h) == redRgb) {
					hasRed = true;
				}
			}
		}
		Assert.assertTrue("The difference wasn't marked red - correctlyMarkedRed", hasRed);
	}
	
	/**
	 * Tests if a difference, a red rectangle where there was none in the reference image
	 * was correctly marked green. ImageComparison parameters: 5, 5, 0.1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedGreen() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(5, 0.1, false, "FUZZYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage output = ImageIO.read(fileOut);
		boolean hasGreen = false;
		int greenRgb = Color.GREEN.getRGB();
		for (int w=100; w<=200; w++) {
			for (int h=125; h<=149; h++) {
				if (output.getRGB(w, h) == greenRgb) {
					hasGreen = true;
				}
			}
		}
		Assert.assertTrue("The difference wasn't marked green - " +
				"correctlyMarkedGreen", hasGreen);
	}
	
	/**
	 * Tests if a difference, a white rectangle where there was none in the reference image
	 * was correctly marked red. ImageComparison parameters: 1, 1, 0.01, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedRedPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.01, false, "FUZZYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage output = ImageIO.read(fileOut);
		boolean hasRed = true;
		int redRgb = Color.RED.getRGB();
		
		for (int w=100; w<=200; w++) {
			for (int h=100; h<=125; h++) {
				if (output.getRGB(w, h) != redRgb) {
					hasRed = false;
				}
			}
		}
		Assert.assertTrue("The difference wasn't marked red - " +
				"correctlyMarkedRedPixelFuzzyEqual", hasRed);
	}
	
	/**
	 * Tests if a difference, a red rectangle where there was none in the reference image
	 * was correctly marked green. ImageComparison parameters: 1, 1, 0.01, false .
	 * The borders to the right and left of the rectangle remain unmarked 
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedGreenPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.01, false, "PIXELFUZZYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage output = ImageIO.read(fileOut);
		boolean hasGreen = true;
		int greenRgb = Color.GREEN.getRGB();
		
		for (int w=100; w<=199; w++) {
			for (int h=126; h<=150; h++) {
				if (output.getRGB(w, h) != greenRgb) {
					hasGreen = false;
				}
			}
		}
		Assert.assertTrue("The difference wasn't completely marked green - " +
				"correctlyMarkedGreenPixelFuzzyEqual", hasGreen);
	}
	
	/**
	 * Tests if a difference, a white rectangle where there was none in the reference image
	 * was correctly marked red. ImageComparison parameters: 1, 1, 0.00, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedRedExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.00, false, "EXACTLYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage output = ImageIO.read(fileOut);
		boolean hasRed = true;
		int redRgb = Color.RED.getRGB();
		for (int w=100; w<=200; w++) {
			for (int h=100; h<=125; h++) {
				if (output.getRGB(w, h) != redRgb) {
					hasRed = false;
				}
			}
		}
		Assert.assertTrue("The difference wasn't completely marked red - " +
				"correctlyMarkedRedExactlyEqual", hasRed);
	}
	
	/**
	 * Tests if a difference, a red rectangle where there was none in the reference image
	 * was correctly marked green. ImageComparison parameters: 1, 1, 0.00, false
	 * The borders to the right and left of the rectangle remain unmarked 
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedGreenExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.00, false, "EXACTLYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage output = ImageIO.read(fileOut);
		boolean hasGreen = true;
		int greenRgb = Color.GREEN.getRGB();
		for (int w=100; w<=199; w++) {
			for (int h=126; h<=150; h++) {
				if (output.getRGB(w, h) != greenRgb) {
					hasGreen = false;
				}
			}
		}
		Assert.assertTrue("The difference wasn't completely marked green - " +
				"correctlyMarkedGreenExactlyEqual", hasGreen);
	}
	
	/**
	 * Deletes the temporary files which were created for this test
	 */
	@AfterClass
	public static void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
	}
	
}
