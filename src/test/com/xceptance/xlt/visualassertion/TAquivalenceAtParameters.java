package test.com.xceptance.xlt.visualassertion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.visualassertion.ImageComparison;

/**
 * Tests if the FUZZY algorithm with both tolerance values = 0 and
 * pixelPerBlockXY = 0 comes to the same result as the EXACTLY algorithm. Does
 * the same with PIXELFUZZY
 * 
 * @author daniel
 * 
 */
public class TAquivalenceAtParameters {

	private static BufferedImage reference;
	private static BufferedImage screenshot;

	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOutExactly = new File(directory,
			"/fileOutExactly.png");
	private static File fileOutFuzzy = new File(directory,
			"/fileOutExactly.png");
	private static File differenceFileExactly = new File(directory,
			"/difference.png");
	private static File differenceFileFuzzy = new File(directory,
			"/difference.png");
	private static File thrashFile = new File(directory, "/tempFile");

	private static ImageComparison imgCompExact = new ImageComparison(10, 10, 0,
			0, 0, false, false, 0, 0, true, "EXACTLY");
	private static ImageComparison imgCompPFuzzy = new ImageComparison(10, 10, 0,
			0, 0, false, false, 0, 0, true, "PIXELFUZZY");
	private static ImageComparison imgCompFuzzy = new ImageComparison(10, 10, 1,
			0, 0, false, false, 0, 0, true, "FUZZY");

	/**
	 * Initializes two 200 * 200 images, all pixels get a random number between
	 * -16777216 and 0 as rgb value
	 */
	@BeforeClass
	public static void initializeImages() {
		final Random random = new Random();

		int rgb;
		reference = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				rgb = random.nextInt(16777216) * (-1);
				reference.setRGB(w, h, rgb);
			}
		}

		screenshot = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		for (int w = 0; w < screenshot.getWidth(); w++) {
			for (int h = 0; h < screenshot.getHeight(); h++) {
				rgb = random.nextInt(16777216) * (-1);
				screenshot.setRGB(w, h, rgb);
			}
		}
	}

	/**
	 * Creates a markedImage and a difference image from the EXACTLY comparison
	 * and the FUZZY comparison. Asserts that the marked images and the
	 * difference images from the different algorithms are equal. Does the same
	 * again for pixelFuzzy
	 * 
	 * @throws IOException
	 */
	@Test
	public void test() throws IOException {

		// Compares FUZZY against EXACTLY 
		imgCompExact.isEqual(reference, screenshot, fileMask, fileOutExactly,
				differenceFileExactly);
		imgCompFuzzy.isEqual(reference, screenshot, fileMask, fileOutFuzzy,
				differenceFileFuzzy);

		BufferedImage reference = ImageIO.read(fileOutExactly);
		BufferedImage screenshot = ImageIO.read(fileOutFuzzy);
		Assert.assertTrue(imgCompExact.isEqual(reference, screenshot, fileMask, thrashFile, thrashFile));
		reference = ImageIO.read(differenceFileExactly);
		screenshot = ImageIO.read(differenceFileFuzzy);
		Assert.assertTrue(imgCompExact.isEqual(reference, screenshot, fileMask, thrashFile, thrashFile));

		// Compares PIXELFUZZY against EXACTLY 
		imgCompExact.isEqual(reference, screenshot, fileMask, fileOutExactly,
				differenceFileExactly);
		imgCompPFuzzy.isEqual(reference, screenshot, fileMask, fileOutFuzzy,
				differenceFileFuzzy);

		reference = ImageIO.read(fileOutExactly);
		screenshot = ImageIO.read(fileOutFuzzy);
		Assert.assertTrue(imgCompExact.isEqual(reference, screenshot, fileMask, thrashFile, thrashFile));
		reference = ImageIO.read(differenceFileExactly);
		screenshot = ImageIO.read(differenceFileFuzzy);
		Assert.assertTrue(imgCompExact.isEqual(reference, screenshot, fileMask, thrashFile, thrashFile));

	}

	/**
	 * Deletes the temporary files which were created for this test
	 */
	@AfterClass
	public static void deleteFile() {
		fileMask.delete();
		fileOutExactly.delete();
		fileOutFuzzy.delete();
		differenceFileExactly.delete();
		differenceFileFuzzy.delete();
		thrashFile.delete();
	}

}
