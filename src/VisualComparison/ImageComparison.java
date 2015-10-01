package VisualComparison;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * Class for comparison of images, written to continously compare screenshots of
 * websites. The class consists of a wrapper method isEqual and several internal
 * methods. It uses the ImageOperations class.
 * 
 * The algorithm to be used, fuzzyness parameters and whether or not a mask
 * should be created is determined at the instantiation. The isEqual method
 * marks differences with rectangles. Their size can not be changed.
 * 
 * @author lucas & damian
 */
public class ImageComparison {

	protected enum ComparisonAlgorithm {
		EXACTLY, PIXELFUZZY, FUZZY
	}

	private BufferedImage difference = null;
	private BufferedImage imgOut = null;
	private BufferedImage maskImage = null;
	private int markingX, markingY, pixelPerBlockXY, imageWidth, imageHeight,
			subImageWidth, subImageHeight;

	private double colTolerance;
	private double pixTolerance;

	private boolean trainingMode;
	private boolean closeMask;
	private int structElementWidth;
	private int structElementHeight;

	private boolean differenceImage;

	private final ComparisonAlgorithm algorithm;

	/**
	 * The parameters pixelPerBlockXY and colTolerance define the fuzzyness of
	 * the comparison, higher parameters mean a comparison that is less strict.
	 * The pixelPerBlock and pixTolerance parameters are only used in the
	 * 'FUZZY' algorithm, the colTolerance in the 'FUZZY' and the 'PIXELFUZZY'
	 * algorithm.
	 * <p>
	 * In case images should only be equal in certain areas or certain areas
	 * should not be compared, a mask image can be created. Black areas in the
	 * mask image will be ignored. Users can manually paint it black or use the
	 * trainingMode to do so.
	 * <p>
	 * If the trainingMode parameter is true, differences will not be marked,
	 * instead the corresponding areas in the mask image will be painted black.
	 * If the closeMask parameter is true, small gaps in he maskImage will be
	 * closed. Black areas in the mask image will not be detected in later
	 * comparisons.
	 * <p>
	 * 
	 * @param markingX
	 *            determines the height of the blocks used for marking and
	 *            masking. Has to be above 0
	 * @param markingY
	 *            determines the width of the blocks used for marking and
	 *            masking. Has to be above 0.
	 * @param pixelPerBlockXY
	 *            in the fuzzyEqual method, the images are divided into blocks
	 *            for further fuzzyness. This parameter determines the width and
	 *            height of the blocks.
	 * @param colTolerance
	 *            up to where small differences in color should be tolerated. A
	 *            value between zero and one should be given. Zero: No
	 *            tolerance.
	 * @param pixTolerance
	 *            how many differences per block will be tolerated, in percent.
	 *            value between zero and one should be given. One: 100%. All
	 *            pixels can be different.
	 * @param trainingMode
	 *            whether or not the training mode should be used. If true,
	 *            differences will be masked for later tests, not marked. And
	 *            the comparison will always return true.
	 * @param closeMask
	 *            decides if small gaps in the mask should be closed after a
	 *            training run.
	 * @param structElementWidth
	 *            determines up to which width gaps should be closed
	 * @param structElementHeight
	 *            determines up to which height gaps should be closed
	 * @param differenceImage
	 *            decides if a difference image should be created in addition to
	 *            the markedImage. The difference image is a greyscale image,
	 *            the higher the difference between the compared images, the
	 *            lighter the corresponding area in the difference image.
	 * @param comparisonAlgorithm
	 *            the algorithm the comparison should use. If the given string
	 *            does not match any algorithm, it throws an
	 *            IllegalArgumentException.
	 */
	public ImageComparison(int markingX, int markingY, int pixelPerBlockXY,
			double colTolerance, double pixTolerance, boolean trainingMode,
			boolean closeMask, int structElementWidth, int structElementHeight,
			boolean differenceImage, String algorithm) {

		// Check if markingX or markingY are below 1, which would make no sense
		// and woudn't work. If they are, just throw an Exception
		if (markingX < 0 || markingY < 0) {
			throw new IllegalArgumentException(
					"Can't draw rectangles with a width or height below one");
		}
		this.markingX = markingX;
		this.markingY = markingY;
		this.pixelPerBlockXY = pixelPerBlockXY;
		this.colTolerance = colTolerance;
		this.pixTolerance = pixTolerance;
		this.trainingMode = trainingMode;
		this.closeMask = closeMask;
		this.structElementWidth = structElementWidth;
		this.structElementHeight = structElementHeight;
		this.differenceImage = differenceImage;

		try {
			this.algorithm = ComparisonAlgorithm.valueOf(algorithm);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Specified algorithm not found");
		}
	}

