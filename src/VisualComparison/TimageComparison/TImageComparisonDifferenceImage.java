package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;

/**
 * Tests if the difference image is created as it should be. Specifically, it
 * tests if the areas where one image was black and the other white are white,
 * the areas where they were equal are black and the area were one was full blue
 * with rgb = {0, 0, 255} and the other blue with {0, 0, 100} are {78, 78, 78}. <br>
 * Tests if a difference image is created if the differenceImage parameter is false.
 * <p>
 * Includes separate tests for exactlyEqual, pixelFuzzyEqual and fuzzyEqual,
 * sind it is implemented separately.
 * <p>
 * TEST
 * <p>
 * @author damian
 */
public class TImageComparisonDifferenceImage {

	private static BufferedImage reference;
	private static BufferedImage screenshot;
	private static BufferedImage differenceImage;

	private final static int rgbBlack = Color.BLACK.getRGB();
	private final static int rgbWhite = Color.WHITE.getRGB();
	private final static int rgbBlue1 = new Color(0, 0, 255).getRGB();
	private final static int rgbBlue2 = new Color(0, 0, 100).getRGB();
	private final static int diffBlue = new Color(78, 78, 78).getRGB();

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");

	@BeforeClass
	public static void initializeImages() {
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < 200; h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}

		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 200; h < 300; h++) {
				reference.setRGB(w, h, rgbBlue1);
			}
		}

		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		for (int w = 0; w < screenshot.getWidth(); w++) {
			for (int h = 0; h < 100; h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}

		for (int w = 0; w < screenshot.getWidth(); w++) {
			for (int h = 100; h < 200; h++) {
				screenshot.setRGB(w, h, rgbWhite);
			}
		}

		for (int w = 0; w < screenshot.getWidth(); w++) {
			for (int h = 200; h < 300; h++) {
				screenshot.setRGB(w, h, rgbBlue2);
			}
		}
	}
	
	/**
	 * Tests the exactlyEqual method.
	 * 
	 * @throws IOException
	 */
	@Test
	public void tExactly() throws IOException {
		ImageComparison imagecomparison2 = new ImageComparison(10, 0.1, 0.01,
				false, false, 3, 3, true, "EXACTLY");
		imagecomparison2.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		differenceImage = ImageIO.read(differenceFile);
		int rgb;
		
		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 0; h < 100; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(rgbBlack, rgb);
			}
		}

		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 100; h < 200; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(rgbWhite, rgb);
			}
		}

		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 200; h < 300; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(diffBlue, rgb);
			}
		}
	}
	
	/**
	 * Tests the pixelFuzzyEqual method.
	 * 
	 * @throws IOException
	 */
	@Test
	public void tPixelFuzzy() throws IOException {
		ImageComparison imagecomparison2 = new ImageComparison(10, 0.1, 0.01,
				false, false, 3, 3, true, "PIXELFUZZY");
		imagecomparison2.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		differenceImage = ImageIO.read(differenceFile);
		int rgb;
		
		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 0; h < 100; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(rgbBlack, rgb);
			}
		}

		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 100; h < 200; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(rgbWhite, rgb);
			}
		}

		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 200; h < 300; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(diffBlue, rgb);
			}
		}
	}
	
	/**
	 * Tests the fuzzyEqual method.
	 * 
	 * @throws IOException
	 */
	@Test
	public void tFuzzy() throws IOException {
		ImageComparison imagecomparison2 = new ImageComparison(10, 0.1, 0.01,
				false, false, 3, 3, true, "FUZZY");
		imagecomparison2.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		differenceImage = ImageIO.read(differenceFile);
		int rgb;
		
		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 0; h < 100; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(rgbBlack, rgb);
			}
		}

		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 100; h < 200; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(rgbWhite, rgb);
			}
		}

		for (int w = 0; w < differenceImage.getWidth(); w++) {
			for (int h = 200; h < 300; h++) {
				rgb = differenceImage.getRGB(w, h);
				Assert.assertEquals(diffBlue, rgb);
			}
		}
	}
	
	/**
	 * Tests if the difference image is created if the differenceImage parameter is false.
	 * Tests exactlyEqual.
	 * @throws IOException 
	 */
	@Test(expected=IOException.class)
	public void testFalseExactly() throws IOException {
		differenceFile.delete();
		ImageComparison imagecomparison2 = new ImageComparison(10, 0.1, 0.01,
				false, false, 3, 3, false, "EXACTLY");
		imagecomparison2.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		differenceImage = ImageIO.read(differenceFile);

	}
	
	/**
	 * Tests if the difference image is created if the differenceImage parameter is false.
	 * Tests pixelFuzzyEqual.
	 * @throws IOException 
	 */
	@Test(expected=IOException.class)
	public void testFalsePixelFuzzy() throws IOException {
		differenceFile.delete();
		ImageComparison imagecomparison2 = new ImageComparison(10, 0.1, 0.01,
				false, false, 3, 3, false, "PIXELFUZZY");
		imagecomparison2.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		differenceImage = ImageIO.read(differenceFile);
	}
	
	/**
	 * Tests if the difference image is created if the differenceImage parameter is false.
	 * Tests fuzzyEqual.
	 * @throws IOException 
	 */
	@Test(expected=IOException.class)
	public void testFalseFuzzy() throws IOException {
		differenceFile.delete();
		ImageComparison imagecomparison2 = new ImageComparison(10, 0.1, 0.01,
				false, false, 3, 3, false, "FUZZY");
		imagecomparison2.isEqual(reference, screenshot, fileMask, fileOut,
				differenceFile);
		differenceImage = ImageIO.read(differenceFile);
	}
	
	/**
	 * Deletes the temporary files which were created for this test
	 */
	@AfterClass
	public static void deleteFile() {
		fileMask.delete();
		fileOut.delete();
		differenceFile.delete();
	}
}
