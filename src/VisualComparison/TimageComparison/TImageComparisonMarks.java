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
 * Only tests the color, not the shape of the marking.
 *
 * Since the marking is done outside of the comparison algorithm, it only tests pixelFuzzyEqual. 
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
	private static File differenceFile = new File(directory + "/difference.png");
	
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
	 * was marked red. Only tests if they were marked red, not how they were marked,
	 * ie if it was a rectangle of the correct size.
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedRed() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.1, 0.01, false, false, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
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
	 * was marked green. Only tests if they were marked green, not how they were marked,
	 * ie if it was a rectangle of the correct size.
	 * 
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyMarkedGreen() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.1, 0.01, false, false, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
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
	 * Deletes the temporary files which were created for this test
	 */
	@AfterClass
	public static void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
		differenceFile.delete();
	}
	
}