	/**
	 * Wrapper method for the different comparison methods. Handles resizing,
	 * masking, calls the comparison method and marks the differences or masks
	 * them if trainingMode is true. Also draws the differenceImage if asked
	 * for. And saves the marked image/ the mask image.
	 * 
	 * @param img1
	 *            the reference image
	 * @param img2
	 *            the image that will be compared to the reference image
	 * @param fileMask
	 *            the file where the mask image can be found or where it should
	 *            be created
	 * @param fileOut
	 *            the file where the marked image should be saved if there are
	 *            differences
	 * @param fileDifference
	 *            the file where the difference image should be saved if the
	 *            instance variable difference image is true
	 * @return false if there were changes, true otherwise
	 * @throws IOException
	 */
	public boolean isEqual(BufferedImage img1, BufferedImage img2,
			File fileMask, File fileOut, File fileDifference)
			throws IOException {

		boolean isEqual = true;

		// Initializes ImageOperations object to access it's methods later
		ImageOperations imageoperations = new ImageOperations();

		// copies the images, so it doesn't work directly on them
		BufferedImage copyImg1 = imageoperations.copyImage(img1);
		imgOut = imageoperations.copyImage(img2);

		// Resizes the images and saves the previous width and height ...

		// Initializes the variables for the previous width and height
		int prevWidth = imgOut.getWidth();
		int prevHeight = imgOut.getHeight();

		if (imgOut.getWidth() > copyImg1.getWidth()) {
			prevWidth = copyImg1.getWidth();
			copyImg1 = imageoperations.increaseImageSize(copyImg1,
					imgOut.getWidth(), copyImg1.getHeight());
		}
		if (imgOut.getHeight() > copyImg1.getHeight()) {
			prevHeight = copyImg1.getHeight();
			copyImg1 = imageoperations.increaseImageSize(copyImg1,
					copyImg1.getWidth(), imgOut.getHeight());
		}
		if (imgOut.getWidth() < copyImg1.getWidth()) {
			prevWidth = imgOut.getWidth();
			imgOut = imageoperations.increaseImageSize(imgOut,
					copyImg1.getWidth(), imgOut.getHeight());
		}
		if (imgOut.getHeight() < copyImg1.getHeight()) {
			prevHeight = imgOut.getHeight();
			imgOut = imageoperations.increaseImageSize(imgOut,
					imgOut.getWidth(), copyImg1.getHeight());
		}

		// end resizing

		// initializes variables for imageHeight and imageWidth
		imageWidth = imgOut.getWidth();
		imageHeight = imgOut.getHeight();

		// initializes maskImage and masks both images:
		maskImage = initializeMaskImage(copyImg1, fileMask);
		copyImg1 = imageoperations.overlayMaskImage(copyImg1, maskImage);
		imgOut = imageoperations.overlayMaskImage(imgOut, maskImage);

		// Initialize differentPixels array and the difference image
		int[][] differentPixels = null;
		initializeDifferenceImage();

		// Checks which imagecomparison method to call and calls it.
		// Sets the differentPixels array
		switch (algorithm) {
		case EXACTLY:
			differentPixels = exactlyEqual(copyImg1, imgOut);
			break;
		case PIXELFUZZY:
			differentPixels = pixelFuzzyEqual(copyImg1, imgOut);
			break;
		case FUZZY:
			differentPixels = fuzzyEqual(copyImg1, imgOut);
			break;
		}

		// If there were differences ...
		if (differentPixels != null) {

			if (trainingMode) {
				// Mask differences in the maskImage
				maskDifferences(differentPixels);

			} else {
				// Draw the differences into the difference image
				drawDifferencesToImage(copyImg1, imgOut, differentPixels);

				// Mark the differences
				markDifferences(differentPixels);
				isEqual = false;
			}
		}

		// Close the maskImage if closeMask = true
		// Do it even if there were no differences or if
		// trainingmode was false
		if (closeMask) {
			maskImage = imageoperations.closeImage(maskImage,
					structElementWidth, structElementHeight);
		}

		// Save the maskImage if trainingMode was on or
		// The maskImage should be closed
		if (trainingMode || closeMask) {
			ImageIO.write(maskImage, "PNG", fileMask);
		}

		if (!trainingMode) {

			// If the size of the image changed, mark the
			// previously nonexistant areas and set isEqual false
			if (imageWidth != prevWidth || imageHeight != prevHeight) {	
				difference = markImageBorders(difference, prevWidth, prevHeight);
				imgOut = markImageBorders(imgOut, prevWidth, prevHeight);				
				isEqual = false;
			}
		}

		if (isEqual) {
			return true;
		} else {
			// Save the differenceImage if differenceImage is true
			if (differenceImage) {
				ImageIO.write(difference, "PNG", fileDifference);
			}

			// Save the marked image
			ImageIO.write(imgOut, "PNG", fileOut);
			return false;
		}

	}

