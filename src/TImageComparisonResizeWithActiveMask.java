import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TImageComparisonResizeWithActiveMask {
	
//	Tests whether a preexistent mask would be used if the screenshot image is smaller or bigger
//  The intention is that, no, if there's change in size, a new maskImage should be created
//	This should be dependent on the 
	
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
	
	@Test
	public void biggerScreenshotImage() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(3, 3, 0.1, false);
		if (imagecomparison1.fuzzyEqual(smallBlackImg, bigWhiteImg, fileMask, fileOut)) {
			Assert.assertTrue("Former maskImage shouldn't be used if the screenshot has a" +
					          " bigger size - ResizeWithActiveMask", false);
		}
	}
	
	@Test
	public void smallerScreenshotImage() throws IOException {
		ImageComparison imagecomparison1 = new ImageComparison(3, 3, 0.1, false);
		if (imagecomparison1.fuzzyEqual(bigWhiteImg, smallBlackImg, fileMask, fileOut)) {
			Assert.assertTrue("Former maskImage shouldn't be used if the screenshot has a" +
					          " bigger size - ResizeWithActiveMask", false);
		}
	}
	
	
	
	public void t() throws IOException {
		ImageComparison imagecomparison2 = new ImageComparison(2, 2, 0.0000000000000001, true);
		imagecomparison2.fuzzyEqual(smallBlackImg, bigWhiteImg, fileMask, fileOut);
		BufferedImage maskImage = ImageIO.read(fileMask);
		for (int w=0; w<bigWhiteImg.getWidth(); w++) {
			for (int h=0; h<bigWhiteImg.getHeight(); h++) {
				if (maskImage.getRGB(w, h) != Color.BLACK.getRGB()) {
					Assert.assertTrue("MaskImage should be completely black - ResizeWithActiveMask", false);
				}
			}
		}
	}

//	Deletes the created imagefiles. sComment this out if you want to see the output
	@After
	public void deleteFiles() {
		fileMask.delete();
		fileOut.delete();
	}
	
//	No relation to the code in ImageComparison
	private BufferedImage initializeBlackMaskImage(BufferedImage img) {
		BufferedImage maskImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = maskImg.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, maskImg.getWidth(), maskImg.getHeight());
		g.dispose();
		return maskImg;
	}
}
