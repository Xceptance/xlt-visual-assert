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
 * if it marked the formerly nonexistent part and did not mark the formerly existent parts. 
 * 
 * Includes separate tests for the following parameters:
 * PixelPerBlockX = 10, PixelPerBlockX = 10, Threshold = 0.1
 * PixelPerBlockX = 1, PixelPerBlockX = 1, Threshold = 0.01
 * PixelPerBlockX = 1, PixelPerBlockX = 1, Threshold = 0.0
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
	private final static int rgbMarked = -65536;

	
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
	 * Tests the resulting marked images size 
	 * ImageComparison Parameters: 10, 10, 0.1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctSize() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 0.1, false, "FUZZYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(reference.getWidth(), img.getWidth());
		Assert.assertEquals(reference.getHeight(), img.getHeight());
	}
	
	/**
	 * Tests if the screenshot image was correctly copied and not marked
	 * And if the formerly nonexistent areas were marked
	 * ImageComparison Parameters: 10, 10, 0.1, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctBreakPoint() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 0.1, false, "FUZZYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
	
		Assert.assertEquals(rgbWhite, img.getRGB(49, 49));
		Assert.assertEquals(rgbMarked, img.getRGB(50, 50));
	}
	
	/**
	 * Tests the resulting marked images size 
	 * ImageComparison Parameters: 1, 1, 0.01, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctSizePixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.01, false, "PIXELFUZZYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(reference.getWidth(), img.getWidth());
		Assert.assertEquals(reference.getHeight(), img.getHeight());
	}
	
	/**
	 * Tests if the screenshot image was correctly copied and not marked
	 * And if the formerly nonexistent areas were marked
	 * ImageComparison Parameters: 1, 1, 0.01, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctBreakPointPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.01, false, "PIXELFUZZYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(rgbWhite, img.getRGB(49, 49));
		Assert.assertEquals(rgbMarked, img.getRGB(50, 50));
	}
	
	/**
	 * Tests the resulting marked images size 
	 * ImageComparison Parameters: 1, 1, 0.00, false
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctSizeExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.00, false, "EXACTLYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(reference.getWidth(), img.getWidth());
		Assert.assertEquals(reference.getHeight(), img.getHeight());
	}
	
	/**
	 * Tests if the screenshot image was correctly copied and not marked
	 * And if the formerly nonexistent areas were marked
	 * ImageComparison Parameters: 1, 1, 0.00, false
	 * 
	 * @throws IOException
	 */		
	@Test
	public void correctBreakPointExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 0.00, false, "EXACTLYYEQUAL");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(rgbWhite, img.getRGB(49, 49));
		Assert.assertEquals(rgbMarked, img.getRGB(50, 50));
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
