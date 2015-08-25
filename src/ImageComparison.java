import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;

public class ImageComparison {
    private BufferedImage imgOut = null;
    private int pixelPerBlockX;
    private int pixelPerBlockY;
    private double threshold;
    private boolean trainingMode;
   
    public ImageComparison(int pixelPerBlockX, int pixelPerBlockY, double threshold, boolean trainingMode) {
        this.pixelPerBlockX = pixelPerBlockX;
        this.pixelPerBlockY = pixelPerBlockY;
        this.threshold = threshold;
        this.trainingMode = trainingMode;
    }
   
      
    public boolean pixelFuzzyEqual (BufferedImage img1, BufferedImage img2, File fileOut) throws IOException {
        /*Method for pixel-based fuzzy comparison*/
       
        boolean equal = true;
//        adapts size of image 2 if necessary
        if ((img1.getWidth() != img2.getWidth()) || (img1.getHeight() != img2.getHeight())) {
            img2 = adaptImageSize(img1,img2);
        }
       
        imgOut = imageToBufferedImage(img2);

       
        int imagewidth = img1.getWidth();
        int imageheight = img1.getHeight();
        int rgbRed = (255 << 24) | (255 << 16) | (0 << 8) | 0;                        
        for (int x = 0; x<imagewidth; x++) {
            for(int y = 0;y<imageheight; y++) {
//                calculates difference and marks them red if above threshold
                if (calculatePixelRgbDiff(x, y, img1, img2) > threshold) {
                    imgOut.setRGB(x, y, rgbRed);                                   
                    equal = false;
                }
            }
        }
       
        if (!equal) {
            saveImage(imgOut,fileOut);
        }
        return equal;
    }

    public boolean exactlyEqual (BufferedImage img1, BufferedImage img2, File fileOut) throws IOException {
        /*Method for the exact comparison of two images*/
        //img1: reference Image, img2: screenshot
        boolean exactlyEqual = true;
//        adapts size of image 2 if necessary
        if ((img1.getWidth() != img2.getWidth()) || (img1.getHeight() != img2.getHeight())) {
            img2 = adaptImageSize(img1,img2);
        }
       
       
        imgOut = imageToBufferedImage(img2);
   
        int imagewidth = img1.getWidth();
        int imageheight = img1.getHeight();
        int rgbRed = (255 << 24) | (255 << 16) | (0 << 8) | 0;            
        for (int x = 0; x<imagewidth; x++) {                           
            for(int y = 0;y<imageheight; y++) {
//                if the RGB values of 2 pixels differ print them red and set equal false
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        exactlyEqual = false;                                                       
                        imgOut.setRGB(x, y, rgbRed);                   
                }
            }
        }
        if (!exactlyEqual) {
                saveImage(imgOut,fileOut);
        }
       
