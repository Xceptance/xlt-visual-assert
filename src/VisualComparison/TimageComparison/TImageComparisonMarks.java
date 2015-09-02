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

public class TImageComparisonMarks {

//	This class tests if differences are marked correctly. Two images are initialized, one black reference 
//	image and one black screenshot, but the black screenshot has one red rectangle and one white 
//	rectangle inside it.
//	The red rectangle should be marked green, the white rectangle should be marked red
	
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
	
	@Test
	public void correctlyMarkedRed() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(5, 5, 0.1, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
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
	
	@Test
	public void correctlyMarkedGreen() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(5, 5, 0.1, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
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
	
	@Test
	public void correctlyMarkedRedExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
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
	
	@Test
	public void correctlyMarkedGreenExactlyEqual() throws IOException {
//		Doesn't mark the borders to the right and left
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
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
	
	@Test
	public void correctlyMarkedRedPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.02, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
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
	
	@Test
	public void correctlyMarkedGreenPixelFuzzyEqual() throws IOException {
//		Doesn't mark the borders to the right or to the left
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut);
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
	
	@AfterClass
	public static void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
	}
	
}
