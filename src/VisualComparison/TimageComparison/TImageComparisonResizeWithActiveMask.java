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


public class TImageComparisonResizeWithActiveMask {
	
//	Tests whether a pre-existent mask would be used if the screenshot image is smaller or bigger
//  The intention is that, no, if there's change in size, a new maskImage should be created
//	Includes separate methods for exactlyEqual and pixelFuzzyEqual
	
	private BufferedImage smallBlackImg;
	private BufferedImage bigWhiteImg;
	
	private String pathHome = System.getProperty("user.home");
	private File fileMask = new File(pathHome + "/maskImage.png");
	private File fileOut = new File(pathHome + "/output.png");
	
	@Before
	public void initializeImagesAndMask() throws IOException {
//		Reference and bigWhiteImg and are equal except for the bottom rows 300-400, 
//		which are white in the bigWhiteImg and nonexistent in the smallBlackImg
		
		BufferedImage smallBlackImg = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
		Color black = Color.BLACK;
		int rgbBlack = black.getRGB();
		for (int w=0; w<smallBlackImg.getWidth(); w++) { 
			for (int h=0; h<smallBlackImg.getHeight(); h++) {
				smallBlackImg.setRGB(w, h, rgbBlack);
			}
		}
		this.smallBlackImg = smallBlackImg;
	
		BufferedImage bigWhiteImg = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
		Color white = Color.WHITE;
		int rgbWhite = white.getRGB();
		for (int w=0; w<bigWhiteImg.getWidth(); w++) { 
			for (int h=0; h<bigWhiteImg.getHeight(); h++) {
				bigWhiteImg.setRGB(w, h, rgbWhite);
			}
		}	
		
		this.bigWhiteImg = bigWhiteImg;
		
		BufferedImage img = initializeBlackMaskImage(smallBlackImg);
		ImageIO.write(img, "PNG", fileMask);
	}
	
//	Tests what happens if the screenshotImage is bigger. 
//	Tests the normal fuzzyEqual method.
	@Test
	public void biggerScreenshotImage() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(3, 3, 0.1, false);
		if (imagecomparison1.fuzzyEqual(smallBlackImg, bigWhiteImg, fileMask, fileOut)) {
			Assert.assertTrue("Former maskImage shouldn't be used if the screenshot has a bigger size", false);
		}
	}
	
//	Changes the order of the parameters and tests what happens if the screenshot image is smaller.
//	Tests the normal fuzzyEqual method.
	@Test
	public void smallerScreenshotImage() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(3, 3, 0.1, false);
		if (imagecomparison1.fuzzyEqual(bigWhiteImg, smallBlackImg, fileMask, fileOut)) {
			Assert.assertTrue("Former maskImage shouldn't be used if the screenshot has a smaller size", false);
		}
	}
	
//	Tests what happens if the screenshotImage is bigger.
//	Tests the exactlyEqual method.
	@Test
	public void biggerScreenshotImageExactlyEqual() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(1, 1, 0.00, false);
		if (imagecomparison1.fuzzyEqual(smallBlackImg, bigWhiteImg, fileMask, fileOut)) {
			Assert.assertTrue("Former maskImage shouldn't be used if the screenshot has a bigger size", false);
		}
	}
	
//	Changes the order of the parameters and tests what happens if the screenshot image is smaller.
//	Tests the exactlyEqual method.
	@Test
	public void smallerScreenshotImageExactlyEqual() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(1, 1, 0.00, false);
		if (imagecomparison1.fuzzyEqual(bigWhiteImg, smallBlackImg, fileMask, fileOut)) {
			Assert.assertTrue("Former maskImage shouldn't be used if the screenshot has a smaller size", false);
		}
	}
	
//	Tests what happens if the screenshotImage is bigger.
//	Tests the pixelFuzzyEqual method.
	@Test
	public void biggerScreenshotImagePixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(1, 1, 0.1, false);
		if (imagecomparison1.fuzzyEqual(smallBlackImg, bigWhiteImg, fileMask, fileOut)) {
			Assert.assertTrue("Former maskImage shouldn't be used if the screenshot has a bigger size", false);
		}
	}
	
//	Changes the order of the parameters and tests what happens if the screenshot image is smaller.
//	Tests the pixelFuzzyEqual method.
	@Test
	public void smallerScreenshotImagePixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(1, 1, 0.1, false);
		if (imagecomparison1.fuzzyEqual(bigWhiteImg, smallBlackImg, fileMask, fileOut)) {
			Assert.assertTrue("Former maskImage shouldn't be used if the screenshot has a smaller size", false);
		}
	}
	
	
	
//	Deletes the created imagefiles. sComment this out if you want to see the output
	@After
	public void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
	}
	
//	No relation to the code in ImageComparison
//	Creates the maskImage image, creates the array with its pixels and fills the array with zeroes
	private BufferedImage initializeBlackMaskImage(BufferedImage img) {
		BufferedImage maskImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		int[] maskImgArray = ((DataBufferInt) maskImg.getRaster().getDataBuffer()).getData();
		Arrays.fill(maskImgArray, 0);
		return maskImg;
	}
}
