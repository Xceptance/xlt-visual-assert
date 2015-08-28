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
		Bildvergleichstest b = new Bildvergleichstest();
//		img = b.increaseImageSize(img, 10, 10);
//		
//		int[][] array = new int[5][5];
//		Arrays.fill(array[2], 0);
//		
//		System.out.println(Arrays.toString(array[2]));
		
		img.setRGB(1, 1, 0);
		System.out.println(img.getRGB(1, 1));
		Color c = new Color(0, true);
		System.out.println(c.getAlpha());
		c = Color.BLACK;
		System.out.println(c.getAlpha());
		
		
		
	}

	private static BufferedImage increaseImageSize(BufferedImage img, int width, int height) {
  		BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
  		Graphics g = newImg.createGraphics();
  		g.drawImage(newImg, 0, 0, null);
  		int alpha = newImg.getRGB(5, 5) >> 24;
//  		System.out.println(alpha);
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