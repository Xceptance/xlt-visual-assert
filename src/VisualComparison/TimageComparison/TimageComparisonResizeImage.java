package VisualComparison.TimageComparison;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;

public class TimageComparisonResizeImage {

//	This class tests whether the ResizeImage method is working as expected. Two Images are created, 
//	both are white, but with different sizes
	
//	The tests test whether the resulting image has the correct size and whether or not the break off between marked black from resizing
//	and unmarked white is at the correct place
	
//	Includes separate methods for exactlyEqual and pixelFuzzyEqual
	
	private BufferedImage reference;
	private BufferedImage screenshot;
	
	private String pathHome = System.getProperty("user.home");
	private File fileMask = new File(pathHome + "/maskImage.png");
	private File fileOut = new File(pathHome + "/output.png");
	
	@Before
	public void initializeImages() {
		BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		int[] referenceArray = ((DataBufferInt) reference.getRaster().getDataBuffer()).getData();
		Color white= Color.WHITE;
		int rgbWhite= white.getRGB();
		Arrays.fill(referenceArray, rgbWhite);
		this.reference = reference;
		
		BufferedImage screenshot = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
		int[] screenshotArray = ((DataBufferInt) screenshot.getRaster().getDataBuffer()).getData();
		Arrays.fill(screenshotArray, rgbWhite);
		this.screenshot = screenshot;
	}
	
	@Test
	public void correctSize() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(2, 1, 0.01, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		if (!(img.getWidth() == 300)) {
			Assert.assertTrue("Marked image does not have the correct width", false);
		}
		
		if (!(img.getHeight() == 300)) {
			Assert.assertTrue("Marked image does not have the correct height", false);
		}
	}
	
	@Test
	public void correctBreakPoint() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(2, 2, 0.01, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		int rgbWhite = Color.WHITE.getRGB();
		
		if (!(img.getRGB(49, 49)==rgbWhite)) {
			Assert.assertTrue("The smaller image was not correctly drawn", false);
		}
		
		int rgbMarked = -65536;
		if (!(img.getRGB(50, 50)==rgbMarked)) {
			Assert.assertTrue("The resized parts were not marked", false);
		}
	}
	
	@Test
	public void correctSizeExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		if (!(img.getWidth() == 300)) {
			System.out.println("Output image does not have the correct width");
		}
		
		if (!(img.getHeight() == 300)) {
			System.out.println("Output image does not have the correct height");
		}
	}
	
	@Test
	public void correctBreakPointExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		int rgbWhite = Color.WHITE.getRGB();
		
		if (!(img.getRGB(49, 49)==rgbWhite)) {
			Assert.assertTrue("The smaller image was not correctly drawn", false);
		}
		
		int rgbMarked = -65536;
		if (!(img.getRGB(50, 50)==rgbMarked)) {
			Assert.assertTrue("The resized parts were not marked", false);
		}
	}
	
	@Test
	public void correctSizePixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.01, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		
		if (!(img.getWidth() == 300)) {
			System.out.println("Output image does not have the correct width");
		}
		
		if (!(img.getHeight() == 300)) {
			System.out.println("Output image does not have the correct height");
		}
	}
	
	@Test
	public void correctBreakPointPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.01, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
		BufferedImage img = ImageIO.read(fileOut);
		int rgbWhite = Color.WHITE.getRGB();
		
		if (!(img.getRGB(49, 49)==rgbWhite)) {
			Assert.assertTrue("The smaller image was not correctly drawn", false);
		}
		
		int rgbMarked = -65536;
		if (!(img.getRGB(50, 50)==rgbMarked)) {
			Assert.assertTrue("The resized parts were not marked", false);
		}
	}
	
	@After
	public void deleteFile() {
		fileMask.delete();
		fileOut.delete();
	}
}
