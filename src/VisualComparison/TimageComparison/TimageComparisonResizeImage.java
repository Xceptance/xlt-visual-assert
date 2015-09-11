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
 * Tests if the resizing when images of different size are compared is working as expected.
 * Specifically it tests if the resulting marked image has the correct size,
 * if the screenshot image was correctly copied to the marked image and
 * if it marked the formerly nonexistent parts and did not mark the formerly existent parts. 
 * 
 * 
 * @author damian
 *
 */
public class TimageComparisonResizeImage {
	
	private static BufferedImage reference;
	private static BufferedImage screenshot;
	
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	
	private final static int rgbWhite = Color.WHITE.getRGB();
	private final static int rgbMarked = Color.RED.getRGB();

	
	@BeforeClass
	public static void initializeImages() {
//		Initializes reference and screenshot images. 
//		Both images are white. Reference image size: 300*300. Screenshot image size: 50*50.

		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		int[] referenceArray = ((DataBufferInt) reference.getRaster().getDataBuffer()).getData();
		Arrays.fill(referenceArray, rgbWhite);
		
		screenshot = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
		int[] screenshotArray = ((DataBufferInt) screenshot.getRaster().getDataBuffer()).getData();
		Arrays.fill(screenshotArray, rgbWhite);
	}
	
	/**
	 * Tests the resulting marked images size. 
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctSizePixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.01, false, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(reference.getWidth(), img.getWidth());
		Assert.assertEquals(reference.getHeight(), img.getHeight());
	}
	
	/**
	 * Tests if the screenshot image was correctly copied and not marked.
	 * And if the formerly nonexistent areas were marked
	 * ImageComparison Parameters: 1, 1, 0.01, false, false, PIXELFUZZY
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctBreakPointPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.01, false, false, "PIXELFUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		for (int w = 0; w < img.getWidth(); w++) {
			for (int h = 0; h < img.getHeight(); h++) {
				if (w < 50 && h < 50) {
					Assert.assertEquals(rgbWhite, img.getRGB(w, h));
				}
				else {
					Assert.assertEquals(rgbMarked, img.getRGB(w, h));
				}
			}
		}
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
