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
	private int pixelPerBlockX, pixelPerBlockY, imageWidth, imageHeight,
			markingX, markingY;
	private double threshold;
	private boolean trainingMode;

	protected enum ComparisonAlgorithm {
		EXACTLYEQUAL, PIXELFUZZYEQUAL, FUZZYEQUAL
	}

	private final ComparisonAlgorithm algorithm;

	/**
	 * The parameters pixelPerBlockX, pixelPerBlockY and threshold define the
	 * fuzzyness in the comparison, higher parameters mean a comparison that is
	 * less strict.
	 * <p>
	 * The algorithm divides the image in blocks, the parameters pixelPerBlockX,
	 * pixelPerBlockY define the size of the block. It compares the average
	 * color in the blocks. The threshold parameter decides how big a difference
	 * in color will remain unnoticed. For example, threshold = 0.2 means a 20%
	 * difference will be tolerated.
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
	 * @param pixelPerBlockX
	 * @param pixelPerBlockY
	 * @param threshold
	 * @param trainingMod
	 * @param comparisonAlgorithm
	 */
	public ImageComparison(int pixelPerBlockX, int pixelPerBlockY,
			double threshold, boolean trainingMode, String algorithm) {
		this.pixelPerBlockX = pixelPerBlockX;
		this.pixelPerBlockY = pixelPerBlockY;
		this.threshold = threshold;
		this.trainingMode = trainingMode;
		markingX = 10;
		markingY = 10;

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
	 * methods
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
				increaseImageSize(img2, img1.getWidth(), img2.getHeight());
			}
			if (img2.getWidth() > img1.getWidth()) {
				increaseImageSize(img1, img2.getWidth(), img1.getHeight());
			}
		}

		// Increases the smaller images height to match the larger one
		while (img1.getHeight() != img2.getHeight()) {
			if (img1.getHeight() > img2.getHeight()) {
				prevHeight = img2.getHeight();
				increaseImageSize(img2, img2.getWidth(), img1.getHeight());
			}
			if (img2.getHeight() > img1.getHeight()) {
				increaseImageSize(img1, img1.getWidth(), img2.getHeight());
			}
		}

		imageWidth = img2.getWidth();
		imageHeight = img2.getHeight();
		imgOut = copyImage(img2);
		// initializes maskImage and masks both images:
		BufferedImage maskImage = initializeMaskImage(img1, fileMask);
		img1 = overlayMaskImage(img1, maskImage);
		imgOut = overlayMaskImage(imgOut, maskImage);

		BufferedImage markedImage = null;

		// Checks which imagecomparison method to call and calls it.
		// Sets the markedImage = imagecomparison method with parameters
		switch (algorithm) {
		case EXACTLYEQUAL:
			if (exactlyEqual(img1, imgOut, maskImage, fileMask) != null) {
				markDifferences(exactlyEqual(img1, imgOut, maskImage, fileMask));
				markedImage = imgOut;
			}
			break;
		case PIXELFUZZYEQUAL:
			if (pixelFuzzyEqual(img1, imgOut, maskImage, fileMask) != null) {
				markDifferences(exactlyEqual(img1, imgOut, maskImage, fileMask));
				markedImage = imgOut;
			}
			break;
		case FUZZYEQUAL:
			markedImage = fuzzyEqual(img1, imgOut, maskImage, fileMask);
			break;
		}

		if (markedImage != null) {
			// Mark the previously not existent areas
			markedImage = markImageBorders(markedImage, prevWidth, prevHeight);
			ImageIO.write(markedImage, "PNG", fileOut);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Marks the bottom and left borders of an image red.
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
	private int[][] pixelFuzzyEqual(BufferedImage img1, BufferedImage img2,
			BufferedImage maskImage, File fileMask) throws IOException {

		boolean equal = true;
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<Integer> yCoords = new ArrayList<Integer>();

		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
		for (int x = 0; x < imagewidth; x++) {
			for (int y = 0; y < imageheight; y++) {

				// calculates difference and marks them red if above threshold
				if ((calculatePixelRgbDiff(x, y, img1, img2) > threshold)) {

					// unless the maskImage is black
					if (maskImage.getRGB(x, y) != Color.BLACK.getRGB()) {

						// If trainingMode is on, the pixel will be set black in
						// the maskImage
						// The markedImage will not be saved
						if (trainingMode) {
							int xBlock = x / markingX;
							int yBlock = y / markingY;
							colorArea(maskImage, xBlock, yBlock);
						}

						else {
							xCoords.add(x);
							yCoords.add(y);
							equal = false;
						}
					}
				}
			}
		}

		ImageIO.write(maskImage, "PNG", fileMask);

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
	private int[][] exactlyEqual(BufferedImage img1, BufferedImage img2,
			BufferedImage maskImage, File fileMask) throws IOException {
		/* Method for the exact comparison of two images */
		// img1: reference Image, img2: screenshot

		boolean exactlyEqual = true;
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<Integer> yCoords = new ArrayList<Integer>();

		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
		for (int x = 0; x < imagewidth; x++) {
			for (int y = 0; y < imageheight; y++) {

				// if the RGB values of 2 pixels differ or one of them is
				// print them red and set equal false ...
				if (img1.getRGB(x, y) != img2.getRGB(x, y)) {

					// unless the maskImage is black
					if (maskImage.getRGB(x, y) != Color.BLACK.getRGB()) {

						// Or trainingMode is on. If trainingMode is on, the
						// pixel will be set black in the maskImage
						// The markedImage will not be saved
						if (trainingMode) {
							int xBlock = x / markingX;
							int yBlock = y / markingY;
							colorArea(maskImage, xBlock, yBlock);
						}

						else {
							xCoords.add(x);
							yCoords.add(y);
							exactlyEqual = false;
						}
					}
				}
			}
		}

		ImageIO.write(maskImage, "PNG", fileMask);

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
	 * 
	 * @throws IOException
	 */
	private BufferedImage fuzzyEqual(BufferedImage img1, BufferedImage img2,
			BufferedImage maskImage, File fileMask) throws IOException {
		/* Method for the regular fuzzy comparison */

		boolean fuzzyEqual = true;

		int subImageHeight;
		int subImageWidth;

		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();

		// calculates number of blocks in the screenshot
		int blocksx = (int) Math.ceil((float) imagewidth
				/ (float) pixelPerBlockX);
		int blocksy = (int) Math.ceil((float) imageheight
				/ (float) pixelPerBlockY);

		for (int y = 0; y < blocksy; y++) {
			for (int x = 0; x < blocksx; x++) {
				// calculates width and height of the next block in case the
				// remaining distance to the edges
				// is smaller than pixelPerBlockX or pixelPerBlockY
				subImageWidth = calcPixSpan(pixelPerBlockX, x, imagewidth);
				subImageHeight = calcPixSpan(pixelPerBlockY, y, imageheight);

				// create two subimages for the current block
				BufferedImage sub1 = img1.getSubimage(x * pixelPerBlockX, y
						* pixelPerBlockY, subImageWidth, subImageHeight);
				BufferedImage sub2 = img2.getSubimage(x * pixelPerBlockX, y
						* pixelPerBlockY, subImageWidth, subImageHeight);
				// Creates a subImage for the mask Image
				BufferedImage subMaskImage = maskImage.getSubimage(x
						* pixelPerBlockX, y * pixelPerBlockY, subImageWidth,
						subImageHeight);

				// calculate average RGB-Values for the subimages
				double[] avgRgb1 = calculateAverageRgb(sub1);
				double[] avgRgb2 = calculateAverageRgb(sub2);
				double[] avgRgbSubMaskImage = calculateAverageRgb(subMaskImage);

				// initialize the RGB values for Black, for comparison with the
				// MaskImage using getRgbDifference
				double[] avgRgbBlack = { Color.BLACK.getRed(),
						Color.BLACK.getGreen(), Color.BLACK.getBlue(),
						Color.BLACK.getAlpha() };

				// if the difference between the subImages is above the
				// threshold
				if (getRgbDifference(avgRgb1, avgRgb2) > threshold) {
					int debug = 1;
					debug = debug + 1;
					// and if the difference between the maskImage and black is
					// above the threshold
					// it compares against the threshold because there might be
					// a mix of black and white in a block
					if (getRgbDifference(avgRgbSubMaskImage, avgRgbBlack) > threshold) {

						// mark the current block. Set fuzzyEqual false ONLY IF
						// trainingMode is false
						drawBorders(x, y, subImageWidth, subImageHeight);

						// If trainingMode is on, all marked areas will be set
						// black in the maskImage
						// The markedImage will not be saved
						if (trainingMode) {
							Graphics gMask = maskImage.getGraphics();
							gMask.setColor(Color.BLACK);
							gMask.fillRect(x * pixelPerBlockX, y
									* pixelPerBlockY, subImageWidth,
									subImageHeight);
							gMask.dispose();

						} // training Mode
						else {
							fuzzyEqual = false;
						}
					} // if the maskImage not black

				} // if the difference between the Images is above the threshold
			}
		}

		ImageIO.write(maskImage, "PNG", fileMask);

		if (!fuzzyEqual) {
			return imgOut;
		}

		else {
			return null;
		}
	}

	/**
	 * Returns an edge image of two images, where the edges should be in
	 * differing shades of white depending on their difference to their
	 * neighbors.
	 * 
	 * @param img
	 *            the image from which the edge image will be created
	 * @return the edge image
	 */
	public BufferedImage getEdgeImage(BufferedImage img) { // is only public for
															// testing
		BufferedImage edgeImage = new BufferedImage(img.getWidth(),
				img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final int rgbWhite = Color.WHITE.getRGB();
		final int rgbBlack = Color.BLACK.getRGB();
		for (int w = 0; w < edgeImage.getWidth(); w++) {
			for (int h = 0; h < edgeImage.getHeight(); h++) {
				// if the difference in color between a pixel and it's neighbors
				// it to high
				if (isEdgePixel(img, w, h)) {
					edgeImage.setRGB(w, h, rgbWhite);
				} else {
					edgeImage.setRGB(w, h, rgbBlack);
				}
			}
		}
		return edgeImage;
	}

	/**
	 * Compares a pixels color to its neighbors. If they are very different,
	 * return true.
	 * 
	 * @param img
	 *            the bufferedimage with the pixel
	 * @param x
	 *            the width
	 * @param y
	 *            the heigh, for identification of the pixel
	 * @return true if he pixel is an EdgePixel, false otherwise
	 */
	protected boolean isEdgePixel(BufferedImage img, int x, int y) {
		final double differenceAllowed = 300;
		boolean isEdgePixel = false;
		for (int w = x - 1; w <= x + 1; w++) {
			for (int h = y - 1; h <= y + 1; h++) {
				if (w >= 0 && w < img.getWidth()) {
					if (h >= 0 && h < img.getHeight()) {

						if (calculateColorDifference(img.getRGB(x, y),
								img.getRGB(w, h)) > differenceAllowed) {
							isEdgePixel = true;
						}
					}
				}
			}
		}
		return isEdgePixel;
	}

	/**
	 * Creates another image, which is a copy of the source image
	 * 
	 * @param source
	 *            the image to copy
	 * @return a copy of that image
	 */
	private BufferedImage copyImage(BufferedImage source) {
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
			return overallSpan % pixelPerBlock;
		else
			return pixelPerBlock;
	}

	/**
	 * The method determines the difference as humans would see it. Based on a
	 * comparison algorithm from http://www.compuphase.com/cmetric.htm The
	 * algorithm is based on experiments with people, not theoretics. It is
	 * thereby not certain.
	 * 
	 * @param col1
	 * @param col2
	 * @return the difference between the colors as a double value. Higher ->
	 *         Bigger difference
	 */
	private double calculateColorDifference(Color color1, Color color2) {
		// Initialize the red, green, blue values
		int r1 = color1.getRed();
		int g1 = color1.getGreen();
		int b1 = color1.getBlue();
		int r2 = color2.getRed();
		int g2 = color2.getGreen();
		int b2 = color2.getBlue();
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
		// Tentatively: Maxdifference = 721

		return cDiff;
	}

	/**
	 * The method determines the difference as humans would see it. Based on a
	 * comparison algorithm from http://www.compuphase.com/cmetric.htm The
	 * algorithm is based on experiments with people, not theoretics. It is
	 * thereby not certain. An overload of the above method
	 * 
	 * @param col1
	 * @param col2
	 * @return the difference between the colors as an int value. Higher ->
	 *         Bigger difference
	 */
	protected double calculateColorDifference(int rgb1, int rgb2) {

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
		// Tentatively: maxDiff: 721

		return cDiff;
	}

	/**
	 * Method calculates the RGB difference between two pixels in comparison to
	 * the maximum possible difference. If one of the pixels is transparent,
	 * presumably because of the resizeImage method, it will return the
	 * maxDifference.
	 * 
	 * @param x
	 *            pixelPosition width
	 * @param y
	 *            pixelPosition height
	 * @param img1
	 *            reference image
	 * @param img2
	 *            image to compare
	 * @return the difference between the pixels
	 */
	private double calculatePixelRgbDiff(int x, int y, BufferedImage img1,
			BufferedImage img2) {

		Color color1 = new Color(img1.getRGB(x, y));
		Color color2 = new Color(img2.getRGB(x, y));

		int red1 = color1.getRed();
		int red2 = color2.getRed();
		int green1 = color1.getGreen();
		int green2 = color2.getGreen();
		int blue1 = color1.getBlue();
		int blue2 = color2.getBlue();

		double maxDifference = Math.max(red1, 255 - red1)
				+ Math.max(green1, 255 - green1) + Math.max(blue1, 255 - blue1);

		double difference = Math.abs(blue1 - blue2) + Math.abs(red1 - red2)
				+ Math.abs(green1 - green2);

		return difference / maxDifference;
	}

	/**
	 * Method calculates average Red, Green, Blue and Alpha values of a picture
	 * and returns them as array used in conjunction with subimages in
	 * fuzzyEqual.
	 * 
	 * @param img
	 * @return
	 */
	private double[] calculateAverageRgb(BufferedImage img) {
		// Method calculates average Red, Green, Blue and Alpha values of a
		// picture and returns them as array
		double[] averageRgb = { 0, 0, 0, 0 };
		int imageHeight = img.getHeight();
		int imageWidth = img.getWidth();

		// sum the respective values of each pixel and divide it by the number
		// of pixels
		for (int y = 0; y < imageHeight; y++) {
			for (int x = 0; x < imageWidth; x++) {
				Color color = new Color(img.getRGB(x, y), true);
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

	/**
	 * Method calculates the difference between to pictures with their given
	 * average Red, Green, Blue and Alpha values based on the maximum RGB
	 * difference alpha values are more influential, because they indicate
	 * pixels that were not existent before. The way it is implemented now, a
	 * difference of 255 in alpha and 0 in red, green and black matches a
	 * threshold of 1
	 * 
	 * @param Rgb1
	 * @param Rgb2
	 * @return
	 */
	private double getRgbDifference(double[] Rgb1, double[] Rgb2) {

		double maxDiff = Math.max(Rgb1[0], 255 - Rgb1[0])
				+ Math.max(Rgb1[1], 255 - Rgb1[1])
				+ Math.max(Rgb1[2], 255 - Rgb1[2]);
		double diff = Math.abs(Rgb1[0] - Rgb2[0]) + Math.abs(Rgb1[1] - Rgb2[1])
				+ Math.abs(Rgb1[2] - Rgb2[2])
				+ Math.abs((Rgb1[3] - Rgb2[3]) * 3);

		return diff / maxDiff;
	}

	/**
	 * Returns the color with which the pixels will be marked, used in
	 * exactlyEqual and pixelFuzzyEqual Checks the pixels to the right and to
	 * the bottom (the surrounding pixels that coudn't have been marked
	 * already).
	 * 
	 * @param img
	 * @param x
	 * @param y
	 * @return
	 */
	private int getMarkRgb(BufferedImage img, int x, int y) {

		int markRgb = Color.RED.getRGB();
		int redPixels = 0;
		if (isRedish(img, x + 1, y)) {
			redPixels++;
			for (int i = x - 1; i <= x + 1; i++) {
				if (isRedish(img, i, y)) {
					redPixels++;
				}
			}
		}

		if (redPixels > 2) {
			markRgb = Color.GREEN.getRGB();
		}
		return markRgb;
	}

	/**
	 * Checks if a certain pixels is redish or not. Used in getMarkRgb
	 * 
	 * @param img
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean isRedish(BufferedImage img, int x, int y) {

		if ((x < img.getWidth() && y < img.getHeight()) && (x >= 0 && y >= 0)) {
			int rgb = img.getRGB(x, y);
			Color tempColor = new Color(rgb, true);
			if (tempColor.getRed() > 250 && tempColor.getGreen() < 140
					&& tempColor.getBlue() < 140) {
				return true;
			}
		}
		return false;
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
			if (xBlock != lastX && yBlock != lastY) {
				int subImageWidth = calcPixSpan(markingX, xBlock, imageWidth);
				int subImageHeight = calcPixSpan(markingY, yBlock, imageHeight);
				drawBorders(xBlock, yBlock, subImageWidth, subImageHeight);
				lastX = xBlock;
				lastY = yBlock;
			}
		}
	}

	/**
	 * Colors the borders of a certain rectangle. Used to mark blocks. Uses the
	 * colorPixel method.
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
	private void drawBorders(int currentX, int currentY, int subImageWidth,
			int subImageHeight) {
		int x, y;

		for (int a = 0; a < subImageWidth; a++) {
			x = currentX * pixelPerBlockX + a;
			y = currentY * pixelPerBlockY;
			colorPixel(x, y);

			y = currentY * pixelPerBlockY + subImageHeight - 1;
			colorPixel(x, y);
		}

		for (int b = 1; b < subImageHeight - 1; b++) {
			x = currentX * pixelPerBlockX;
			y = currentY * pixelPerBlockY + b;
			colorPixel(x, y);

			x = currentX * pixelPerBlockX + subImageWidth - 1;
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
	
	private void colorArea (BufferedImage mask, int x, int y) {
		int rgb = Color.BLACK.getRGB();
		int xCorner = x * markingX;
		int yCorner = y * markingY;
		for (int a = 0; a<markingX; a++){
			for (int b = 0; b<markingY; b++) {
				mask.setRGB(xCorner + a, yCorner + b, rgb);
			}
		}
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

	/**
	 * Increases an images width and height, the new image will be in the top
	 * left corner; the rest will be transparent black
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
}
