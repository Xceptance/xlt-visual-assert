package VisualComparison;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Provides methods to perform operations on images. 
 * Methods are shrinking, scaling, resizing, erosion, dilation and closing.
 * <p>
 * For erosion and dilation, the background/ foreground colors can be manually set,
 * defaults are below. The structure element is always a square filled with ones.
 * It's width and height are relative to the image it is performing an erosion or
 * dilation on. The relative size can be set with the structElementScale attribute,
 * it's default value can be found below. 
 * <p>
 * For an example: If an image has a width and height of 1000 pixels and it is shrinked
 * with a compressionFactor of 10, the shrinked image will be 100 pixels wide and high.
 * With structElementScale = 0.1, the structure element to that image will be 10 pixels 
 * wide and high.
 * <p>
 * I bigger structElementScale means a bigger structElement which means the erosion, 
 * dilation and closing methods have more impact. So the erosion will erode more, 
 * the dilation will dilate more and the closing method will close wider gaps.
 * <p>
 * The compressionFactor, meaning the pixels per block in x and y direction 
 * can be set in the constructor.
 * <p>
 * The constructor without parameters initializes these defaults:
 * <p>
 * Foreground color: Black from the import java.awt.Color class, Color.BLACK
 * Background color: Transparent white with rgb values of 255, 255, 255 and alpha = 0  
 * CompressionFactor: 10                                                                       
 * structElementScale: 0.1
 * 
 * @author damian
 * 
 */
public class ImageOperations {
	
	int rgbForegroundColor;
	int rgbBackgroundColor;
	int compressionFactor;
	double structElementScale;
	
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
		this.compressionFactor = 10;
		this.structElementScale = 0.1;
	}
	
	/**
	 * Constructor for manually setting the compressionFactor/ the pixels per block 
	 * and the structure elements size in comparison to the image.
	 * (Parameter structElementScale)
	 * 
	 * @param compressionFactor
	 * @param structElementScale
	 */
	protected ImageOperations(int compressionFactor, int structElementScale) {
		this.rgbBackgroundColor = new Color(255, 255, 255, 0).getRGB();
		this.rgbForegroundColor = Color.BLACK.getRGB();
		this.compressionFactor = compressionFactor;
		this.structElementScale = structElementScale;
	}
	
	/**
	 * Manually sets the background and foreground colors
	 * for the erosion and dilation methods and derivative methods.
	 * 
	 * @param rgbBackgroundColor the rgb value of the background color
	 * @param rgbForegroundColor the rgb value of the foreground color
	 * @param compressionFactor
	 * @param structElementScale
	 */
	protected ImageOperations(int rgbBackgroundColor, int rgbForegroundColor, int compressionFactor, int structElementScale) {
		this.rgbBackgroundColor = rgbBackgroundColor;
		this.rgbForegroundColor = rgbForegroundColor;
		this.compressionFactor = compressionFactor;
		this.structElementScale = structElementScale;
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
	protected BufferedImage shrinkImage(BufferedImage img) {
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
	protected BufferedImage scaleImage(BufferedImage img, int newWidth,
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
	protected BufferedImage increaseImageSize(BufferedImage img, int width,
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
	 * Creates another image, which is a copy of the source image
	 * 
	 * @param source
	 *            the image to copy
	 * @return a copy of that image
	 */
	protected BufferedImage copyImage(BufferedImage source) {
		// Creates a fresh BufferedImage that has the same size and content of
		// the source image
		BufferedImage copy = new BufferedImage(source.getWidth(),
				source.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = copy.getGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return copy;
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
	protected BufferedImage erodeImage(BufferedImage img) {

		int structElementWidth = (int) (img.getWidth() * structElementScale) % 1;
		int structElementHeight = (int) (img.getHeight() * structElementScale) % 1;
		
		if (structElementWidth == 1 || structElementHeight == 1) {
			throw new IllegalArgumentException("The comressionFactor and structElementScale lead to " +
												"to small a structure element. Increase at least one of them.");
		}
		
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
	protected BufferedImage dilateImage(BufferedImage img) {
		
		int structElementWidth = (int) (img.getWidth() * structElementScale) % 1;
		int structElementHeight = (int) (img.getHeight() * structElementScale) % 1;

		if (structElementWidth == 1 || structElementHeight == 1) {
			throw new IllegalArgumentException("The comressionFactor and structElementScale lead to " +
												"to small a structure element. Increase at least one of them.");
		}
		
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
	protected BufferedImage closeImage(BufferedImage img) {
		img = dilateImage(img);
		img = erodeImage(img);
		return img;
	}
}
