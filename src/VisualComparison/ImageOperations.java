package VisualComparison;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Provides methods to perform operations on images. 
 * Methods are shrinking, scaling, resizing, erosion, dilation and closing.
 * <p>
 * For erosion and dilation, the background/ foreground colors can be set
 * in the constructor with their parameters. The constructor without parameters
 * initializes these defaults:
 * <p>
 * Foreground color: Black from the import java.awt.Color class, Color.BLACK
 * Background color: Transparent white with rgb values of 255, 255, 255 and alpha = 0.                                                                         
 * 
 * @author damian
 * 
 */
public class ImageOperations {
	
	int rgbForegroundColor;
	int rgbBackgroundColor;

	/**
	 * Sets the background/ foreground colors for erosion/ dilation
	 * and derivative methods. 
	 * <p>
	 * Background color: Transparent white. <br>
	 * Foreground color: Black.
	 */
	protected ImageOperations() {
		this.rgbBackgroundColor = new Color(255, 255, 255, 0).getRGB();
		this.rgbForegroundColor = Color.BLACK.getRGB();
	}
	
	/**
	 * Manually sets the background and foreground colors
	 * for the erosion and dilation methods and derivative methods.
	 * 
	 * @param rgbBackgroundColor the rgb value of the background color
	 * @param rgbForegroundColor the rgb value of the foreground color
	 */
	protected ImageOperations(int rgbBackgroundColor, int rgbForegroundColor) {
		this.rgbBackgroundColor = rgbBackgroundColor;
		this.rgbForegroundColor = rgbForegroundColor;
	}
	
	/**
	 * Shrinks the given image by the given factor
	 * 
	 * @param img
	 *            the image to shrink
	 * @param compressionFactor
	 *            by this factor
	 * @return the shrinked image
	 */
	private BufferedImage shrinkImage(BufferedImage img, int compressionFactor) {
		int newWidth = img.getWidth() / compressionFactor;
		int newHeight = img.getHeight() / compressionFactor;
		BufferedImage newImg = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = newImg.getGraphics();
		g.drawImage(img, 0, 0, newImg.getWidth(), newImg.getHeight(), null);
		g.dispose();
		return newImg;
	}

	/**
	 * Scales an image up (or down) to the given size. Does not innately
	 * preserve Width/ Height ratio.
	 * 
	 * @param img
	 * @param newWidth
	 * @param newHeight
	 * @return the scaled image
	 */
	private BufferedImage scaleImage(BufferedImage img, int newWidth,
			int newHeight) {
		BufferedImage newImg = new BufferedImage(newWidth, newHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = newImg.getGraphics();
		g.drawImage(img, 0, 0, newImg.getWidth(), newImg.getHeight(), null);
		g.dispose();
		return newImg;
	}

	/**
	 * Increases an images width and height, the old image will be in the top
	 * left corner of the new image; the rest will be transparent black
	 * 
	 * @param img
	 * @param width
	 * @param height
	 * @return
	 */
	private BufferedImage increaseImageSize(BufferedImage img, int width,
			int height) {
		BufferedImage newImg = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] newImgArray = ((DataBufferInt) newImg.getRaster().getDataBuffer())
				.getData();
		int index;
		for (int w = img.getWidth(); w <= width; w++) {
			for (int h = img.getHeight(); h <= height; h++) {
				index = (h - 1) * newImg.getWidth() + w - 1;
				newImgArray[index] = 0;
			}
		}
		Graphics g = newImg.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return newImg;
	}
	
