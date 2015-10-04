package test.com.xceptance.xlt.visualassertion;

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

import com.xceptance.xlt.visualassertion.ImageComparison;

/**
 * Tests if small non-marked gaps in the maskImage are closed. Also tests if the
 * maskImage stays the same otherwise.
 * 
 * @author daniel
 * 
 */
public class TCloseMask {

	private static BufferedImage reference;
	private static BufferedImage screenshot;

	private final static int rgbBlack = Color.BLACK.getRGB();
	private final static int rgbWhite = Color.WHITE.getRGB();
	private final static int rgbTransparentWhite = 0;

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");

	private final ImageComparison train = new ImageComparison(10, 10, 10,
			0.0, 0.01, true, true, 3, 3, false, "PIXELFUZZY");
	private final ImageComparison compare = new ImageComparison(10, 10, 10,
			0.0, 0.01, false, false, 3, 3, false, "PIXELFUZZY");

	@BeforeClass
	public static void initializeReference() {
		reference = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
	}

	@Before
	public void initializeScreenshot() throws IOException {
		screenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				screenshot.setRGB(w, h, rgbWhite);
			}
		}
	}

	@Before
	public void initializeMaskImage() throws IOException {
		final BufferedImage maskImage = new BufferedImage(200, 200,
				BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				maskImage.setRGB(w, h, rgbTransparentWhite);
			}
		}
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
		final BufferedImage maskImage = ImageIO.read(fileMask);

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
		final BufferedImage maskImage = ImageIO.read(fileMask);

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
		final BufferedImage maskImage = ImageIO.read(fileMask);

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
		final BufferedImage maskImage = ImageIO.read(fileMask);

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
	 * Tests what happens if a single pixel is different.
	 * Expected result: The marking block of that pixel is masked,
	 * nothing was closed. Works with markingX/ -Y = 10.
	 * 
	 * @throws IOException 
	 */
	@Test
	public void singlePixel() throws IOException {
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}
		screenshot.setRGB(55, 55, rgbWhite);

		train.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		final BufferedImage maskImage = ImageIO.read(fileMask);

		for (int w = 0; w < maskImage.getWidth(); w++) {
			for (int h = 0; h < maskImage.getHeight(); h++) {
				if ( 50 <= w && w < 60 && 50 <= h && h < 60) {
					Assert.assertEquals(rgbBlack, maskImage.getRGB(w, h));
				}
				else {
					Assert.assertEquals(rgbTransparentWhite, maskImage.getRGB(w, h));
				}
			}
		}
	}

	/**
	 * Tests what happens if there is a difference on every corner 
	 * of the structure element. 
	 * Calculation: MarkingX/ -Y = 10 -> Every block of ten pixels 
	 * corresponds to one pixel while closing.
	 * With an image width of 200 pixels, the image to close will have 
	 * a width of 20 pixels and the structure element a width of 3 pixels.
	 * Analogous for the height.
	 * 
	 * That means one difference in 40-50, none in 50-60, one in 60-70
	 * None in the row below that.
	 * And again the same in the row below that.
	 * @throws IOException 
	 * 
	 */
	@Test
	public void everyCorner() throws IOException {
		for (int h = 0; h < screenshot.getHeight(); h++) {
			for (int w = 0; w < screenshot.getWidth(); w++) {
				screenshot.setRGB(w, h, rgbBlack);
			}
		}
		screenshot.setRGB(44, 44, rgbWhite);
		screenshot.setRGB(44, 66, rgbWhite);
		screenshot.setRGB(66, 44, rgbWhite);
		screenshot.setRGB(66, 66, rgbWhite);

		train.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		final BufferedImage maskImage = ImageIO.read(fileMask);

		for (int w = 0; w < maskImage.getWidth(); w++) {
			for (int h = 0; h < maskImage.getHeight(); h++) {
				if ( 40 <= w && w < 70 && 40 <= h && h < 70) {
					Assert.assertEquals(rgbBlack, maskImage.getRGB(w, h));
				}
				else {
					Assert.assertEquals(rgbTransparentWhite, maskImage.getRGB(w, h));
				}
			}
		}
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
