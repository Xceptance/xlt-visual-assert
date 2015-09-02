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

public class TImageComparisonInfluenceAlpha {
//				Tests if the methods detect it if alpha is zero. Expected: Yes, they do. 
//				The test doesn't test for alpha values between 255 and zero.
	
	private static BufferedImage notTransparentImg;
	private static BufferedImage transparentImg;
	
	private final static int rgbBlack = new Color(0, false).getRGB(); 
	private final static int rgbTransparentBlack = new Color(0, true).getRGB(); 
	
	private final static File directory = SystemUtils.getJavaIoTmpDir();
	private static File fileMask = new File(directory, "/fileMask.png");
	private static File fileOut = new File(directory, "/fileOut.png");
	
//	Initializes the images; the notTransparentImg Image is black, the transparentImg image transparent black
	@BeforeClass
	public static void initializeImages() {
		notTransparentImg = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

		for (int w=0; w<notTransparentImg.getWidth(); w++) { 
			for (int h=0; h<notTransparentImg.getHeight(); h++) {
				notTransparentImg.setRGB(w, h, rgbBlack);
			}
		}
	
		transparentImg = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		for (int w=0; w<transparentImg.getWidth(); w++) { 
			for (int h=0; h<transparentImg.getHeight(); h++) {
				transparentImg.setRGB(w, h, rgbTransparentBlack);
			}	
		}
	}
	
//	Tests as said at the top. Tests once for the transparentImg image as screenshot
//	and once for the transparentImg image as reference
	@Test
	public void influenceAlphaFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(2, 2, 0.01, false);
		boolean result = imagecomparison.fuzzyEqual(notTransparentImg, transparentImg, fileMask, fileOut);
		Assert.assertFalse("A transparent screenshot went undetected - influenceAlphaFuzzyEqual", result);
		result = imagecomparison.fuzzyEqual(transparentImg, notTransparentImg, fileMask, fileOut);
		Assert.assertFalse("A transparent reference image went undetected - influenceAlphaFuzzyEqual", result);
	}
	
//	Tests as said at the top. Tests once for the transparentImg as screenshot and once for the transparentImg as reference
	@Test
	public void influenceAlphaPixelFuzzyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.01, false);
		boolean result = imagecomparison.fuzzyEqual(notTransparentImg, transparentImg, fileMask, fileOut);
		Assert.assertFalse("A transparent screenshot went undetected - influenceAlphaPixelFuzzyEqual", result);
		
		result = imagecomparison.fuzzyEqual(transparentImg, notTransparentImg, fileMask, fileOut);
		Assert.assertFalse("A transparent reference Image went undetected - influenceAlphaPixelFuzzyEqual", result);	
	}
	
//	Tests as said at the top. Tests once for the transparentImg as screenshot and once for the transparentImg as reference
	@Test
	public void influenceAlphaExactlyEqual() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.0, false);
		boolean result = imagecomparison.fuzzyEqual(notTransparentImg, transparentImg, fileMask, fileOut);
		Assert.assertFalse("A transparent screenshot went undetected - influenceAlphaExactlyEqual", result);
		result = imagecomparison.fuzzyEqual(transparentImg, notTransparentImg, fileMask, fileOut);
		Assert.assertFalse("A transparent reference Image went undetected - influenceAlphaExactlyEqual", result);
	}
	
	@AfterClass
	public static void deleteFile() {
		fileMask.delete();
		fileOut.delete();
	}
}
