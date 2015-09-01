package VisualComparison;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

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
   
      
    public boolean pixelFuzzyEqual (BufferedImage img1, BufferedImage img2, File fileMask, File fileOut) throws IOException {
        /*Method for pixel-based fuzzy comparison*/
       
        boolean equal = true;
       
        imgOut = img2;
//      Initializes maskImage
        BufferedImage maskImage = initializeMaskImage(img1, fileMask);

        int imagewidth = img1.getWidth();
        int imageheight = img1.getHeight();
        int markRgb;
        for (int x = 0; x<imagewidth; x++) {
            for(int y = 0;y<imageheight; y++) {
            	boolean isTransparent = isTransparent(img1.getRGB(x, y)) || isTransparent(img2.getRGB(x, y));

//                calculates difference and marks them red if above threshold or transparent...
                if ( (calculatePixelRgbDiff(x, y, img1, img2) > threshold) || isTransparent ) {
                	
//                  and if the maskImage is not Black ...                	
                	if ( maskImage.getRGB(x, y) != Color.BLACK.getRGB() ) {   
                		
//						If trainingMode is on, the pixel will be set black in the maskImage
//						The markedImage will not be saved
                        if (trainingMode) {
                        			maskImage.setRGB(x, y, Color.black.getRGB());
                        }            
                        
                        else {
                        			markRgb = getMarkRgb(img2, x, y);
                        			imgOut.setRGB(x, y, markRgb);                                   
                        			equal = false;
                        }
                	}  
                }
            }
        }
        ImageIO.write(maskImage, "PNG", fileMask);                       
        if (!equal) {
            ImageIO.write(imgOut, "PNG", fileOut);
        }
        return equal;
    }

    public boolean exactlyEqual (BufferedImage img1, BufferedImage img2, File fileMask, File fileOut) throws IOException {
        /*Method for the exact comparison of two images*/
        //img1: reference Image, img2: screenshot
        boolean exactlyEqual = true;  
       
        imgOut = img2;
//      Initializes maskImage
        BufferedImage maskImage = initializeMaskImage(img1, fileMask);
        
        int imagewidth = img1.getWidth();
        int imageheight = img1.getHeight();
        int markRgb;
        for (int x = 0; x<imagewidth; x++) {                           
            for(int y = 0;y<imageheight; y++) {
            	boolean isTransparent = isTransparent(img1.getRGB(x, y)) || isTransparent(img2.getRGB(x, y));
            	
//                if the RGB values of 2 pixels differ or one of them is transparent, print them red and set equal false ...
                    if ((img1.getRGB(x, y) != img2.getRGB(x, y)) || isTransparent){

//                      unless the maskImage is black                 	
                    	if ( maskImage.getRGB(x, y) != Color.BLACK.getRGB() ) { 
                    
//    						Or trainingMode is on. If trainingMode is on, the pixel will be set black in the maskImage
//    						The markedImage will not be saved
                            if (trainingMode) {
                            			maskImage.setRGB(x, y, Color.black.getRGB());
                            }            
                            
                            else {
                            			markRgb = getMarkRgb(img2, x, y);
                            			imgOut.setRGB(x, y, markRgb);                                   
                            			exactlyEqual = false;
                            }                
                    	}		
                    }
            }
        }
        ImageIO.write(maskImage, "PNG", fileMask);                       
        if (!exactlyEqual) {
                ImageIO.write(imgOut, "PNG", fileOut);
        }
        return exactlyEqual;
    }
   
    public boolean fuzzyEqual(BufferedImage img1, BufferedImage img2, File fileMask, File fileOut) throws IOException {
        /*Method for the regular fuzzy comparison*/
       
    boolean fuzzyEqual = true;
    
//  Checks if one image is smaller then the other and if yes which. Increases the images Width and Height until they are equal
//  The original Images will be in the top left corner
	while ( (img1.getWidth() != img2.getWidth()) || (img1.getHeight() != img2.getHeight()) ) {
		if ( (img1.getWidth() > img2.getWidth()) || (img1.getHeight() > img2.getHeight()) ) {
			img2 = adaptImageSize(img1, img2);
		}
		if ( (img1.getWidth() < img2.getWidth()) || (img1.getHeight() < img2.getHeight()) ) {
			img1 = adaptImageSize(img2, img1);
		}
	}
    
//        checks if another method fits the given parameters and calls it
        if ((pixelPerBlockX == 1) && (pixelPerBlockY == 1)) {
            if (threshold == 0.00) {
                return exactlyEqual(img1, img2, fileMask, fileOut);
            }
            else {
                return pixelFuzzyEqual(img1, img2, fileMask, fileOut);
            }
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
                double[] avgRgbBlack = {Color.BLACK.getRed(), Color.BLACK.getGreen(), Color.BLACK.getBlue(), Color.BLACK.getAlpha()};

//              if the difference between the subImages is above the threshold
                if  (getRgbDifference(avgRgb1, avgRgb2) > threshold) {
                   
//                    and if the difference between the maskImage and black is above the threshold  
//                	  it compares against the threshold because there might be a mix of black and white in a block
                    if (getRgbDifference(avgRgbSubMaskImage, avgRgbBlack) > threshold) {       
                       
//                        mark the current block. Set fuzzyEqual false ONLY IF trainingMode is false
                    	drawBorders(imgOut, subImageWidth, subImageHeight, x, y);
                       
//						If trainingMode is on, all marked areas will be set black in the maskImage
//						The markedImage will not be saved
                        if (trainingMode) {
                                Graphics gMask  = maskImage.getGraphics();
                                gMask.setColor(Color.BLACK);
                                gMask.fillRect(x * pixelPerBlockX, y * pixelPerBlockY,
                                subImageWidth, subImageHeight);
                                gMask.dispose();
                                
                        }             //training Mode
                        else {
                        	fuzzyEqual = false;
                        }
                    }    // if the maskImage not black
                   
                }        // if the difference between the Images is above the threshold
            }
        }
        ImageIO.write(maskImage, "PNG", fileMask);                       
        if (!fuzzyEqual) {
                ImageIO.write(imgOut, "PNG", fileOut);
        }
       
        return fuzzyEqual;
    }

    private int calcPixSpan(int pixelPerBlock, int n, int overallSpan) {
        if (pixelPerBlock * (n + 1) > overallSpan)
            return overallSpan % pixelPerBlock;
        else
            return pixelPerBlock;
    }
    
    private double calculatePixelRgbDiff(int x, int y, BufferedImage img1, BufferedImage img2) {
//        Method calculates the RGB difference of two pixels in comparison to the
//        maximum possible difference
//    	  If one of the pixels is transparent, presumably because of the resizeImage method, it will return the maxDifference
    	
    	Color color1 = new Color(img1.getRGB(x, y));
        Color color2 = new Color(img2.getRGB(x, y));
        
        int red1 = color1.getRed();
        int red2 = color2.getRed();
        int green1 = color1.getGreen();
        int green2 = color2.getGreen();
        int blue1 = color1.getBlue();
        int blue2 = color2.getBlue();
    	
        double maxDifference = Math.max(red1, 255 - red1)
        						+ Math.max(green1, 255 - green1)
        						+ Math.max(blue1, 255 - blue1);
              
        if (isTransparent(img1.getRGB(x, y))) {
        	return 1.00;
        }
        if (isTransparent(img2.getRGB(x, y))) {
        	return 1.00;
        }

        double difference = Math.abs(blue1 - blue2)
                            + Math.abs(red1 - red2)
                            + Math.abs(green1 - green2);
       
        return difference/maxDifference;
    }
   
    private double[] calculateAverageRgb (BufferedImage img) {
//        Method calculates average Red, Green, Blue and Alpha values of a picture and returns them as array
        double[] averageRgb = {0, 0, 0, 0};
        int imageHeight = img.getHeight();
        int imageWidth = img.getWidth();
       
//        sum the respective values of each pixel and divide it by the number of pixels
        for (int y = 0; y<imageHeight; y++) {
            for (int x = 0; x<imageWidth; x++) {
                Color color = new Color (img.getRGB(x, y), true);
                averageRgb[0] = averageRgb[0] + (double) color.getRed();
                averageRgb[1] = averageRgb[1] + (double) color.getGreen();
                averageRgb[2] = averageRgb[2] + (double) color.getBlue();
                averageRgb[3] = averageRgb[3] + (double) color.getAlpha();
            }
        }
       
        double pixels = imageWidth * imageHeight;
        averageRgb[0] = averageRgb[0] / pixels;
        averageRgb[1] = averageRgb[1] / pixels;
        averageRgb[2] = averageRgb[2] / pixels;
        averageRgb[3] = averageRgb[3] / pixels;
        return averageRgb;
    }
   
    private double getRgbDifference(double [] Rgb1, double [] Rgb2) {
//        Method calculates the difference between to pictures with their given average Red, Green, Blue and Alpha values
//    	  based on the maximum RGB difference
//		  Alpha values are more influential, because they indicate pixels that were not existent before
//    	  The way it is implemented now, a difference of 255 in alpha and 0 in red, green and black matches a threshold of 1
    	
        double maxDiff = Math.max(Rgb1[0], 255 - Rgb1[0]) +
        				Math.max(Rgb1[1], 255 - Rgb1[1]) +
        				Math.max(Rgb1[2], 255 - Rgb1[2]); 
        double diff = Math.abs(Rgb1[0] - Rgb2[0]) +
                    Math.abs(Rgb1[1] - Rgb2[1]) +
                    Math.abs(Rgb1[2] - Rgb2[2]) +
                    Math.abs( (Rgb1[3] - Rgb2[3])*3 );

       
        return diff/maxDiff;
    }

