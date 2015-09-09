package VisualComparison;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * Class for comparison of images, in particular screenshots of websites.
 * 
 * @author lucas & damian
 */
public class ImageComparison {
	private BufferedImage imgOut = null;
	private BufferedImage maskImage = null;
	private int pixelPerBlockXY, imageWidth, imageHeight, markingX, markingY,
			subImageWidth, subImageHeight;
	private double threshold;
	private boolean trainingMode;

	protected enum ComparisonAlgorithm {
		EXACTLYEQUAL, PIXELFUZZYEQUAL, FUZZYEQUAL
	}

	private final ComparisonAlgorithm algorithm;

	/**
	 * The parameters pixelPerBlockXY, pixelPerBlockXY and threshold define the
	 * fuzzyness in the comparison, higher parameters mean a comparison that is
	 * less strict.
	 * <p>
	 * The algorithm divides the image in blocks, the parameters
	 * pixelPerBlockXY, pixelPerBlockXY define the size of the block. It
	 * compares the average color in the blocks. The threshold parameter decides
	 * how big a difference in color will remain unnoticed. For example,
	 * threshold = 0.2 means a 20% difference will be tolerated.
	 * <p>
	 * In case images should only be equal in certain areas or certain areas
	 * should not be compared, a mask image will be created. Black areas in the
	 * mask image will be ignored. Users can manually paint it black or use the
	 * trainingMode to do so.
	 * <p>
	 * If the trainingMode parameter is true, differences will not be marked,
	 * but the corresponding areas in the mask image will be painted black.
	 * Barring manual intervention, differences in these areas will not be
	 * detected in later runs.
	 * <p>
	 * 
	 * @param pixelPerBlockXY
	 * @param pixelPerBlockXY
	 * @param threshold
	 * @param trainingMod
	 * @param comparisonAlgorithm
	 */
	public ImageComparison(int pixelPerBlockXY, double threshold,
			boolean trainingMode, String algorithm) {
		this.pixelPerBlockXY = pixelPerBlockXY;
		this.threshold = threshold;
		this.trainingMode = trainingMode;
		markingX = 8;
		markingY = 8;

		try {
			this.algorithm = ComparisonAlgorithm.valueOf(algorithm);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Specified algorithm not found");
		}

	}

	/**
	 * Wrapper method for the different comparison methods. Handles resizing,
	 * mask initialization and mask overlay. Calls the comparison method And
	 * saves the markedImage. Also marks the resized parts.
	 * 
	 * Does not modify the maskImage, that happens in the actual comparison 
	 * methods. It does however save the maskImage.
	 * 
	 * @param img1
	 * @param img2
	 * @param maskImage
	 * @param fileOut
	 * @return false if there were changes, true otherwise
	 * @throws IOException
	 */
	public boolean isEqual(BufferedImage img1, BufferedImage img2,
			File fileMask, File fileOut) throws IOException {

		// Initializes ImageOperations object to access it's methods later
		ImageOperations imageoperations = new ImageOperations();

		// Checks if one image is smaller then the other and if yes which.
		// Increases width/ height so the images will have the same.
		// The original Images will be in the top left corner
		// Remembers the previous width and height so it can be marked later
		int prevWidth = img1.getWidth();
		int prevHeight = img1.getHeight();

		// Increases the smaller images width to match the larger one
		while (img1.getWidth() != img2.getWidth()) {
			if (img1.getWidth() > img2.getWidth()) {
				prevWidth = img2.getWidth();
				imageoperations.increaseImageSize(img2, img1.getWidth(),
						img2.getHeight());
			}
			if (img2.getWidth() > img1.getWidth()) {
				imageoperations.increaseImageSize(img1, img2.getWidth(),
						img1.getHeight());
			}
		}

		// Increases the smaller images height to match the larger one
		while (img1.getHeight() != img2.getHeight()) {
			if (img1.getHeight() > img2.getHeight()) {
				prevHeight = img2.getHeight();
				img2 = imageoperations.increaseImageSize(img2, img2.getWidth(),
						img1.getHeight());
			}
			if (img2.getHeight() > img1.getHeight()) {
				img1 = imageoperations.increaseImageSize(img1, img1.getWidth(),
						img2.getHeight());
			}
		}

		imageWidth = img2.getWidth();
		imageHeight = img2.getHeight();
		imgOut = imageoperations.copyImage(img2);

		// initializes maskImage and masks both images:
		maskImage = initializeMaskImage(img1, fileMask);
		img1 = overlayMaskImage(img1, maskImage);
		imgOut = overlayMaskImage(imgOut, maskImage);

		BufferedImage markedImage = null;
		int[][] differentPixels = null;

		// Checks which imagecomparison method to call and calls it.
		// Sets the differentPixels array
		switch (algorithm) {
		case EXACTLYEQUAL:
			differentPixels = exactlyEqual(img1, imgOut);
			break;
		case PIXELFUZZYEQUAL:
			differentPixels = pixelFuzzyEqual(img1, imgOut);
			break;
		case FUZZYEQUAL:							
			differentPixels = fuzzyEqual(img1, imgOut);
			break;
		}
		
		if (differentPixels != null) {
			if (trainingMode) {
				
				//Save maskedImage
				ImageIO.write(maskImage, "PNG", fileMask);
				return true;
			}
			else {
				
				//Mark the differences
				markDifferences(differentPixels);
				markedImage = imgOut;
				
				// Mark the previously not existent areas, save the image and return false
				markedImage = markImageBorders(markedImage, prevWidth, prevHeight);
				ImageIO.write(markedImage, "PNG", fileOut);
				
				return false;
			}
		}
		else {
			return true;
		}
	}

