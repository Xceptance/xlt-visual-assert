//	Checks whether two images with identical red, green and blue values but different alpha values will come back different
//	One Alpha value will be 255, he other 0. The expected result is YES! 
//	Different test cases for 	pixelPerBlockX, pixelPerBlockY > 1, threshold > 0.0					(fuzzyEqual)
//								pixelPerBlockX, pixelPerBlockY = 1, threshold > 0.0					(pixelFuzzyEqual
//								pixelPerBlockX, pixelPerBlockY = 1, threshold = 0.0					(exactlyEqual)

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TImageComparisonInfluenceAlpha {

	private BufferedImage notTransparentImg;
	private BufferedImage transparentImg;

	private String pathHome = System.getProperty("user.home");
	private File fileMask = new File(pathHome + "/maskImage.png");
	private File fileOut = new File(pathHome + "/output.png");

//	Initializes the images; the notTransparentImg Image is black, the transparentImg image transparent black
	@Before
	public void initializeImages() {
		BufferedImage notTransparentImg = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		int red = 0;
		int green = 0;
		int blue = 0;
		int alpha = 255;
		Color cReference = new Color (red, green, blue, alpha);
		int rgb = cReference.getRGB();
		for (int w=0; w<notTransparentImg.getWidth(); w++) { 
			for (int h=0; h<notTransparentImg.getHeight(); h++) {
				notTransparentImg.setRGB(w, h, rgb);
			}
		}
		this.notTransparentImg = notTransparentImg;
	
		BufferedImage transparentImg = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		alpha = 0;
		Color cScreenshot = new Color (red, green, blue, alpha);
		rgb = cScreenshot.getRGB();
		for (int w=0; w<transparentImg.getWidth(); w++) { 
			for (int h=0; h<transparentImg.getHeight(); h++) {
				transparentImg.setRGB(w, h, rgb);
			}	
		}
		this.transparentImg = transparentImg;
	}
	
//	Tests as said at the top. Tests once for the transparentImg as screenshot and once for the transparentImg as reference
	@Test
	public void influenceAlphaFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(2, 2, 0.01, false);
		if (imagecomparison.fuzzyEqual(notTransparentImg, transparentImg, fileMask, fileOut)) {
			Assert.assertTrue("A transparent screenshot went undetected - influenceAlphaFuzzyEqual", false);
		}
		if (imagecomparison.fuzzyEqual(transparentImg, notTransparentImg, fileMask, fileOut)) {
			Assert.assertTrue("A transparent reference image went undetected - influenceAlphaFuzzyEqual", false);
		}
	}
	
//	Tests as said at the top. Tests once for the transparentImg as screenshot and once for the transparentImg as reference
	@Test
	public void influenceAlphaPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.01, false);
		if (imagecomparison.fuzzyEqual(notTransparentImg, transparentImg, fileMask, fileOut)) {
			Assert.assertTrue("A transparent screenshot went undetected - influenceAlphaPixelFuzzyEqual", false);
		}
		if (imagecomparison.fuzzyEqual(transparentImg, notTransparentImg, fileMask, fileOut)) {
			Assert.assertTrue("A transparent reference Image went undetected - influenceAlphaPixelFuzzyEqual", false);
		}
	}
	
//	Tests as said at the top. Tests once for the transparentImg as screenshot and once for the transparentImg as reference
	@Test
	public void influenceAlphaExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.0, false);
		if (imagecomparison.fuzzyEqual(notTransparentImg, transparentImg, fileMask, fileOut)) {
			Assert.assertTrue("A transparent screenshot went undetected - influenceAlphaExactlyEqual", false);
		}
		if (imagecomparison.fuzzyEqual(transparentImg, notTransparentImg, fileMask, fileOut)) {
			Assert.assertTrue("A transparent reference Image went undetected - influenceAlphaExactlyEqual", false);
		}
	}
}
