import java.awt.Color;
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

	private final static int rgbBlack = Color.BLACK.getRGB();
	private final static Color White = new Color(255, 255, 255, 0);
	private final static int rgbWhite = White.getRGB();
	private final static int rgbMarked = Color.RED.getRGB();

	private static BufferedImage reference = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
	private static BufferedImage screenshot = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
	private static BufferedImage mask = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);

	public static void main(String[] args) throws IOException {
		
		BufferedImage img = ImageIO.read(first);
		ImageComparison imagecomparison = new ImageComparison(10, 10, 0.1, false, "FUZZYEQUAL");
		img = imagecomparison.getEdgeImage(img);
		ImageIO.write(img, "PNG", second);
	}

	public static void initializeImages() throws IOException {
		// Initializes the reference, screenshot and the maskImage;

		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
					reference.setRGB(w, h, rgbBlack);
			}
		}
		
		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
					screenshot.setRGB(w, h, rgbMarked);
			}
		}

		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				if (h >= 250) {
					mask.setRGB(w, h, rgbBlack);
				} else {
					mask.setRGB(w, h, rgbWhite);
				}
			}
		}
	}
}