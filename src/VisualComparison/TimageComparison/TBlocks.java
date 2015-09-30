package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;

/**
 * Tests if the method can handle it should the blocks would go over the border
 *
 * @author damian
 */
public class TBlocks {

	private static BufferedImage reference;
	private static BufferedImage screenshot;
	
	private final static int rgbBlack = Color.BLACK.getRGB();
	private final static int rgbWhite = Color.WHITE.getRGB();
	
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	private static File differenceFile = new File(directory + "/difference.png");
	
	@BeforeClass
	public static void initializeImages() {
		reference = new BufferedImage(301, 301, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
		
		screenshot = new BufferedImage(301, 301, BufferedImage.TYPE_INT_RGB);
		for (int w=0; w<screenshot.getWidth(); w++) { 
			for (int h=0; h<screenshot.getHeight(); h++) {
				if ((w % 10) == 0) { 
					screenshot.setRGB(w, h, rgbWhite);
				}	
				else {
					screenshot.setRGB(w, h, rgbBlack);
				}
			}	
		}
	}	
	
	/**
	 * Tests what happens if the blocks go over the border
	 * @throws IOException
	 */
	@Test				
	public void blocksGoOverBorder() throws IOException {
		
//		This test tests what happens if the borders go over the edge
//		It should also check the drawBorders method, since the treshold is so low, it should find differences everywhere
		ImageComparison imagecomparison2 = new ImageComparison(10, 10, 20, 0.0, 0.01, false, false, 3, 3, false, "FUZZY");
		boolean result = imagecomparison2.isEqual(reference, screenshot, fileMask, fileOut, differenceFile);
		Assert.assertFalse("This wasn't what the test is for, but it's still wrong", result);
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
