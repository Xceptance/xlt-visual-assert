import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Bildvergleichstest {
	

	public static void main(String[] args) throws IOException {
		File first = new File("/home/lzoulkowski/Pictures/weiss.jpeg");
		File second = new File("/home/lzoulkowski/Pictures/schwarz.jpeg");
		String imgOutputDifferences = "/home/lzoulkowski/Pictures/new_screenshot_with_changes.jpg";
		
		BufferedImage img1 = ImageIO.read(first);
		BufferedImage img2 = ImageIO.read(second);
		
		double maxDifference = 255 * 255 * 255;
		double difference = Math.abs(img1.getRGB(40, 40) - img2.getRGB(40, 40));
		double diff = difference/maxDifference;
		System.out.println(img1.getRGB(40, 40));
		System.out.println (img2.getRGB(40, 40));
		System.out.println(difference);
		System.out.println(diff);
		
		
		ImageComparison imageComparison = new ImageComparison(1,1,0.00);
		
//		if(imageComparison.exactlyEqual(img1, img2, imgOutputDifferences))
//			System.out.println("Die Bilder sind gleich");
//		else 
//			System.out.println("Die Bilder sind nicht gleich");
		
	
//		if(imageComparison.fuzzyEqual(first,second,imgOutputDifferences))
//			System.out.println("Images are fuzzy-equal.");
//		else
//			System.out.println("Images are not fuzzy-equal.");
	}
}
