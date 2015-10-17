package com.xceptance.xlt.visualassertion;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.xceptance.xlt.api.util.XltLogger;

/**
 * Class for comparison of images, written to continuously compare screenshots of
 * websites. The class consists of a wrapper method isEqual and several internal
 * methods. It uses the ImageOperations class.
 * 
 * The algorithm to be used, fuzzyness parameters and whether or not a mask
 * should be created is determined at the instantiation. The isEqual method
 * marks differences with rectangles. Their size can not be changed.
 * 
 * @author lucas & damian & rene
 */
public class ImageComparison
{
	public enum Algorithm
	{
		MATCH, COLORFUZZY, FUZZY
	}

	private BufferedImage difference;
	private BufferedImage maskImage;

	private final int markingSizeX;
	private final int markingSizeY;
	private final int fuzzyBlockDimension;

	private int imageWidth;
	private int imageHeight;
	private int subImageWidth;
	private int subImageHeight;

	private final double colorTolerance;
	private final double pixelTolerance;

	private final boolean trainingsMode;
	private final boolean closeMask;
	private final int closeMaskWidth;
	private final int closeMaskHeight;

	private final boolean differenceImage;

	private final Algorithm algorithm;

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
	 * @param markingX
	 *            determines the height of the blocks used for marking and
	 *            masking. Has to be above 0
	 * @param markingY
	 *            determines the width of the blocks used for marking and
	 *            masking. Has to be above 0.
	 * @param fuzzyBlockDimension
	 *            in the fuzzyEqual method, the images are divided into blocks
	 *            for further fuzzyness. This parameter determines the width and
	 *            height of the blocks.
	 * @param colorTolerance
	 *            up to where small differences in color should be tolerated. A
	 *            value between zero and one should be given. Zero: No
	 *            tolerance.
	 * @param pixelTolerance
	 *            how many differences per block will be tolerated, in percent.
	 *            value between zero and one should be given. One: 100%. All
	 *            pixels can be different.
	 * @param trainingsMode
	 *            whether or not the training mode should be used. If true,
	 *            differences will be masked for later tests, not marked. And
	 *            the comparison will always return true.
	 * @param closeMask
	 *            decides if small gaps in the mask should be closed after a
	 *            training run.
	 * @param closeMaskWidth
	 *            determines up to which width gaps should be closed
	 * @param closeMaskHeight
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
	public ImageComparison(final Algorithm algorithm, final int markingX, final int markingY,
			final int fuzzyBlockDimension, final double colorTolerance, final double pixelTolerance,
			final boolean trainingsMode, final boolean closeMask, final int closeMaskWidth,
			final int closeMaskHeight, final boolean differenceImage)
	{
		// Check if markingX or markingY are below 1, which would make no sense
		// and woudn't work. If they are, just throw an Exception
		if (markingX < 1 || markingY < 1)
		{
			throw new IllegalArgumentException("Can't draw rectangles with a width or height of 0");
		}

		this.algorithm = algorithm;

		this.markingSizeX = markingX;
		this.markingSizeY = markingY;
		this.fuzzyBlockDimension = fuzzyBlockDimension;
		this.colorTolerance = colorTolerance;
		this.pixelTolerance = pixelTolerance;
		this.trainingsMode = trainingsMode;
		this.closeMask = closeMask;
		this.closeMaskWidth = closeMaskWidth;
		this.closeMaskHeight = closeMaskHeight;
		this.differenceImage = differenceImage;
	}

	/**
	 * Just requires the algorithm. The rest is defaults.
	 * 
	 * @param algorithm
	 *            the algorithm to use
	 */
	public ImageComparison(final Algorithm algorithm)
	{
		this(algorithm, 10, 10, 10, 0.1, 0.1, false, false, 10, 10, false);
	}

