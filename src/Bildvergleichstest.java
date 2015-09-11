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
	
//	private static BufferedImage reference = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
//	private static BufferedImage screenshot = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);

	public static void main(String[] args) throws IOException {
		
//		initializeImages();

		BufferedImage screenshot = ImageIO.read(first);
		BufferedImage reference = ImageIO.read(second);
		final String algorithm = "EXACTLYEQUAL";
		
		ImageComparison imagecomparison = new ImageComparison(10, 0.1, false, false, algorithm);
		boolean result = imagecomparison.isEqual(screenshot, reference, fileMask, fileOut);
		if (result) {
			System.out.println("Images are equal!");
		}
		
		else {
			System.out.println("Images arn't equal!");
		}
	}
	
//	private static void initializeImages() {
//		for (int w = 0; w < reference.getWidth(); w++) {
//			for (int h = 0; h < reference.getHeight() / 2; h++) {
//				reference.setRGB(w, h, Color.BLACK.getRGB());
//			}	
//			for (int h = reference.getHeight() / 2 +1; h < reference.getHeight(); h++) {
//				reference.setRGB(w, h, Color.WHITE.getRGB());
//			}
//		}
//		
//		for (int w = 0; w < screenshot.getWidth(); w++) {
//			for (int h = 0; h < screenshot.getHeight(); h++) {
//				screenshot.setRGB(w, h, Color.WHITE.getRGB());
//			}
//		}
//	}
}