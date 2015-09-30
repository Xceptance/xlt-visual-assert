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
 * Tests if the mask is working as expected. Specifically, it tests if a
 * difference in the images is marked and detected if these areas in the mask
 * image are black and if a difference in the images is detected if these areas
 * in the mask image are white. <br>
 * 
 * @author damian
 * 
 */
public class TMask {

	// The reference image is fully black while the screenshot image is black up
	// to row 200, then white between
	// rows 200 and 300. The maskimage is black from row 250 to row 300.

	// The tests test if the resulting markedImage is marked between rows 250
	// and 300 (it shoudn't be)
	// and if the resulting markedImage is marked between rows 200 and 250 (it
	// should be)
	
	// Tests the same thing again, but with markingX and markingY = 1
	

	private static BufferedImage reference;
	private static BufferedImage screenshot;

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");

	private final static int rgbBlack = Color.BLACK.getRGB();
	private final static int rgbWhite = Color.WHITE.getRGB();
	private final static int rgbTransparentWhite = new Color(255, 255, 255, 0)
			.getRGB();
	private final int rgbMarked = Color.RED.getRGB();

	@BeforeClass
	public static void initializeImages() throws IOException {
		// Initializes the reference, screenshot and the maskImage;
		reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}

		screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < screenshot.getWidth(); w++) {
			for (int h = 0; h < screenshot.getHeight(); h++) {
				if (h >= 200) {
					screenshot.setRGB(w, h, rgbWhite);
				} else {
					screenshot.setRGB(w, h, rgbBlack);
				}
			}
		}

		BufferedImage maskImage = new BufferedImage(300, 300,
				BufferedImage.TYPE_INT_ARGB);
		for (int w = 0; w < screenshot.getWidth(); w++) {
			for (int h = 0; h < screenshot.getHeight(); h++) {
				if (h >= 250) {
					maskImage.setRGB(w, h, rgbBlack);
				} else {
					maskImage.setRGB(w, h, rgbTransparentWhite);
				}
			}
		}
		ImageIO.write(maskImage, "PNG", fileMask);
	}

	/**
	 * Tests if the parts where the mask is transparent and there were
	 * differences was marked
	 * 
	 * @throws IOException
	 */
	@Test
	public void changesCorrectlyMarked() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 10, 10,
				0.1, 0.01, false, false, 3, 3, false, "PIXELFUZZY");

		boolean result = imagecomparison.isEqual(reference, screenshot,
				fileMask, fileOut, differenceFile);
		Assert.assertFalse("A difference that wasn't masked went undetected",
				result);

		BufferedImage output = ImageIO.read(fileOut);

		// Checks if the unmarked parts have both white and red in them
		boolean hasWhite = false;
		boolean isMarked = false;

		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 200; h < 250; h++) {
				if (output.getRGB(w, h) == rgbWhite) {
					hasWhite = true;
				}
				if (output.getRGB(w, h) == rgbMarked) {
					isMarked = true;
				}
			}
		}
		Assert.assertTrue(hasWhite);
		Assert.assertTrue(isMarked);
	}

	/**
	 * Tests if the parts where the mask is black and there were differences was
	 * marked
	 * 
	 * @throws IOException
	 */
	@Test
	public void changesCorrectlyHidden() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 10, 10,
				0.1, 0.01, false, false, 3, 3, false, "FUZZY");
		imagecomparison.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		BufferedImage output = ImageIO.read(fileOut);

		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 250; h < output.getHeight(); h++) {
				Assert.assertEquals(Color.BLACK.getRGB(), output.getRGB(w, h));
			}
		}
	}

	/**
	 * Deletes the temporary files which were created for this test
	 */
	// Deletes the created files after the test.
	@AfterClass
	public static void deleteFile() {
		fileMask.delete();
		fileOut.delete();
		differenceFile.delete();
	}
}
