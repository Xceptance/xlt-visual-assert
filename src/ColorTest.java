import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ColorTest {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        File fileRed = new File("/home/daniel/workspace/red.png");
        BufferedImage redImg = ImageIO.read(fileRed);
        Color red = new Color(redImg.getRGB(2, 2));
        System.out.println("RGB: " + red.getRGB());
        System.out.println("Red: " + red.getRed());
       
        System.out.println("Debug: red.getRGB(): " + red.getRGB()); //Debug
        Color rgb = new Color (150, 0, 0, 0);
        System.out.println("Debug: rgb.getRGB(): " + rgb.getRGB()); //Debug
        redImg.setRGB(2,2,rgb.getRGB());
       
        red = new Color(redImg.getRGB(2, 2));
        System.out.println("RGB: " + redImg.getRGB(2,2));
        System.out.println("Red: " + red.getRed());
       
    }
   
    public static BufferedImage setAlpha(BufferedImage img) {
        for (int w=0; w<10; w++) {
            for (int h=0; h<10; h++) {
                Color color = new Color(img.getRGB(w,h));
                int red = color.getRed();
                int green = color.getGreen();
                int blue = color.getBlue();
                int alpha = 0;
                Color rgb = new Color (red, green, blue, alpha);
                img.setRGB(w,h,rgb.getRGB());
                System.out.print("geht");
            }
        }
        return img;
    }
   
    public static void printRGB(BufferedImage img) throws IOException {
        Color red = new Color(img.getRGB(2, 2));
        System.out.println("RGB: " + red.getRGB());
        System.out.println("Red: " + red.getRed());
        System.out.println("Green: " + red.getGreen());
        System.out.println("Blue: " + red.getBlue());
        System.out.println("Alpha: " + red.getAlpha());
    }
}
