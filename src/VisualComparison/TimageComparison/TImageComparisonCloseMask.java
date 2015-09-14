package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;

/**
 * Tests if small non-marked gaps in the maskImage are closed. Also tests if the
 * maskImage stays the same otherwise.
 * 
 * @author daniel
 * 
 */
public class TImageComparisonCloseMask {

	private static BufferedImage reference;
	private static BufferedImage screenshot;

	private final static int rgbBlack = Color.BLACK.getRGB();
	private final static int rgbWhite = Color.WHITE.getRGB();
	private final static int rgbTransparentWhite = new Color(255, 255, 255, 0)
			.getRGB();

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");

	private final ImageComparison train = new ImageComparison(10, 0.0, 0.01,
			true, true, false, "PIXELFUZZY");
	private final ImageComparison compare = new ImageComparison(10, 0.0, 0.01,
			false, false, false, "PIXELFUZZY");

	@BeforeClass
	public static void initializeReference() {
		reference = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
	}

	@Before
	public void initializeScreenshot() throws IOException {
		screenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				screenshot.setRGB(w, h, rgbWhite);
			}
		}
	}

	@Before
	public void initializeMaskImage() throws IOException {
		BufferedImage maskImage = new BufferedImage(200, 200,
				BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				maskImage.setRGB(w, h, rgbTransparentWhite);
			}
		}
		ImageIO.write(maskImage, "PNG", fileMask);
	}

	/**
	 * Tests what happens if the unmasked gaps in the maskImage are smaller then
	 * the structuring element. (The structuring element is used to close the
	 * image. If it can fit into a gap completely, the gap won't be closed. It's
	 * default width and height are a tenth of the maskImage). <br>
	 * The maskImage is painted using the blocks from markingX/ markingY, which
	 * makes a gap that fits the structuring element exactly difficult.
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyClosedLess() throws IOException {

		// Paints a black line in the middle of the screenshot
		// It's size should exactly match the size of the structuring element
		for (int w = 90; w < 110; w++) {
			for (int h = 0; h < 200; h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}

		train.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		boolean isCorrectlyMasked = true;
		BufferedImage maskImage = ImageIO.read(fileMask);

		// Checks if the black line remained unmasked
		for (int w = 90; w < 110; w++) {
			for (int h = 0; h < 200; h++) {
				if (maskImage.getRGB(w, h) != rgbBlack) {
					isCorrectlyMasked = false;
				}
			}
		}
		Assert.assertTrue(isCorrectlyMasked);

		// Paints the line white and checks if the comparison
		// detects a difference for redundancy
		for (int w = 89; w < 121; w++) {
			for (int h = 0; h < 200; h++) {
				screenshot.setRGB(w, h, rgbWhite);
			}
		}
		Assert.assertTrue(compare.isEqual(reference, screenshot, fileMask,
				fileOut, differenceFile));
	}

	/**
	 * Tests what happens if the unmasked gaps in the maskImage are bigger then
	 * the structuring element. (The structuring element is used to close the
	 * image. If it can fit into a gap completely, the gap won't be closed. It's
	 * default width and height are a tenth of the maskImage). <br>
	 * The maskImage is painted using the blocks from markingX/ markingY, which
	 * makes a gap that fits the structuring element exactly difficult.
	 * 
	 * @throws IOException
	 */
	@Test
	public void correctlyNotClosedMore() throws IOException {

		// Paints a black line in the middle of the screenshot
		// It's size should exactly match the size of the structuring element
		for (int w = 89; w < 121; w++) {
			for (int h = 0; h < 200; h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}

		train.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		boolean isCorrectlyNotMasked = true;
		BufferedImage maskImage = ImageIO.read(fileMask);

		// Checks if black line remained unmasked
		for (int w = 89; w < 121; w++) {
			for (int h = 0; h < 200; h++) {
				if (maskImage.getRGB(w, h) == rgbBlack) {
					isCorrectlyNotMasked = false;
				}
			}
		}
		Assert.assertFalse(isCorrectlyNotMasked);

		// Paints the line white and checks if the comparison
		// detects a difference for redundancy
		for (int w = 89; w < 121; w++) {
			for (int h = 0; h < 200; h++) {
				screenshot.setRGB(w, h, rgbWhite);
			}
		}
		Assert.assertFalse(compare.isEqual(reference, screenshot, fileMask,
				fileOut, differenceFile));
	}

	/**
	 * Tests if a gap in the corner will be closed if it is smaller then the
	 * structuring element. It should be.
	 * @throws IOException 
	 */
	@Test
	public void smallerCorner() throws IOException {
		
		//Set everything except the corner black
		for (int w = 0; w < 10; w++) {
			for (int h = 0; h < 10; h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}
		
		train.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		boolean isCorrectlyMasked = true;
		BufferedImage maskImage = ImageIO.read(fileMask);

		// Checks if the corner was masked
		for (int w = 0; w < 10; w++) {
			for (int h = 0; h < 10; h++) {
				if (maskImage.getRGB(w, h) != rgbBlack) {
					isCorrectlyMasked = false;
				}
			}
		}
		Assert.assertTrue(isCorrectlyMasked);	
	}
	
	/**
	 * Tests if a gap in the corner will be closed if it is bigger then the
	 * structuring element. It shoudn't be.
	 * @throws IOException 
	 */
	@Test
	public void biggerCorner() throws IOException {
		
		//Set everything except the corner black
		for (int w = 0; w < 25; w++) {
			for (int h = 0; h < 25; h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}
		
		train.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		boolean isCorrectlyNotMasked = true;
		BufferedImage maskImage = ImageIO.read(fileMask);

		// Checks if the corner was masked
		for (int w = 0; w < 25; w++) {
			for (int h = 0; h < 25; h++) {
				if (maskImage.getRGB(w, h) == rgbBlack) {
					isCorrectlyNotMasked = false;
				}
			}
		}
		Assert.assertFalse(isCorrectlyNotMasked);	
	}

	/**
	 * Deletes the temporary files which were created for this test
	 */
	@After
	public void deleteMask() {
		fileMask.delete();
	}

	@AfterClass
	public static void deleteMarked() {
		fileOut.delete();
	}
}