        return exactlyEqual;
    }
   
    public boolean fuzzyEqual(BufferedImage img1, BufferedImage img2, File fileMask, File fileOut) throws IOException {
        /*Method for the regular fuzzy comparison*/
       
    boolean fuzzyEqual = true;
//        checks if another method fits the given parameters and calls it
        if ((pixelPerBlockX == 1) && (pixelPerBlockY == 1)) {
            if (threshold == 0.00) {
                return exactlyEqual(img1, img2, fileOut);
            }
            else {
                return pixelFuzzyEqual(img1, img2, fileOut);
            }
        }
//        adapts size of image 2 if necessary
        if ((img1.getWidth() != img2.getWidth()) || (img1.getHeight() != img2.getHeight())) {
            img2 = adaptImageSize(img1,img2);
        }

        imgOut = img2;
       
        int subImageHeight;
        int subImageWidth;
       
        int imagewidth = img1.getWidth();
        int imageheight = img1.getHeight();

//        calculates number of blocks in the screenshot
        int blocksx = (int) Math.ceil((float) imagewidth
                / (float) pixelPerBlockX);
        int blocksy = (int) Math.ceil((float) imageheight
                / (float) pixelPerBlockY);
       
//        Initializes maskImage
        BufferedImage maskImage = initializeMaskImage(img1, fileMask);
       
        for (int y = 0; y < blocksy; y++) {
            for (int x = 0; x < blocksx; x++) {
//                calculates width and height of the next block in case the remaining distance to the edges
//                is smaller than pixelPerBlockX or pixelPerBlockY
                subImageWidth=calcPixSpan(pixelPerBlockX,x,imagewidth);
                subImageHeight=calcPixSpan(pixelPerBlockY,y,imageheight);

//                create two subimages for the current block
                BufferedImage sub1 = img1.getSubimage(x * pixelPerBlockX, y * pixelPerBlockY,
                                    subImageWidth, subImageHeight);
                BufferedImage sub2 =  img2.getSubimage(x * pixelPerBlockX, y * pixelPerBlockY,
                        subImageWidth, subImageHeight);
                //Creates a subImage for the mask Image
                BufferedImage subMaskImage = maskImage.getSubimage(x * pixelPerBlockX, y * pixelPerBlockY,
                        subImageWidth, subImageHeight);
               
//                calculate average RGB-Values for the subimages
                double[] avgRgb1 = calculateAverageRgb(sub1);
                double[] avgRgb2 = calculateAverageRgb(sub2);
                double[] avgRgbSubMaskImage = calculateAverageRgb(subMaskImage);
               
//             initialize the RGB values for Black, for comparison with the MaskImage using getRgbDifference
                double[] avgRgbBlack = {Color.BLACK.getRed(), Color.BLACK.getGreen(), Color.BLACK.getBlue()};

//              if the difference between the subImages is above the threshold
                if  (getRgbDifference(avgRgb1, avgRgb2) > threshold) {
                   
//                    and if the difference between the maskImage and Black is above the threshold               
                    if (getRgbDifference(avgRgbSubMaskImage, avgRgbBlack) > threshold) {       
                       
//                        mark the current block. Set fuzzyEqual false ONLY IF trainingMode is false
                    	drawBorders(imgOut, subImageWidth, subImageHeight, x, y);
                       
//						If trainingMode is on, all marked areas will be set black in the maskImage
//						The markedImage will still be saved
                        if (trainingMode) {
                                Graphics gMask  = maskImage.getGraphics();
                                gMask.setColor(Color.BLACK);
                                gMask.fillRect(x * pixelPerBlockX, y * pixelPerBlockY,
                                subImageWidth, subImageHeight);
                                
                        }             //training Mode
                        else {
                        	fuzzyEqual = false;
                        }
                    }    // if the maskImage not black
                   
                }        // if the difference between the Images is above the treshold
            }
        }
        ImageIO.write(maskImage, "PNG", fileMask);                       
        if (!fuzzyEqual) {
                saveImage(imgOut,fileOut);
        }
       
        return fuzzyEqual;
    }

    private int calcPixSpan(int pixelPerBlock, int n, int overallSpan) {
        if (pixelPerBlock * (n + 1) > overallSpan)
            return overallSpan % pixelPerBlock;
        else
            return pixelPerBlock;
    }
   
    private BufferedImage adaptImageSize(BufferedImage img1, BufferedImage img2) throws IOException {
//        Method for the scaling of the new screenshot in case its size differs from the reference
        int scalePixelWidth;
        int scalePixelHeight;       

        if(((float)img2.getWidth()/(float)img1.getWidth()) < ((float)img2.getHeight()/(float)img1.getHeight())){
            scalePixelWidth = img1.getWidth();
            scalePixelHeight = (int) (img2.getHeight() * Math.ceil((float)img1.getWidth()/(float)img2.getWidth()));
        }else {
            scalePixelHeight = img1.getHeight();
            scalePixelWidth = (int) (img2.getWidth() * Math.ceil((float)img1.getHeight()/(float)img2.getHeight()));
        }
        return Thumbnails.of(img2).size(scalePixelWidth, scalePixelHeight).asBufferedImage();
    }
   
    private double calculatePixelRgbDiff(int x, int y, BufferedImage img1, BufferedImage img2) {
//        Method calculates the RGB difference of two pixels in comparison to the
//        maximum possible difference
        double maxDifference = 3 * 255;
        Color color1 = new Color(img1.getRGB(x, y));
        Color color2 = new Color(img2.getRGB(x, y));
        double difference = Math.abs(color1.getBlue() - color2.getBlue())
                            + Math.abs(color1.getRed() - color2.getRed())
                            + Math.abs(color1.getGreen() - color2.getGreen());
       
        return difference/maxDifference;
    }
   
    private double[] calculateAverageRgb (BufferedImage img) {
//        Method calculates average Red, Green and Blue values of a picture and returns them as array
        double[] averageRgb = {0, 0, 0};
        int imageHeight = img.getHeight();
        int imageWidth = img.getWidth();
       
//        sum the respective values of each pixel and divide it by the number of pixels
        for (int y = 0; y<imageHeight; y++) {
            for (int x = 0; x<imageWidth; x++) {
                Color color = new Color (img.getRGB(x, y));
                averageRgb[0] = averageRgb[0] + (double) color.getRed();
                averageRgb[1] = averageRgb[1] + (double) color.getGreen();
                averageRgb[2] = averageRgb[2] + (double) color.getBlue();
            }
        }
       
        double pixels = imageWidth * imageHeight;
        averageRgb[0] = averageRgb[0] / pixels;
        averageRgb[1] = averageRgb[1] / pixels;
        averageRgb[2] = averageRgb[2] / pixels;
       
       
        return averageRgb;
    }
   
    private double getRgbDifference(double [] Rgb1, double [] Rgb2) {
//        Method calculates the difference between to pictures with their given average Red, Green and Blue
//        values
        double maxDiff = 3 * 255;
        double diff = Math.abs(Rgb1[0] - Rgb2[0]) +
                    Math.abs(Rgb1[1] - Rgb2[1]) +
                    Math.abs(Rgb1[2] - Rgb2[2]);
       
        return diff/maxDiff;
    }
    
    private Color getComplementary (Color currentColor) {
    	int red = currentColor.getRed();
    	int green = currentColor.getGreen();
    	int blue = currentColor.getBlue();
    	int biggest = Math.max(red, green);
    	biggest = Math.max(biggest, blue);
    	Color newColor = Color.WHITE;
    	
    	if (biggest == red) {
    		newColor = Color.GREEN;
    	}
    	if (biggest == blue) {
    		newColor = Color.RED;
    	}
    	if ((biggest - green) < 30) {
    		newColor = Color.RED;
    	}
    	
    	return newColor;
    	
    }

    private void drawBorders (BufferedImage img, int subImageWidth, int subImageHeight, int currentX, int currentY) {
    	for (int a = 0; a < subImageWidth; a++) {
    		int rgb = img.getRGB((currentX * pixelPerBlockX) + a, currentY * pixelPerBlockY);
    		Color currentColor = new Color (rgb);
    		Color newColor = getComplementary(currentColor);
    		int newRgb = newColor.getRGB();
    		img.setRGB((currentX * pixelPerBlockX) + a, currentY * pixelPerBlockY, newRgb);
    		
    		rgb = img.getRGB((currentX * pixelPerBlockX) + a, (currentY * pixelPerBlockY) + subImageHeight - 1);
    		currentColor = new Color (rgb);
    		newColor = getComplementary(currentColor);
    		newRgb = newColor.getRGB();
    		img.setRGB((currentX * pixelPerBlockX) + a, (currentY * pixelPerBlockY) + subImageHeight - 1, newRgb);
    	}
    	
    	for (int b = 1; b < subImageHeight - 1; b++) {
    		int rgb = img.getRGB(currentX * pixelPerBlockX, (currentY * pixelPerBlockY) + b);
    		Color currentColor = new Color (rgb);
    		Color newColor = getComplementary(currentColor);
    		int newRgb = newColor.getRGB();
    		img.setRGB(currentX * pixelPerBlockX, (currentY * pixelPerBlockY) + b, newRgb);
    		
    		rgb = img.getRGB((currentX * pixelPerBlockX) + subImageWidth - 1, (currentY * pixelPerBlockY) + b);
    		currentColor = new Color (rgb);
    		newColor = getComplementary(currentColor);
    		newRgb = newColor.getRGB();
    		img.setRGB((currentX * pixelPerBlockX) + subImageWidth - 1, (currentY * pixelPerBlockY) + b, newRgb);
    	}
    }

   
    private BufferedImage imageToBufferedImage(Image img) {
        BufferedImage bi = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, null, null);
        return bi;
    }

    private void saveImage(BufferedImage img, File file) {
        try {
            ImageIO.write(img,"png", file);
        }
        catch (IOException io) {
        }
    }
   
    private BufferedImage initializeMaskImage(BufferedImage img, File file) throws IOException {
    	//read and return mask Image if it already exists and has the same size 
    	if (file.exists()) {
            BufferedImage mask = ImageIO.read(file); 
            if ( (mask.getWidth() == img.getWidth()) && (mask.getHeight() == img.getHeight()) ) {
            	return mask;
            }
        }
    	
    	int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = mask.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        return mask;
    }
}


