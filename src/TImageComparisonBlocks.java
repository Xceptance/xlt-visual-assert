import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TImageComparisonBlocks {

	private BufferedImage reference;
	private BufferedImage screenshot;
	
	private String pathHome = System.getProperty("user.home");
	private File fileMask = new File(pathHome + "/maskImage.png");
	private File fileOut = new File(pathHome + "/output.png");
	
	@Before
	public void initializeImages() {
		BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		Color black = Color.BLACK;
		int rgbBlack = black.getRGB();
		for (int w=0; w<reference.getWidth(); w++) { 
			for (int h=0; h<reference.getHeight(); h++) {
				reference.setRGB(w, h, rgbBlack);
			}
		}
		this.reference = reference;
		
		BufferedImage screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		Color white = Color.WHITE;
		int rgbWhite = white.getRGB();
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
		this.screenshot = screenshot;
	}	
	
//	Tests whether or not the block system is working as expected. The initialized images have a difference every tenth pixel. 
//	With a threshold of 10 percent, the following function should return true since the blocks are congruent.
	@Test	
	public void blocksExactly() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 1, 0.1, false);
		if (!imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("Unexpected result in the threshold calculation: blockyExactly", false);
		}
	}
	
	
//	The blocks are slightly wider then ten pixels and should shift relative to the image 
//	until there are two differences in one block
	@Test
	public void blocksBarelyBigger() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(11, 1, 0.1, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("An unexpected result in the threshold calculation: blocksBarelyBigger", false);
		}
		
		fileMask.delete();
		fileOut.delete();
	}
	
//	The blocks are slightly smaller then ten pixels and should shift relative to he image
//	until there are two differences in one block
	@Test				
	public void blocksBarelySmaller() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(9, 1, 0.1, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("An unexpected result in the threshold calculation: blocksBarelySmaller", false);
		}
	}
	
//	This test tests what happens if the borders go over the edge on the right
//	It should also check the drawBorders method, since the treshold is so low, it should find differences everywhere
	@Test				
	public void blocksGoOverBorderLeft() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(14, 1, 0.0000000000000001, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("the drawBorders function could not be tested, blocksGoOverBorderLeft didn't find a difference to mark", false);
		}
	}

//	This test tests what happens if the borders go over the edge on the bottom
//	It should also check the drawBorders method, since the treshold is so low, it should find differences everywhere
	@Test				
	public void blocksGoOverBorderBottom() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 28, 0.0000000000000001, false);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			Assert.assertTrue("the drawBorders function could not be tested, blocksGoOverBorderBottom didn't find a difference to mark", false);
		}
	}
	
	@After
	public void deleteFile() {
		fileMask.delete();
	}
}