	/**
	 * Creates and returns an erosion image, using the alorithm from
	 * morphological image processing.
	 * <p>
	 * Assumes the structuring element is
	 * filled with 1 and thereby only needs it's width and height.
	 * The origin is placed in the middle of the structuring element.
	 * If width and/ or height are even, they are incremented to make sure 
	 * there is a middle pixel.
	 * 
	 * 
	 * @param img
	 * @param structElementWidth
	 * @param structElementHeight
	 * @return
	 */
	private BufferedImage erodeImage(BufferedImage img,
			int structElementWidth, int structElementHeight) {

		BufferedImage erosionedImage = new BufferedImage(img.getWidth(),
				img.getHeight(), BufferedImage.TYPE_INT_ARGB);

		boolean fits;

		// The origin of the structuring element will be it's middle pixel
		// Therefore make sure there is a middle pixel, ie make width and height
		// uneven.
		if ((structElementWidth % 2) == 0) {
			structElementWidth++;
		}
		if ((structElementHeight % 2) == 0) {
			structElementHeight++;
		}

		// Metaphorically places the structure element
		// In every possible position
		for (int w = 0; w < img.getWidth(); w++) {
			for (int h = 0; h < img.getHeight(); h++) {

				fits = true;

				// The origin of the structuring element is it's middle pixel
				for (int x = w - (structElementWidth % 2); x <= w
						+ (structElementWidth % 2); x++) {
					for (int y = h - (structElementWidth % 2); y <= h
							+ (structElementHeight % 2); y++) {

						// As long as the pixels not over the border
						if (x >= 0 && x < img.getWidth() && y >= 0
								&& y < img.getHeight()) {

							// Assumes all the pixels in the structureImage are
							// 1. If the pixel does not have the right color
							// black, set fits false, set the pixel in the erosionImage
							// to the false color and break the loop
							if (img.getRGB(x, y) != rgbForegroundColor) {
								fits = false;
								erosionedImage.setRGB(w, h, rgbBackgroundColor);
							}
						}
					}
				}

				// After every pixel was checked and if fits is true
				// Set the pixel in the erosionImage black
				// Some room for performance increase with a better break?
				if (fits) {
					erosionedImage.setRGB(w, h, rgbForegroundColor);
				}
			}
		}
		return erosionedImage;
	}
	
	/**
	 * Creates and returns a dilation image using the algorithm for
	 * morphological image processing.
	 * <p>
	 * Assumes the structuring element is filled with 1 and thereby 
	 * only needs it's width and height. The origin is placed in the middle 
	 * of the structuring element. If width and/ or height are even, 
	 * they are incremented to make sure there is a middle pixel.
	 * 
	 * @param img
	 * @param structElementWidth
	 * @param structElementHeight
	 * @return
	 */
	private BufferedImage dilateImage(BufferedImage img,
			int structElementWidth, int structElementHeight) {

		BufferedImage dilationImage = new BufferedImage(img.getWidth(),
				img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		boolean hits;

		// The origin of the structuring element will be it's middle pixel
		// Therefore make sure there is a middle pixel, ie make width and height
		// uneven
		if ((structElementWidth % 2) == 0) {
			structElementWidth++;
		}
		if ((structElementHeight % 2) == 0) {
			structElementHeight++;
		}

		// Metaphorically places the structure element
		// In every possible position
		for (int w = 0; w < img.getWidth(); w++) {
			for (int h = 0; h < img.getHeight(); h++) {

				hits = false;

				// Check every pixel of the structured element
				// against the pixel it metaphorically overlaps
				// There might be some room for performance increase here
				// The origin of the structuring element is it's middle pixel

				for (int x = w - (structElementWidth % 2); x <= w
						+ (structElementWidth % 2); x++) {
					for (int y = h - (structElementWidth % 2); y <= h
							+ (structElementHeight % 2); y++) {

						// As long as the pixels don't go over the border
						if (x >= 0 && x < img.getWidth() && y >= 0
								&& y < img.getHeight()) {

							// Assumes all the pixels in the structureImage are
							// 1. If the pixel is black, set hits true, set the
							// pixel in the dilationImage to the positive color and break the loop
							if (img.getRGB(x, y) == rgbForegroundColor) {
								hits = true;
								dilationImage.setRGB(w, h, rgbForegroundColor);
							}
						}
					}
				}

				// After every pixel was checked and if fits is true
				// Set the pixel in the dilationImage to he negative color
				if (!hits) {
					dilationImage.setRGB(w, h, rgbBackgroundColor);
				}
			}
		}
		return dilationImage;
	}

	/**
	 * Closes an image using the dilation and erosion methods in this class
	 * 
	 * @param img
	 * @param structElementWidth
	 * @param structElementHeight
	 * @return
	 */
	private BufferedImage closeImage(BufferedImage img, int structElementWidth,
			int structElementHeight) {
		img = dilateImage(img, structElementWidth, structElementHeight);
		img = erodeImage(img, structElementWidth, structElementHeight);
		return img;
	}
}
