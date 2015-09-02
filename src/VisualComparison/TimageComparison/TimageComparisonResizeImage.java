package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;

public class TimageComparisonResizeImage {

//	This class tests whether the ResizeImage method is working as expected. Two Images are created, 
//	both are white, but with different sizes
	
//	The tests test whether the resulting image has the correct size and whether or not the break off between 
//	marked black from resizing and unmarked white is at the correct place
	
//	Includes separate methods for exactlyEqual and pixelFuzzyEqual
	
	private BufferedImage reference;
	private BufferedImage screenshot;
	
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	
	private final static int rgbWhite = Color.WHITE.getRGB();
	private final static int rgbMarked = -65536;

	
//	Initializes reference and screenshot images. 
	@Before
	public void initializeImages() {
		BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		int[] referenceArray = ((DataBufferInt) reference.getRaster().getDataBuffer()).getData();
		Arrays.fill(referenceArray, rgbWhite);
		this.reference = reference;
		
		BufferedImage screenshot = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
		int[] screenshotArray = ((DataBufferInt) screenshot.getRaster().getDataBuffer()).getData();
		Arrays.fill(screenshotArray, rgbWhite);
		this.screenshot = screenshot;
	}
	
//	Tests if the resulting marked image has the correct size (the same size as the big reference image)
//	Tests the normal fuzzyEqual method
	@Test
	public void correctSize() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(2, 1, 0.01, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(reference.getWidth(), img.getWidth());
		Assert.assertEquals(reference.getHeight(), img.getHeight());
	}
	
//	Tests if the smaller image correctly preserved during the resizing and if the resized parts are marked
//	Tests the normal fuzzyEqual method
	@Test
	public void correctBreakPoint() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(2, 2, 0.01, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
	
		Assert.assertEquals(rgbWhite, img.getRGB(49, 49));
		Assert.assertEquals(rgbMarked, img.getRGB(50, 50));
	}
	
//	Tests if the resulting marked image has the correct size (the same size as the big reference image)
//	Tests the normal exactlyEqual method	
	@Test
	public void correctSizeExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(reference.getWidth(), img.getWidth());
		Assert.assertEquals(reference.getHeight(), img.getHeight());
	}
	
//	Tests if the smaller image correctly preserved during the resizing and if the resized parts are marked
//	Tests the normal exactlyEqual method		
	@Test
	public void correctBreakPointExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(rgbWhite, img.getRGB(49, 49));
		Assert.assertEquals(rgbMarked, img.getRGB(50, 50));
	}
	
//	Tests if the resulting marked image has the correct size (the same size as the big reference image)
//	Tests the normal pixelFuzzyEqual method	
	@Test
	public void correctSizePixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.01, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(reference.getWidth(), img.getWidth());
		Assert.assertEquals(reference.getHeight(), img.getHeight());
	}
	
//	Tests if the smaller image correctly preserved during the resizing and if the resized parts are marked
//	Tests the normal pixelFuzzyEqual method	
	@Test
	public void correctBreakPointPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.01, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		Assert.assertEquals(rgbWhite, img.getRGB(49, 49));
		Assert.assertEquals(rgbMarked, img.getRGB(50, 50));
	}
	
	@After
	public void deleteFile() {
		fileMask.delete();
		fileOut.delete();
	}
}