	/**
	 * Method for the pixel based comparison with a threshold. So it will
	 * compare pixel by pixel, but it will have a certain tolerance.
	 * <p>
	 * If the images do not have the same size, the difference in size will be
	 * filled up and marked. Differences will be marked red unless the image
	 * itself is red. In that case, they will be marked green.
	 * 
	 * @param img1
	 *            the reference image
	 * @param img2
	 *            the image that will be compared with the reference image
	 * @param fileMask
	 *            the file where the mask image is or where it should be
	 * @param fileOut
	 *            the file where, if there are difference, the img2 with the
	 *            differences marked will be saved
	 * @return the marked image if there are differences, null otherwise
	 */
	private int[][] pixelFuzzyEqual(BufferedImage img1, BufferedImage img2) throws IOException {

		boolean equal = true;
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<Integer> yCoords = new ArrayList<Integer>();

		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
		for (int x = 0; x < imagewidth; x++) {
			for (int y = 0; y < imageheight; y++) {
				int xBlock = x / markingX;											
				int yBlock = y / markingY;
 				subImageWidth = calcPixSpan(markingX, xBlock, imageWidth);			
				subImageHeight = calcPixSpan(markingY, yBlock, imageHeight);		

				// calculates difference and marks them red if above threshold
				if ((calculatePixelRgbDiff(x, y, img1, img2) > threshold)) {

					// If trainingMode is on, the pixel will be set black in
					// the maskImage. The markedImage will not be saved here.
					if (trainingMode) {
						colorArea(maskImage, xBlock, yBlock);						//TODO
					}

					else {
						xCoords.add(x);
						yCoords.add(y);
						equal = false;
					}
				}
			}
		}

		int s = xCoords.size();
		int[] xArray = new int[s];
		for (int i = 0; i < s; i++) {
			xArray[i] = xCoords.get(i).intValue();
		}

		s = yCoords.size();
		int[] yArray = new int[s];
		for (int i = 0; i < s; i++) {
			yArray[i] = yCoords.get(i).intValue();
		}

		int[][] pixels = new int[xArray.length][2];
		for (int i = 0; i < xArray.length; i++) {
			pixels[i][0] = xArray[i];
			pixels[i][1] = yArray[i];
		}
		if (!equal) {
			return pixels;
		} else {
			return null;
		}
	}