//    Returns the color with which the pixels will be marked, used in exactlyEqual and pixelFuzzyEqual
//    Checks the pixels to the right and to the bottom (the surrounding pixels that coudn't have been marked already)
    private int getMarkRgb (BufferedImage img, int x, int y) {
    	
    	int markRgb = Color.RED.getRGB();
    	int redPixels = 0;
    	if (isRedish(img, x+1, y)) {
    		redPixels++;
    		for (int i=x-1; i<=x+1; i++) {
    			if (isRedish(img, i, y)) {
    				redPixels++;
    			}
    		}	
    	}
    	
    	if (redPixels>2) {
    		markRgb = Color.GREEN.getRGB();
    	}
    	return markRgb;
    }
    
//    Checks if a certain pixels is redish or not. Used in getMarkRgb
    private boolean isRedish (BufferedImage img, int x, int y) {
    	
    	if ( (x<img.getWidth() && y<img.getHeight()) && (x>=0 && y>=0) ) {
    		int rgb = img.getRGB(x, y);
    		Color tempColor = new Color (rgb, true);
    		if ( tempColor.getRed() > 250 && tempColor.getGreen() < 140 && tempColor.getBlue() < 140) {
    			return true;
    		}	
    	}
    	return false;
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
    	int x, y;
    	
    	for (int a = 0; a < subImageWidth; a++) {
    		x = currentX * pixelPerBlockX + a;
    		y = currentY * pixelPerBlockY;
    		colorPixel(img, x, y);

    		y = currentY * pixelPerBlockY + subImageHeight - 1;
    	    colorPixel(img, x, y);
    	}
    	
    	for (int b = 1; b < subImageHeight - 1; b++) {
    		x = currentX * pixelPerBlockX;
    		y = currentY * pixelPerBlockY + b;
    		colorPixel(img, x, y);
    		
    		x = currentX * pixelPerBlockX + subImageWidth - 1;
    		colorPixel(img, x, y);
    	}
    }
    
    private void colorPixel(BufferedImage img, int x, int y) {
    	int rgb, newRgb;
    	Color currentColor, newColor;
    	
    	rgb = img.getRGB(x, y);
		currentColor = new Color (rgb);
		newColor = getComplementary(currentColor);
		newRgb = newColor.getRGB();
		img.setRGB(x, y, newRgb);
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
        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int [] maskArray = ((DataBufferInt) mask.getRaster().getDataBuffer()).getData();
        int rgbWhite = Color.WHITE.getRGB();
        Arrays.fill(maskArray, rgbWhite);
        return mask;
    }
    
 // Increases the size of the second Image to the size of the first Image
    private BufferedImage adaptImageSize(BufferedImage img1, BufferedImage imgToIncrease) {
    	int maxWidth = imgToIncrease.getWidth();
    	int maxHeight = imgToIncrease.getHeight();
		
		if (img1.getWidth() > maxWidth) {
			maxWidth = img1.getWidth();
			imgToIncrease = increaseImageSize(imgToIncrease, maxWidth, maxHeight);
		}
		
		if (img1.getHeight() > maxHeight) {
			maxHeight = img1.getHeight();
			imgToIncrease = increaseImageSize(imgToIncrease, maxWidth, maxHeight);
		}
		return imgToIncrease;
    }
    
//  Increases an images width and height, the new image will be in the top left corner; the rest will be transparent black 
	private BufferedImage increaseImageSize(BufferedImage img, int width, int height) {
  		BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
  		int[] newImgArray = ((DataBufferInt) newImg.getRaster().getDataBuffer()).getData();
  		int index;
  		for (int w=img.getWidth(); w<width; w++) {
  			for (int h=img.getHeight(); h<height; h++) {
  				index = h * newImg.getWidth() + w;
  				newImgArray[index] = 0;
  			}
  		}
  		Graphics g = newImg.createGraphics();
  		g.drawImage(img, 0, 0, null);
  		g.dispose();
  		return newImg;
  	}
  	
//	Checks if a certain pixel of a certain image has an alpha value of zero
  	private boolean isTransparent(int rgb) {
  		Color color = new Color(rgb, true);
  		if (color.getAlpha() == 0) {
  			return true;
  		}
  		return false;
  	}
}


