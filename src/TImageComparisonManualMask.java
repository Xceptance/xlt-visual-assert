import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;


public class TImageComparisonManualMask {
	
//	This class tests whether or not you can manually manipulate the mask image, 
//	ie if it does what it should when you paint it black/ white with a image editor
//	It is not for automated testing
//	The initialized Images are completely different at the start
	
	private BufferedImage reference;
	private BufferedImage screenshot;
	
	private String pathHome = System.getProperty("user.home");
	private File fileMask = new File(pathHome + "/maskImage.png");
	private File fileOut = new File(pathHome + "/output.png");
	
	@Before
	public void initializeImages() throws IOException {
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
			for (int h=0; h<(screenshot.getHeight()/2); h++) {
				screenshot.setRGB(w, h, rgbWhite);
			}	
		}
		this.screenshot = screenshot;
		
	}
	
//	This method has to be changed manually to do any work, set traininMode = false after you've initialized the mask image
	@Test	
	public void test() throws IOException {
		ImageComparison imagecomparison = new ImageComparison(10, 10, 0.1, true);
		if (imagecomparison.fuzzyEqual(reference, screenshot, fileMask, fileOut)) {
			System.out.println("Images are equal");
		}
		else {
			System.out.println("Images arn't equal");
		}
			
	}
}