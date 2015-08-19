import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class CheckTransparencyImageRedye {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		File fileReference = new File("/home/daniel/workspace/reference.png");
		File fileScreenshot = new File("/home/daniel/workspace/screenshot.png");
		String output = "/home/daniel/workspace/output.jpg";
		String check = "/home/daniel/workspace/check.png";
		File fileCheck = new File(check);
        try {
			BufferedImage referenceImage = ImageIO.read(fileReference);
			BufferedImage screenshotImage = ImageIO.read(fileScreenshot);
			for (int w=0; w<referenceImage.getWidth(); w++) {
				for (int h=0; h<referenceImage.getHeight(); h++) {
					int alpha = (referenceImage.getRGB(w, h) >> 24) & 0xFF;
					if (alpha == 0) {
						int rgb = screenshotImage.getRGB(w, h);
						referenceImage.setRGB(w, h, rgb);
					}		
				}
			}	
		ImageIO.write(referenceImage, "PNG", fileCheck); 
		ImageComparison imagecomparison = new ImageComparison(5, 5, 0.3);
		if (imagecomparison.fuzzyEqual(referenceImage, screenshotImage, output)) {
			System.out.println("The Images are fuzzy equal!");
		}
		else System.out.println("The Images arn't fuzzy equal!");
		} catch (IOException e) {
			System.out.println("Error trying to read Files");
			e.printStackTrace();
		}

	}
}