	/**
	 * Method for the pixel based comparison with colTolerance. So it will
	 * compare pixel by pixel, but it will have a certain tolerance for color
	 * differences.
	 * 
	 * @param img1
	 *            the reference image
	 * @param img2
	 *            the image that will be compared with the reference image
	 * @return an array with the coordinates of the pixels where there were
	 *         differences, null if there were no differences
	 */
	private int[][] pixelFuzzyEqual(BufferedImage img1, BufferedImage img2)
			throws IOException {

		boolean equal = true;
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<Integer> yCoords = new ArrayList<Integer>();

		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
		for (int x = 0; x < imagewidth; x++) {
			for (int y = 0; y < imageheight; y++) {

				// calculates difference and adds the coordinates to
				// the relevant ArrayList if the difference is above the
				// colTolerance
				double difference = calculatePixelRgbDiff(x, y, img1, img2);

				// draws the differenceImage if needed

				if (difference > colTolerance) {
					xCoords.add(x);
					yCoords.add(y);
					equal = false;
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
	 * Method for the exact pixel based comparison . Zero tolerance.
	 * ColTolerance is not used.
	 * <p>
	 * 
	 * @param img1
	 *            the reference image
	 * @param img2
	 *            the image that will be compared with the reference image
	 * @return an array with the coordinates of the pixels where there were
	 *         differences, null if there were no differences
	 */
	private int[][] exactlyEqual(BufferedImage img1, BufferedImage img2)
			throws IOException {
		/* Method for the exact comparison of two images */
		// img1: reference Image, img2: screenshot

		boolean exactlyEqual = true;
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<Integer> yCoords = new ArrayList<Integer>();

		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
		for (int x = 0; x < imagewidth; x++) {
			for (int y = 0; y < imageheight; y++) {

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
	 * Compares the img2 image to the img1 image using the fuzzyness parameters
	 * defined in the ImageComparison constructor.
	 * 
	 * Uses pixelPerBlockXY, pixTolerance and colTolerance.
	 * 
	 * @param img1
	 *            the reference image
	 * @param img2
	 *            the image that will be compared with the reference image
	 * @return an array with the coordinates of the pixels where there were
	 *         differences, null if there were no differences
	 * @throws IOException
	 */
	private int[][] fuzzyEqual(BufferedImage img1, BufferedImage img2)
			throws IOException {

		/* Method for the regular fuzzy comparison */

		boolean equal = true;
		ArrayList<Integer> xCoords = new ArrayList<Integer>();
		ArrayList<Integer> yCoords = new ArrayList<Integer>();
		ArrayList<Integer> xCoordsTemp = new ArrayList<Integer>();
		ArrayList<Integer> yCoordsTemp = new ArrayList<Integer>();
		int differencesAllowed;

		// Create blocks, go through every block
		int xBlock = imageWidth / pixelPerBlockXY;
		int yBlock = imageHeight / pixelPerBlockXY;

		for (int x = 0; x < xBlock; x++) {
			for (int y = 0; y < yBlock; y++) {
				subImageWidth = calcPixSpan(pixelPerBlockXY, x, imageWidth);
				subImageHeight = calcPixSpan(pixelPerBlockXY, y, imageHeight);
				int differencesPerBlock = 0;
				differencesAllowed = (int) Math.floor(subImageWidth
						* subImageHeight * pixTolerance);

				// Go through every pixel in that block
				for (int w = 0; w < subImageWidth; w++) {
					for (int h = 0; h < subImageHeight; h++) {

						int xCoord = x * pixelPerBlockXY + w;
						int yCoord = y * pixelPerBlockXY + h;

						// calculate the difference and draw the differenceImage
						// if needed
						double difference = calculatePixelRgbDiff(xCoord,
								yCoord, img1, img2);

						// If there is a notable difference
						if (difference > colTolerance) {

							// Increment differencesPerBlock and add the
							// coordinates to the
							// temporary arraylists
							differencesPerBlock++;
							xCoordsTemp.add(xCoord);
							yCoordsTemp.add(yCoord);
						}
					}
				}

				// If differencesPerBlock is above pixTolerance
				// Write the temporary coordinates to the permanent ones
				// And set equal false
				if (differencesPerBlock > differencesAllowed) {
					xCoords.addAll(xCoordsTemp);
					yCoords.addAll(yCoordsTemp);
					xCoordsTemp.clear();
					yCoordsTemp.clear();
					equal = false;
				}
				// Otherwise clear the temporary coordinates
				else {
					xCoordsTemp.clear();
					yCoordsTemp.clear();
				}
			}
		}

		// Turn the ArrayLists into a single 2d array.
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
	 * Calculates how many pixel there can be in the current block. Necessary in
	 * case the block would go over the border.
	 * 
	 * @param pixelPerBlock
	 *            how many pixels every block has
	 * @param n
	 *            the block we're currently in
	 * @param overallSpan
	 *            the width/ height that shoudn't be passed
	 * @return
	 */
	private int calcPixSpan(int pixelPerBlock, int n, int overallSpan) {
		if (pixelPerBlock * (n + 1) > overallSpan)
			return overallSpan % pixelPerBlock;
		else
			return pixelPerBlock;
	}

	/**
	 * The method weights the red, green and blue values and determines the
	 * difference as humans would see it.
	 * 
	 * Based on a comparison algorithm from
	 * http://www.compuphase.com/cmetric.htm . The algorithm is based on
	 * experiments with people, not theoretics. It is thereby not certain.
	 * 
	 * @param x
	 *            the first color as an rgb value
	 * @param y
	 *            the second color as an rgb value
	 * @return the difference between the colors as an int value. Higher ->
	 *         Bigger difference
	 */
	private double calculatePixelRgbDiff(int x, int y, BufferedImage img1,
			BufferedImage img2) {

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
	 * Returns red unless the currentColor is mainly red. Used to determine the
	 * marking color. The names not really right.
	 * 
	 * @param currentColor
	 * @return the color with which to mark
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
	 * Method to mark areas around the detected differences. Goes through every
	 * pixel that was different and marks the marking block it is in, unless it
	 * was marked already. <br>
	 * If markingX of markingY are 1, it will simply mark the detected
	 * differences.
	 * 
	 * Works directly on imgOut.
	 * 
	 * @param pixels
	 *            the array with the differences.
	 */
	private void markDifferences(int[][] pixels) {

		// Check if markingX or markingY are 1. If they are, just mark every
		// different pixel,
		// don't bother with rectangles
		if (markingX == 1 || markingY == 1) {
			for (int i = 0; i < pixels.length; i++) {
				colorPixel(pixels[i][0], pixels[i][1]);
			}
		}

		// And if markingX and markingY are above one, paint rectangles!
		// Normal case
		else {
			int blocksX = imageWidth / markingX;
			int blocksY = imageHeight / markingY;
			boolean[][] markedBlocks = new boolean[blocksX + 1][blocksY + 1];
			for (boolean[] row : markedBlocks) {
				Arrays.fill(row, false);
			}
			for (int x = 0; x < pixels.length; x++) {
				int xBlock = pixels[x][0] / markingX;
				int yBlock = pixels[x][1] / markingY;
				if (!markedBlocks[xBlock][yBlock]) {
					subImageWidth = calcPixSpan(markingX, xBlock, imageWidth);
					subImageHeight = calcPixSpan(markingY, yBlock, imageHeight);
					drawBorders(xBlock, yBlock, markingX, markingY);
					markedBlocks[xBlock][yBlock] = true;
				}
			}
		}
	}

	/**
	 * Very close to markDifferences. Goes through every pixel that was
	 * different and masks the marking block it is in, unless it was marked
	 * already. Works directly on the mask image.
	 * 
	 * @param pixels
	 *            the array with the differences.
	 */
	private void maskDifferences(int[][] pixels) {

		// This method doesn't need a separate if for markingX/ markingY = 1,
		// the colorArea method works for them

		int blocksX = imageWidth / markingX;
		int blocksY = imageHeight / markingY;
		boolean[][] maskedBlocks = new boolean[blocksX + 1][blocksY + 1];
		for (boolean[] row : maskedBlocks) {
			Arrays.fill(row, false);
		}
		for (int x = 0; x < pixels.length; x++) {
			int xBlock = pixels[x][0] / markingX;
			int yBlock = pixels[x][1] / markingY;
			if (!maskedBlocks[xBlock][yBlock]) {
				subImageWidth = calcPixSpan(markingX, xBlock, imageWidth);
				subImageHeight = calcPixSpan(markingY, yBlock, imageHeight);
				colorArea(maskImage, xBlock, yBlock);
				maskedBlocks[xBlock][yBlock] = true;
			}
		}

	}

	/**
	 * Colors the borders of a certain rectangle. Used to mark blocks. Uses the
	 * colorPixel method and subImageHeight/ subImageWidth. <br>
	 * Works directly on imgOut.
	 * 
	 * @param img
	 *            The image in which something will be marked
	 * @param currentX
	 *            Starting position
	 * @param currentY
	 *            Starting position
	 * @param width
	 *            Vertical length of the rectangle to mark
	 * @param height
	 *            Horizontal length of the rectangle to mark
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
	 * Colors a certain pixel using getComplementaryColor. Works directly on
	 * imgOut.
	 * 
	 * @param x
	 *            the x coordinate of the pixel to color
	 * @param y
	 *            the y coordinate of the pixel to color
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

	private void colorArea(BufferedImage img, int x, int y) {
		Color black = Color.BLACK;
		x = x * markingX;
		y = y * markingY;
		Graphics g = img.getGraphics();
		g.setColor(black);
		g.fillRect(x, y, subImageWidth, subImageHeight);
		g.dispose();

	}

	/**
	 * Fully marks the bottom and right borders of an image transparent
	 * (transparent white). Used in isEqual to mark the previously not existent
	 * parts of an image.
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

		final Color markTransparentWhite = new Color(255, 255, 255, 0);
		Graphics2D g = img.createGraphics();
		g.setColor(markTransparentWhite);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1.0f));

		if (startW < imageWidth) {
			g.fillRect(startW, 0, img.getWidth() - startW, img.getHeight());
		}
		if (startH < imageHeight) {
			g.fillRect(0, startH, img.getWidth(), img.getHeight() - startH);
		}
		g.dispose();

		return img;
	}

	/**
	 * Initializes the difference image. It is initialized fully black, since
	 * black = no difference. Works directly on the difference image.
	 */
	private void initializeDifferenceImage() {

		int rgbBlack = Color.BLACK.getRGB();
		difference = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_ARGB);
		int[] differenceArray = ((DataBufferInt) difference.getRaster()
				.getDataBuffer()).getData();
		Arrays.fill(differenceArray, rgbBlack);
	}

	/**
	 * Draws the differences onto the difference image with the correct
	 * grayness. Calculates the difference where there was one and paints the
	 * pixel to the right color. Works directly on the difference image.
	 * 
	 * @param differentPixels
	 */
	private void drawDifferencesToImage(BufferedImage reference,
			BufferedImage toCompare, int[][] differentPixels) {

		double rgbDifference;

		// Go through the differentPixels array, get the difference and
		// draw the pixel into
		for (int i = 0; i < differentPixels.length; i++) {
			rgbDifference = calculatePixelRgbDiff(differentPixels[i][0],
					differentPixels[i][1], reference, toCompare);
			drawDifferencePixel(rgbDifference, differentPixels[i][0],
					differentPixels[i][1]);
		}
	}

	/**
	 * Draws a certain pixel with a certain grayness into the difference image.
	 * 
	 * @param ratio
	 *            difference ratio between the two pixels
	 * @param x
	 *            current x coordinate
	 * @param y
	 *            current y coordinate
	 */
	private void drawDifferencePixel(double ratio, int x, int y) {
		double grey = 255 * ratio;
		int diffColor = (int) Math.round(grey);
		Color greyscale = new Color(diffColor, diffColor, diffColor, 255);
		difference.setRGB(x, y, greyscale.getRGB());
	}

	/**
	 * Initializes the mask Image. If there already is a mask Image in the file,
	 * it will simply read that. If not, it will create a white Image of the
	 * same height and width as the picture given.
	 * 
	 * @param img
	 *            the image corresponding to the maskedImage
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

				// Change image type to TYPE_INT_ARGB
				mask = new ImageOperations().copyImage(mask);
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
}
