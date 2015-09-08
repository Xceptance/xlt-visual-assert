import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import VisualComparison.ImageComparison;

public class Bildvergleichstest {

	private final static File fileMask = new File(
			"/home/daniel/Pictures/maskImage.png");
	private final static File fileOut = new File(
			"/home/daniel/Pictures/output.png");
	private final static File first = new File(
			"/home/daniel/Pictures/first.png");
	private final static File second = new File(
			"/home/daniel/Pictures/second.png");

	public static void main(String[] args) throws IOException {
		
		BufferedImage reference = ImageIO.read(first);
		BufferedImage screenshot = ImageIO.read(second);
		
		final String algorithm = "PIXELFUZZYEQUAL";
		
		ImageComparison imagecomparison = new ImageComparison(10, 0.1, false, algorithm);
		boolean result = imagecomparison.isEqual(reference, screenshot, fileMask, fileOut);
		if (result) {
			System.out.println("Images are equal!");
		}
		
		else {
			System.out.println("Images arn't equal!");
		}
	}
}