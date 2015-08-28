import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class Bildvergleichstest {
	

	public static void main(String[] args) throws IOException {
		
		BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
		int rgbRed = Color.RED.getRGB();
		for (int w=0; w<img.getWidth(); w++) {
			for (int h=0; h<img.getHeight(); h++) {
				img.setRGB(w, h, rgbRed);
			}
		}
		System.out.println("Alpha red: " + Color.RED.getAlpha());
		img = increaseImageSize(img, 10, 20);
		

		//		int[][] array = new int[5][5];
//		Arrays.fill(array[2], 0);
//		
//		System.out.println(Arrays.toString(array[2]));

		for (int w=0; w<img.getWidth(); w++)
			for (int h=0; h<img.getHeight(); h++) {
				System.out.println("w: " + w + "| h: " + h  + isTransparent(img, w, h));
				Color c = new Color(img.getRGB(w, h), true);
				System.out.println(c.getAlpha());
			}
	}

	private static BufferedImage increaseImageSize(BufferedImage img, int width, int height) {
  		BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//  	fills the new parts of the image with transparent black, ie rgb = 0
  		for (int w=img.getWidth(); w<width; w++) {
  			for (int h=img.getHeight(); h<height; h++) {
  				newImg.setRGB(w, h, 0);
  			}
  		}
  		Graphics g = newImg.createGraphics();
  		g.drawImage(img, 0, 0, null);
  		g.dispose();
  		return newImg;
  	}
	
//	Checks if a certain pixel of a certain image has an alpha value of zero
  	private static boolean isTransparent(BufferedImage img, int x, int y) {
  		int rgb = img.getRGB(x, y);
  		Color color = new Color(rgb, true);
  		if (color.getAlpha() == 0) {
  			return true;
  		}
  		return false;
  	}
}