	/**
	 * Method for the pixel based comparison exact comparison. Zero tolerance.
	 * <p>
	 * If the images do not have the same size, the difference in size will be
	 * filled up and marked. Differences will be marked red unless the image
	 * itself is red. In that case, they will be marked green.
	 * 
	 * @param img1
	 *            the reference image
	 * @param img2
	 *            the image that will be compared with the reference image
	 * @param fileMask
	 *            the file where the mask image is or where it should be
	 * @param fileOut
	 *            the file where, if there are difference, the img2 with the
	 *            differences marked will be saved
	 * @return the marked image if there are differences, null otherwise
	 */
	private int[][] exactlyEqual(BufferedImage img1, BufferedImage img2) throws IOException {
		/* Method for the exact comparison of two images */
		// img1: reference Image, img2: screenshot

		boolean exactlyEqual = true;
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<Integer> yCoords = new ArrayList<Integer>();

		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
		for (int x = 0; x < imagewidth; x++) {
			for (int y = 0; y < imageheight; y++) {

				int xBlock = x / markingX;
				int yBlock = y / markingY;
				subImageWidth = calcPixSpan(markingX, xBlock, imageWidth);
				subImageHeight = calcPixSpan(markingY, yBlock, imageHeight);

				// if the RGB values of 2 pixels differ
				// add the x- and y- coordinates to the corresponding ArrayLists
				if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
						xCoords.add(x);
						yCoords.add(y);
						exactlyEqual = false;
				}
			}
		}
		int s = xCoords.size();
		int[] xArray = new int[s];
		for (int i = 0; i < s; i++) {
			xArray[i] = xCoords.get(i).intValue();
		}

		s = yCoords.size();
		int[] yArray = new int[s];
		for (int i = 0; i < s; i++) {
			yArray[i] = yCoords.get(i).intValue();
		}

		int[][] pixels = new int[xArray.length][2];
		for (int i = 0; i < xArray.length; i++) {
			pixels[i][0] = xArray[i];
			pixels[i][1] = yArray[i];
		}