	/**
	 * Wrapper method for the different comparison methods. Handles resizing,
	 * masking, calls the comparison method and marks the differences or masks
	 * them if trainingMode is true. Also draws the differenceImage if asked
	 * for. And saves the marked image/ the mask image.
	 * 
	 * @param baselineImage
	 *            the reference image
	 * @param compareImage
	 *            the image that will be compared to the reference image
	 * @param fileMask
	 *            the file where the mask image can be found or where it should
	 *            be created
	 * @param fileMarked
	 *            the file where the marked image should be saved if there are
	 *            differences
	 * @param fileDifference
	 *            the file where the difference image should be saved if the
	 *            instance variable difference image is true
	 * @return false if there were changes, true otherwise
	 * @throws IOException
	 */
	public boolean isEqual(final BufferedImage baselineImage, final BufferedImage compareImage,
			final File fileMask, final File fileMarked, final File fileDifference) throws IOException
			{
		boolean isEqual = true;

		// Initializes ImageOperations object to access it's methods later
		final ImageOperations imageoperations = new ImageOperations();

		// copies the images, so it doesn't work directly on them
		BufferedImage baselineImageCopy = imageoperations.copyImage(baselineImage);
		BufferedImage compareImageCopy = imageoperations.copyImage(compareImage);

		// Resizes the images and saves the previous width and height ...

		// Initializes the variables for the previous width and height
		final int baselineImageOriginalWidth = compareImageCopy.getWidth();
		final int baselineImageOriginalHeight = compareImageCopy.getHeight();

		final int compareImageOriginalWidth = compareImageCopy.getWidth();
		final int compareImageOriginalHeight = compareImageCopy.getHeight();

		boolean sizeChanged = false;

		if (baselineImageCopy.getWidth() < compareImageCopy.getWidth())
		{
			baselineImageCopy = imageoperations.increaseImageSize(baselineImageCopy, compareImageCopy.getWidth(), baselineImageCopy.getHeight());
			sizeChanged = true;
		}
		if (baselineImageCopy.getHeight() < compareImageCopy.getHeight())
		{
			baselineImageCopy = imageoperations.increaseImageSize(baselineImageCopy, baselineImageCopy.getWidth(), compareImageCopy.getHeight());
			sizeChanged = true;
		}
		if (baselineImageCopy.getWidth() > compareImageCopy.getWidth())
		{
			compareImageCopy = imageoperations.increaseImageSize(compareImageCopy, baselineImageCopy.getWidth(), compareImageCopy.getHeight());
			sizeChanged = true;
		}
		if (baselineImageCopy.getHeight() > compareImageCopy.getHeight())
		{
			compareImageCopy = imageoperations.increaseImageSize(compareImageCopy, compareImageCopy.getWidth(), baselineImageCopy.getHeight());
			sizeChanged = true;
		}

		// end resizing

		// initializes variables for imageHeight and imageWidth
		imageWidth = compareImageCopy.getWidth();
		imageHeight = compareImageCopy.getHeight();

		// initializes maskImage and masks both images:
		maskImage = initializeMaskImage(baselineImageCopy, fileMask);
		baselineImageCopy = imageoperations.overlayMaskImage(baselineImageCopy, maskImage);
		compareImageCopy = imageoperations.overlayMaskImage(compareImageCopy, maskImage);

		// Initialize differentPixels array and the difference image
		int[][] differentPixels = null;
		initializeDifferenceImage();

		// Checks which imagecomparison method to call and calls it.
		// Sets the differentPixels array
		switch (algorithm)
		{
		case MATCH:
			differentPixels = exactlyEqual(baselineImageCopy, compareImageCopy);
			break;
		case COLORFUZZY:
			differentPixels = pixelFuzzyEqual(baselineImageCopy, compareImageCopy);
			break;
		case FUZZY:
			differentPixels = fuzzyEqual(baselineImageCopy, compareImageCopy);
			break;
		}

		// If there are differences ...
		if (differentPixels != null)
		{
			if (trainingsMode)
			{
				// Mask differences in the maskImage only
				// do not report problems
				maskDifferences(differentPixels);

			}
			else
			{
				// Draw the differences into the difference image
				if (differenceImage)
				{
					drawDifferencesToImage(baselineImageCopy, compareImageCopy, differentPixels);
				}

				// Mark the differences
				markDifferences(compareImageCopy, differentPixels);
				isEqual = false;
			}
		}

		// Close the maskImage if closeMask = true
		// Do it even if there were no differences or if
		// trainingmode was false
		if (closeMask)
		{
			maskImage = imageoperations.closeImage(maskImage, closeMaskWidth, closeMaskHeight);
		}

		// Save the maskImage if trainingMode was on or
		// The maskImage should be closed
		if (trainingsMode || closeMask)
		{
			ImageIO.write(maskImage, "PNG", fileMask);
		}

		if (!trainingsMode)
		{
			// If the size of the image changed, mark the
			// previously nonexistant areas and set isEqual false
			if (sizeChanged)
			{
				difference = markImageBorders(difference, 
						Math.min(baselineImageOriginalWidth, compareImageOriginalWidth), 
						Math.min(baselineImageOriginalHeight, compareImageOriginalHeight));

				// disabled, because do not mark make the size change transparent
				// just really mark it
				//	compareImageCopy = markImageBorders(compareImageCopy,
				//		Math.min(baselineImageOriginalWidth, compareImageOriginalWidth), 
				//		Math.min(baselineImageOriginalHeight, compareImageOriginalHeight));

				isEqual = false;
			}
		}

		if (isEqual)
		{
			return true;
		}
		else
		{
			// Save the differenceImage if differenceImage is true
			if (differenceImage)
			{
				ImageIO.write(difference, "PNG", fileDifference);
			}

			// Save the marked image
			ImageIO.write(compareImageCopy, "PNG", fileMarked);
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
	private int[][] pixelFuzzyEqual(final BufferedImage img1, final BufferedImage img2)
			throws IOException
			{

		boolean equal = true;
		final ArrayList<Integer> xCoords = new ArrayList<Integer>();
		final ArrayList<Integer> yCoords = new ArrayList<Integer>();

		final int imagewidth = img1.getWidth();
		final int imageheight = img1.getHeight();
		for (int x = 0; x < imagewidth; x++)
		{
			for (int y = 0; y < imageheight; y++)
			{

				// calculates difference and adds the coordinates to
				// the relevant ArrayList if the difference is above the
				// colTolerance
				final double difference = calculatePixelRgbDiff(x, y, img1, img2);

				// draws the differenceImage if needed

				if (difference > colorTolerance)
				{
					xCoords.add(x);
					yCoords.add(y);
					equal = false;
				}
			}
		}

		int s = xCoords.size();
		final int[] xArray = new int[s];
		for (int i = 0; i < s; i++)
		{
			xArray[i] = xCoords.get(i).intValue();
		}

		s = yCoords.size();
		final int[] yArray = new int[s];
		for (int i = 0; i < s; i++)
		{
			yArray[i] = yCoords.get(i).intValue();
		}

		final int[][] pixels = new int[xArray.length][2];
		for (int i = 0; i < xArray.length; i++)
		{
			pixels[i][0] = xArray[i];
			pixels[i][1] = yArray[i];
		}
		if (!equal)
		{
			return pixels;
		}
		else
		{
			return null;
		}
			}

	/**
	 * Method for the exact pixel based comparison . Zero tolerance.
	 * <p>
	 * 
	 * @param img1
	 *            the reference image
	 * @param img2
	 *            the image that will be compared with the reference image
	 * @return an array with the coordinates of the pixels where there were
	 *         differences, null if there were no differences
	 */
	private int[][] exactlyEqual(final BufferedImage img1, final BufferedImage img2)
			throws IOException
			{
		/* Method for the exact comparison of two images */
		// img1: reference Image, img2: screenshot

		boolean exactlyEqual = true;
		final ArrayList<Integer> xCoords = new ArrayList<Integer>();
		final ArrayList<Integer> yCoords = new ArrayList<Integer>();

		final int imagewidth = img1.getWidth();
		final int imageheight = img1.getHeight();
		for (int x = 0; x < imagewidth; x++)
		{
			for (int y = 0; y < imageheight; y++)
			{

				// if the RGB values of 2 pixels differ
				// add the x- and y- coordinates to the corresponding ArrayLists
				if (img1.getRGB(x, y) != img2.getRGB(x, y))
				{
					xCoords.add(x);
					yCoords.add(y);
					exactlyEqual = false;
				}
			}
		}

		int s = xCoords.size();
		final int[] xArray = new int[s];
		for (int i = 0; i < s; i++)
		{
			xArray[i] = xCoords.get(i).intValue();
		}

		s = yCoords.size();
		final int[] yArray = new int[s];
		for (int i = 0; i < s; i++)
		{
			yArray[i] = yCoords.get(i).intValue();
		}

		final int[][] pixels = new int[xArray.length][2];
		for (int i = 0; i < xArray.length; i++)
		{
			pixels[i][0] = xArray[i];
			pixels[i][1] = yArray[i];
		}

		if (!exactlyEqual)
		{
			return pixels;
		}
		else
		{
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
	private int[][] fuzzyEqual(final BufferedImage img1, final BufferedImage img2)
			throws IOException
			{

		/* Method for the regular fuzzy comparison */

		boolean equal = true;
		final ArrayList<Integer> xCoords = new ArrayList<Integer>();
		final ArrayList<Integer> yCoords = new ArrayList<Integer>();
		final ArrayList<Integer> xCoordsTemp = new ArrayList<Integer>();
		final ArrayList<Integer> yCoordsTemp = new ArrayList<Integer>();
		int differencesAllowed;

		// Create blocks, go through every block
		final int xBlock = imageWidth / fuzzyBlockDimension;
		final int yBlock = imageHeight / fuzzyBlockDimension;

		for (int x = 0; x < xBlock; x++)
		{
			for (int y = 0; y < yBlock; y++)
			{
				subImageWidth = calcPixSpan(fuzzyBlockDimension, x, imageWidth);
				subImageHeight = calcPixSpan(fuzzyBlockDimension, y, imageHeight);
				int differencesPerBlock = 0;
				differencesAllowed = (int) Math.floor(subImageWidth
						* subImageHeight * pixelTolerance);

				// Go through every pixel in that block
				for (int w = 0; w < subImageWidth; w++)
				{
					for (int h = 0; h < subImageHeight; h++)
					{

						final int xCoord = x * fuzzyBlockDimension + w;
						final int yCoord = y * fuzzyBlockDimension + h;

						// calculate the difference and draw the differenceImage
						// if needed
						final double difference = calculatePixelRgbDiff(xCoord,
								yCoord, img1, img2);

						// If there is a notable difference
						if (difference > colorTolerance)
						{

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
				if (differencesPerBlock > differencesAllowed)
				{
					xCoords.addAll(xCoordsTemp);
					yCoords.addAll(yCoordsTemp);
					xCoordsTemp.clear();
					yCoordsTemp.clear();
					equal = false;
				}
				// Otherwise clear the temporary coordinates
				else
				{
					xCoordsTemp.clear();
					yCoordsTemp.clear();
				}
			}
		}

		// Turn the ArrayLists into a single 2d array.
		int s = xCoords.size();
		final int[] xArray = new int[s];
		for (int i = 0; i < s; i++)
		{
			xArray[i] = xCoords.get(i).intValue();
		}

		s = yCoords.size();
		final int[] yArray = new int[s];
		for (int i = 0; i < s; i++)
		{
			yArray[i] = yCoords.get(i).intValue();
		}

		final int[][] pixels = new int[xArray.length][2];
		for (int i = 0; i < xArray.length; i++)
		{
			pixels[i][0] = xArray[i];
			pixels[i][1] = yArray[i];
		}
		if (!equal)
		{
			return pixels;
		}
		else
		{
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
	private int calcPixSpan(final int pixelPerBlock, final int n, final int overallSpan)
	{
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
	private double calculatePixelRgbDiff(final int x, final int y, final BufferedImage img1,
			final BufferedImage img2)
	{

		final double MAXDIFF = 721.2489168102785;

		final int rgb1 = img1.getRGB(x, y);
		final int rgb2 = img2.getRGB(x, y);

		// Initialize the red, green, blue values
		final int r1 = (rgb1 >> 16) & 0xFF;
		final int g1 = (rgb1 >> 8) & 0xFF;
		final int b1 = rgb1 & 0xFF;
		final int r2 = (rgb2 >> 16) & 0xFF;
		final int g2 = (rgb2 >> 8) & 0xFF;
		final int b2 = rgb2 & 0xFF;
		final int rDiff = r1 - r2;
		final int gDiff = g1 - g2;
		final int bDiff = b1 - b2;

		// Initialize the weight parameters
		final int rLevel = (r1 + r2) / 2;
		final double rWeight = 2 + rLevel / 256;
		final double gWeight = 4.0;
		final double bWeight = 2 + ((255 - rLevel) / 256);

		final double cDiff = Math.sqrt(rWeight * rDiff * rDiff + gWeight * gDiff
				* gDiff + bWeight * bDiff * bDiff);

		final double cDiffInPercent = cDiff / MAXDIFF;

		return cDiffInPercent;
	}

	/**
	 * Returns red unless the currentColor is mainly red. Used to determine the
	 * marking color. The names not really right.
	 * 
	 * @param currentColor
	 * @return the color with which to mark
	 */
	private Color getComplementary(final Color currentColor)
	{
		final double redLimit = 0.8;

		final int nonRedSum = currentColor.getGreen() + currentColor.getBlue();

		if (nonRedSum == 0)
		{
			// only red
			return Color.RED;
		}
		if (currentColor.getRed() < currentColor.getBlue() || currentColor.getRed() < currentColor.getGreen())
		{
			// red does not dominate
			return Color.RED;
		}
		else if ((currentColor.getRed() / nonRedSum) > redLimit )
		{
			// red is strong in that one
			return Color.GREEN;
		}
		else
		{
			return Color.RED;
		}
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
	private void markDifferences(final BufferedImage image, final int[][] pixels)
	{
		// Check if markingX or markingY are 1. If they are, just mark every
		// different pixel,
		// don't bother with rectangles
		if (markingSizeX == 1 || markingSizeY == 1)
		{
			for (int i = 0; i < pixels.length; i++)
			{
				colorPixel(image, pixels[i][0], pixels[i][1]);
			}

			return;
		}

		// And if markingX and markingY are above one, paint rectangles!
		// Normal case
		final int blocksX = imageWidth / markingSizeX;
		final int blocksY = imageHeight / markingSizeY;

		final boolean[][] markedBlocks = new boolean[blocksX + 1][blocksY + 1];

		for (final boolean[] row : markedBlocks)
		{
			Arrays.fill(row, false);
		}

		for (int x = 0; x < pixels.length; x++)
		{
			final int xBlock = pixels[x][0] / markingSizeX;
			final int yBlock = pixels[x][1] / markingSizeY;

			if (!markedBlocks[xBlock][yBlock])
			{
				subImageWidth = calcPixSpan(markingSizeX, xBlock, imageWidth);
				subImageHeight = calcPixSpan(markingSizeY, yBlock, imageHeight);

				drawBorders(image, xBlock, yBlock, markingSizeX, markingSizeY);
				markedBlocks[xBlock][yBlock] = true;
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
	private void maskDifferences(final int[][] pixels)
	{

		// This method doesn't need a separate if for markingX/ markingY = 1,
		// the colorArea method works for them

		final long s = System.currentTimeMillis();

		for (int i = 0; i < pixels.length; i++)
		{
			final int x = Math.max(0, pixels[i][0] - (markingSizeX / 2));
			final int y = Math.max(0, pixels[i][1] - (markingSizeY / 2));

			drawBlackRectangle(maskImage, x, y, markingSizeX, markingSizeY);
		}

		final long e = System.currentTimeMillis();
		XltLogger.runTimeLogger.info(MessageFormat.format("Masking image took {0}ms.", e - s));
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
	private void drawBorders(final BufferedImage image, final int currentX, final int currentY, final int width, final int height)
	{
		int x, y;

		for (int a = 0; a < subImageWidth; a++)
		{
			x = currentX * width + a;
			y = currentY * height;
			colorPixel(image, x, y);

			y = currentY * height + subImageHeight - 1;
			colorPixel(image, x, y);
		}

		for (int b = 1; b < subImageHeight - 1; b++)
		{
			x = currentX * width;
			y = currentY * height + b;
			colorPixel(image, x, y);

			x = currentX * width + subImageWidth - 1;
			colorPixel(image, x, y);
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
	private void colorPixel(final BufferedImage image, final int x, final int y)
	{
		int rgb, newRgb;
		Color currentColor, newColor;

		rgb = image.getRGB(x, y);
		currentColor = new Color(rgb);
		newColor = getComplementary(currentColor);
		newRgb = newColor.getRGB();
		image.setRGB(x, y, newRgb);
	}

	/**
	 * Just draws a black rectangle
	 * 
	 * @param img
	 *            the image to draw on
	 * @param x
	 *            the x position
	 * @param y
	 *            the x position
	 * @param widthX
	 *            the X width
	 * @param widthY
	 *            the Y width
	 */
	private void drawBlackRectangle(final BufferedImage img, final int x, final int y, final int widthX, final int widthY)
	{
		final Color black = Color.BLACK;

		final Graphics g = img.getGraphics();
		g.setColor(black);
		g.fillRect(x, y, widthX, widthY);
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
	private BufferedImage markImageBorders(final BufferedImage img, final int startW, final int startH)
	{
		final Color markTransparentWhite = new Color(255, 255, 255, 0);

		final Graphics2D g = img.createGraphics();
		g.setColor(markTransparentWhite);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1.0f));

		if (startW < imageWidth)
		{
			g.fillRect(startW, 0, img.getWidth() - startW, img.getHeight());
		}
		if (startH < imageHeight)
		{
			g.fillRect(0, startH, img.getWidth(), img.getHeight() - startH);
		}
		g.dispose();

		return img;
	}

	/**
	 * Initializes the difference image. It is initialized fully black, since
	 * black = no difference. Works directly on the difference image.
	 */
	private void initializeDifferenceImage()
	{

		final int rgbBlack = Color.BLACK.getRGB();
		difference = new BufferedImage(imageWidth, imageHeight,
				BufferedImage.TYPE_INT_ARGB);
		final int[] differenceArray = ((DataBufferInt) difference.getRaster()
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
	private void drawDifferencesToImage(final BufferedImage reference,
			final BufferedImage toCompare, final int[][] differentPixels)
	{

		double rgbDifference;

		// Go through the differentPixels array, get the difference and
		// draw the pixel into
		for (int i = 0; i < differentPixels.length; i++)
		{
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
	private void drawDifferencePixel(final double ratio, final int x, final int y)
	{
		final double grey = 255 * ratio;
		final int diffColor = (int) Math.round(grey);
		final Color greyscale = new Color(diffColor, diffColor, diffColor, 255);
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
	private BufferedImage initializeMaskImage(final BufferedImage img, final File file)
			throws IOException
			{
		final Color transparentWhite = new Color(255, 255, 255, 0);
		final int rgbTransparentWhite = transparentWhite.getRGB();

		if (file != null && file.exists())
		{
			BufferedImage mask = ImageIO.read(file);
			if ((mask.getWidth() == img.getWidth())
					&& (mask.getHeight() == img.getHeight()))
			{

				// Change image type to TYPE_INT_ARGB
				mask = new ImageOperations().copyImage(mask);
				return mask;
			}
		}

		final int width = img.getWidth();
		final int height = img.getHeight();
		final BufferedImage mask = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		final int[] maskArray = ((DataBufferInt) mask.getRaster().getDataBuffer())
				.getData();
		Arrays.fill(maskArray, rgbTransparentWhite);
		return mask;
			}
}
