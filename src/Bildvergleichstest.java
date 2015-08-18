import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Bildvergleichstest {
	

	public static void main(String[] args) throws IOException {
		File first = new File("/home/lzoulkowski/Pictures/spektrum1.png");
		File second = new File("/home/lzoulkowski/Pictures/test.png");
		String imgOutputDifferences = "/home/lzoulkowski/Pictures/new_screenshot_with_changes.jpg";
		
		BufferedImage img1 = ImageIO.read(first);
		BufferedImage img2 = ImageIO.read(second);
		
		
		ImageComparison imageComparison = new ImageComparison(50,50,0.05);
		
//		if (imageComparison.pixelFuzzyEqual(img1, img2, imgOutputDifferences)) 
//			System.out.println("Die Bilder sind gleich");
//		else 
//			System.out.println("Die Bilder sind nicht gleich");
		
//		if(imageComparison.exactlyEqual(img1, img2, imgOutputDifferences))
//			System.out.println("Die Bilder sind gleich");
//		else 
//			System.out.println("Die Bilder sind nicht gleich");
		
	
		if(imageComparison.fuzzyEqual(first,second,imgOutputDifferences))
			System.out.println("Images are fuzzy-equal.");
		else
			System.out.println("Images are not fuzzy-equal.");
	}
}