		if (!exactlyEqual) {
			return pixels;
		} else {
			return null;
		}
	}

	/**
	 * Compares the img2 image to the img1 image using the parameters defined in
	 * the ImageComparison constructor.
	 * 
	 * Differences in size are marked in isEqual
	 * 
	 * @param img1
	 *            the reference image
	 * @param img2
	 *            the image that will be compared with the reference image
	 * @param fileMask
	 *            the file where the mask image is or where it should be
	 * @param fileOut
	 *            the file where, if there are difference, the img2 with the
	 *            differences marked will be saved
	 * @return the marked image if there are differences, null otherwise
	 * 
	 * @throws IOException
	 */
	private int[][] fuzzyEqual(BufferedImage img1, BufferedImage img2) throws IOException {
		
		/* Method for the regular fuzzy comparison */

		boolean fuzzyEqual = true;
		
		//Save the width and height before shrinking the images
		int prevWidth = img1.getWidth();
		int prevHeight = img1.getHeight();
		
		//Shrink the image to create pseudo-blocks
		ImageOperations imageoperations = new ImageOperations(pixelPerBlockXY, 0.2);
		BufferedImage shrunkImg1 = imageoperations.shrinkImage(img1);
		BufferedImage shrunkImg2 = imageoperations.shrinkImage(img2);
		maskImage = imageoperations.shrinkImage(maskImage);
		
		int[][] pixels = pixelFuzzyEqual(shrunkImg1, shrunkImg2);
		
		//Scale back the maskImage and img2
		maskImage = imageoperations.scaleImage(maskImage, prevWidth, prevHeight);
		imgOut = imageoperations.scaleImage(shrunkImg2, prevWidth, prevHeight);

		//Scale back the array 
		for (int x = 0; x < pixels.length; x++) {
			for (int y = 0; x < pixels.length; y++) {
				pixels[x][y] = pixels[x][y] * pixelPerBlockXY;
			}
		}
		
		if (!fuzzyEqual) {
			return pixels;
		}
		else {
			return null;
		}
	}

	/**
	 * Calculates how many pixel there can be in the current block. Necessary in
	 * case the block would go over the border.
	 * 
	 * @param pixelPerBlock
	 * @param n
	 * @param overallSpan
	 * @return
	 */
	private int calcPixSpan(int pixelPerBlock, int n, int overallSpan) {
		if (pixelPerBlock * (n + 1) > overallSpan)
			return overallSpan % pixelPerBlock;							//TODO
		else
			return pixelPerBlock;
	}

	/**
	 * The method weights the red, green and blue values and 
	 * determines the difference as humans would see it.
	 * 
	 * Based on a comparison algorithm from http://www.compuphase.com/cmetric.htm . The
	 * algorithm is based on experiments with people, not theoretics. It is
	 * thereby not certain.
	 * 
	 * @param col1
	 * @param col2
	 * @return the difference between the colors as an int value. Higher ->
	 *         Bigger difference
	 */
	private double calculatePixelRgbDiff(int x, int y, BufferedImage img1, BufferedImage img2) {

		final double MAXDIFF = 721.2489168102785;

		int rgb1 = img1.getRGB(x, y);
		int rgb2 = img2.getRGB(x, y);
		
		// Initialize the red, green, blue values
		int r1 = (rgb1 >> 16) & 0xFF;
		int g1 = (rgb1 >> 8) & 0xFF;
		int b1 = rgb1 & 0xFF;
		int r2 = (rgb2 >> 16) & 0xFF;
		int g2 = (rgb2 >> 8) & 0xFF;
		int b2 = rgb2 & 0xFF;
		int rDiff = r1 - r2;
		int gDiff = g1 - g2;
		int bDiff = b1 - b2;

		// Initialize the weight parameters
		int rLevel = (r1 + r2) / 2;
		double rWeight = 2 + rLevel / 256;
		double gWeight = 4.0;
		double bWeight = 2 + ((255 - rLevel) / 256);

		double cDiff = Math.sqrt(rWeight * rDiff * rDiff + gWeight * gDiff
				* gDiff + bWeight * bDiff * bDiff);

		double cDiffInPercent = cDiff / MAXDIFF;

		return cDiffInPercent;
	}

	/**
	 * Returns red unless he currentColor is mainly red
	 * 
	 * @param currentColor
	 * @return
	 */
	private Color getComplementary(Color currentColor) {
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

	/**
	 * Method to mark areas around single changed pixels in ExactlyEqual and
	 * PixelFuzzyEqual
	 * 
	 * @param pixels
	 */
	private void markDifferences(int[][] pixels) {
		int lastX = -1;						
		int lastY = -1;
		for (int x = 0; x < pixels.length; x++) {
			int xBlock = pixels[x][0] / markingX;
			int yBlock = pixels[x][1] / markingY;
			if (xBlock != lastX || yBlock != lastY) {
				subImageWidth = calcPixSpan(markingX, xBlock, imageWidth);
				subImageHeight = calcPixSpan(markingY, yBlock, imageHeight);
				drawBorders(xBlock, yBlock, markingX, markingY);				//TODO
				lastX = xBlock;
				lastY = yBlock;
			}
		}
	}

	/**
	 * Colors the borders of a certain rectangle. Used to mark blocks. Uses the
	 * colorPixel method and subImageHeight/ subImageWidth
	 * 
	 * @param img
	 *            The image in which something will be marked
	 * @param subImageWidth
	 *            Vertical length of the rectangle to mark
	 * @param subImageHeight
	 *            Horizontal length of the rectangle to mark
	 * @param currentX
	 *            Starting position
	 * @param currentY
	 *            Starting position
	 */
	private void drawBorders(int currentX, int currentY, int width, int height) {
		int x, y;

		for (int a = 0; a < subImageWidth; a++) {
			x = currentX * width + a;
			y = currentY * height;
			colorPixel(x, y);

			y = currentY * height + subImageHeight - 1;
			colorPixel(x, y);
		}

		for (int b = 1; b < subImageHeight - 1; b++) {
			x = currentX * width;
			y = currentY * height + b;
			colorPixel(x, y);

			x = currentX * width + subImageWidth - 1;
			colorPixel(x, y);
		}
	}

	/**
	 * Colors a certain pixel using getComplementaryColor.
	 * 
	 * @param img
	 * @param x
	 * @param y
	 */
	private void colorPixel(int x, int y) {
		int rgb, newRgb;
		Color currentColor, newColor;

		rgb = imgOut.getRGB(x, y);
		currentColor = new Color(rgb);
		newColor = getComplementary(currentColor);
		newRgb = newColor.getRGB();
		imgOut.setRGB(x, y, newRgb);
	}

	private void colorArea(BufferedImage mask, int x, int y) {
		int rgb = Color.BLACK.getRGB();
		int xCorner = x * markingX;
		int yCorner = y * markingY;
		for (int a = 0; a < subImageWidth; a++) {
			for (int b = 0; b < subImageHeight; b++) {
				mask.setRGB(xCorner + a, yCorner + b, rgb);
			}
		}
	}

	/**
	 * Marks the bottom and left borders of an image red.
	 * Used in isEqual to mark the previously not existent parts of an image
	 * 
	 * @param img
	 *            the image to mark
	 * @param startW
	 *            the width from which to start marking
	 * @param startH
	 *            the height from which the marking starts
	 * @return the marked image
	 */
	private BufferedImage markImageBorders(BufferedImage img, int startW,
			int startH) {
		final int redRgb = Color.RED.getRGB();
		int[] imgArray = ((DataBufferInt) img.getRaster().getDataBuffer())
				.getData();
		for (int w = startW; w < img.getWidth(); w++) {
			for (int h = startH; h < img.getHeight(); h++) {
				imgArray[(h - 1) * w + w] = redRgb;
			}
		}
		return img;
	}
	
	/**
	 * Initializes the mask Image. If there already is a mask Image in the file,
	 * it will simply read that. If not, it will create a white Image of the
	 * same height and width as the picture given.
	 * 
	 * @param img
	 *            the picture corresponding to the maskedImage
	 * @param file
	 *            the file where the maskedImage is/ where it will be
	 * @return the maskedImage
	 * @throws IOException
	 */
	private BufferedImage initializeMaskImage(BufferedImage img, File file)
			throws IOException {
		final Color transparentWhite = new Color(255, 255, 255, 0);
		final int rgbTransparentWhite = transparentWhite.getRGB();

		if (file.exists()) {
			BufferedImage mask = ImageIO.read(file);
			if ((mask.getWidth() == img.getWidth())
					&& (mask.getHeight() == img.getHeight())) {

				// //Initialize an ImageOperations object and close the mask
				// image
				// ImageOperations imageoperations = new ImageOperations();
				// mask = imageoperations.closeImage(mask);

				return mask;
			}
		}

		int width = img.getWidth();
		int height = img.getHeight();
		BufferedImage mask = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		int[] maskArray = ((DataBufferInt) mask.getRaster().getDataBuffer())
				.getData();
		Arrays.fill(maskArray, rgbTransparentWhite);
		return mask;
	}

	/**
	 * Overlays one image over another. Intended for use with a mask image where
	 * every pixel is either transparent white or fully black. Should be used on
	 * both images before they are compared. That way differences will be
	 * ignored if the mask image is black in that area.
	 * 
	 * @param imageToMask
	 *            the image that should be masked, ie one of the images to
	 *            compare
	 * @param maskImage
	 *            the maskimage consisting of transparent, white pixels and
	 *            black pixels
	 * @return
	 */
	private BufferedImage overlayMaskImage(BufferedImage imageToMask,
			BufferedImage maskImage) {
		Graphics gImageToMask = imageToMask.getGraphics();
		gImageToMask.drawImage(maskImage, 0, 0, null);
		gImageToMask.dispose();
		return imageToMask;
	}
}
