import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Bildvergleichstest {
	

	public static void main(String[] args) throws IOException {
		File first = new File("/home/daniel/Pictures/spektrum1.png");
		File second = new File("/home/daniel/Pictures/test.png");
		String imgOutputDifferences = "/home/daniel/Pictures/new_screenshot_with_changes.png";
		
		BufferedImage img1 = ImageIO.read(first);
		BufferedImage img2 = ImageIO.read(second);
		
		img1 = overwriteTransparentPixels(img1, img2);
		ImageComparison imageComparison = new ImageComparison(30,30,0.05);
		
		if (imageComparison.pixelFuzzyEqual(img1, img2, imgOutputDifferences)) 
			System.out.println("Die Bilder sind gleich");
		else 
			System.out.println("Die Bilder sind nicht gleich");
		
//		if(imageComparison.exactlyEqual(img1, img2, imgOutputDifferences))
//			System.out.println("Die Bilder sind gleich");
//		else 
//			System.out.println("Die Bilder sind nicht gleich");
		
	
//		if(imageComparison.fuzzyEqual(img1, img2,imgOutputDifferences))
//			System.out.println("Images are fuzzy-equal.");
//		else
//			System.out.println("Images are not fuzzy-equal."); 
	}
	
    /**
     * Overwrites all transparent pixels in the reference image with corresponding pixels of the screenshot image.
     * Pseudotransparency
     * 
     * @throws IOException
     */
	private static BufferedImage overwriteTransparentPixels(BufferedImage reference, BufferedImage screenshot) {
		for (int w=0; w<reference.getWidth(); w++) {
			for (int h=0; h<reference.getHeight(); h++) {
				int alpha = (reference.getRGB(w, h) >> 24) & 0xFF;
				if (alpha == 0) {
					int rgb = screenshot.getRGB(w, h);
					reference.setRGB(w, h, rgb);
				}
			}
		}
		return reference;
	}
}
