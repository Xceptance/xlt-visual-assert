import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

import VisualComparison.ImageComparison;

public class Bildvergleichstest {
	

	public static void main(String[] args) throws IOException {
		File fileMask = new File("/home/daniel/Pictures/maskImage.png");
		File fileOut = new File("/home/daniel/Pictures/output.png");
		File second = new File("/home/daniel/Pictures/test.png");
		
		BufferedImage white = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		int[] whiteArray = ((DataBufferInt) white.getRaster().getDataBuffer()).getData();
		int rgbWhite = Color.WHITE.getRGB();
		Arrays.fill(whiteArray, rgbWhite);
		for (int i=5000; i<15000; i++) {
			whiteArray[i] = Color.RED.getRGB();
		}
		
		BufferedImage red = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
		int[] redArray = ((DataBufferInt) red.getRaster().getDataBuffer()).getData();
		int rgbRed = Color.RED.getRGB();
		Arrays.fill(redArray, rgbRed);
		ImageIO.write(red, "PNG", second);

		
		ImageComparison imagecomparison = new ImageComparison(1, 1, 0.00, false);
		imagecomparison.fuzzyEqual(white, red, fileMask, fileOut);
	}
}