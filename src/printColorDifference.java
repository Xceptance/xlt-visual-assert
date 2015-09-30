import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageIO;

/**
 * A class to show what the difference in the colors is,
 * to illustrate how the color difference is calculated and which
 * color tolerance values make sense.
 * 
 * It prints an image. The borders of the image are in a certain color
 * while the middle of it is full of blocks with different colors.
 * On top of these blocks is a certain number. That number is the color
 * difference between the color of the block and the color around the image.
 * 
 * @author daniel
 *
 */
public class printColorDifference {

	private static final int imgHeight = 1000;
	private static final int imgWidth = 1000;

	private static BufferedImage reference = new BufferedImage(imgWidth,
			imgHeight, BufferedImage.TYPE_INT_ARGB);
	private static BufferedImage comparison = new BufferedImage(imgWidth,
			imgHeight, BufferedImage.TYPE_INT_ARGB);;
	private static File outputFile = new File(
			"/home/daniel/Pictures/output.png");

	private static final Color colReference = new Color(200, 75, 230);
	private static final int blockHeight = 200;
	private static final int blockWidth = imgWidth / 7;

	/**
	 * Calls the method to initialize the reference image and to 
	 * draw the comparison image. Uses a loop and calls the printColDiff method
	 * to draw the color difference onto every block of the comparison image.
	 * 
	 * Finally initializes the output image. draws the comparison image onto it
	 * and prints the result.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		initializeReferenceImage();
		drawComparisonImage();

		// write the color difference into every block
		for (int yBlock = 0; yBlock < comparison.getHeight() / blockHeight; yBlock++) {
			for (int xBlock = 0; xBlock < comparison.getWidth() / blockWidth; xBlock++) {
				printColDiff(xBlock, yBlock);
			}
		}
		
		
		// Create the output image. Fill it with the reference color as a background.
		// Draw the comparison image onto it, so that the borders still have the comparison images color.
		
		BufferedImage outputImage = new BufferedImage(comparison.getWidth() + 200, 
				comparison.getHeight() + 200, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = outputImage.getGraphics();
		graphics.setColor(colReference);
		graphics.fillRect(0, 0, outputImage.getWidth(), outputImage.getHeight());
		graphics.drawImage(comparison, 100, 100, null);
		graphics.dispose();

		ImageIO.write(outputImage, "PNG", outputFile);
	}

	/**
	 * Initializes the reference image. It's color is the color to compare.
	 */
	private static void initializeReferenceImage() {
		int rgb = colReference.getRGB();
		int[] referenceArray = ((DataBufferInt) reference.getRaster()
				.getDataBuffer()).getData();
		Arrays.fill(referenceArray, rgb);
	}

	/**
	 * Draws the comparison image. Divides the image into four columns. In the
	 * first column, the red value changes, in the second the green value
	 * changes, in the third the blue value changes and in the last they all do.
	 * 
	 * The changes should form a circle in every column, so the values that
	 * change change by 256 over the course of the whole column.
	 * 
	 * Results in weird image since the difference between (255, 0, 0) and (204,
	 * 0, 0) is a lot bigger then the difference between (255, 0, 51). There are
	 * clear breaks when it goes from 255 to 0.
	 * 
	 */
	private static void drawComparisonImage() {
		int maxXBlock = comparison.getWidth() / blockWidth;
		int maxYBlock = comparison.getHeight() / blockHeight;

		Graphics graphics = comparison.getGraphics();

		for (int xBlock = 0; xBlock < maxXBlock; xBlock++) {
			Color currentColor = new Color(0, 0, 0);
			for (int yBlock = 0; yBlock < maxYBlock; yBlock++) {
				graphics.setColor(currentColor);
				int x = xBlock * blockWidth;
				int y = yBlock * blockHeight;
				graphics.fillRect(x, y, blockWidth, blockHeight);
				currentColor = getNextColor(currentColor, xBlock + 1);
			}
		}

		graphics.dispose();
	}

	/**
	 * Calculates the next color in a column. Takes all the colors, determines
	 * if red, green, blue or all of them correspond to the current column
	 * increases them. Treats rgb like a circle. 256 => 0.
	 * 
	 * @param xBlock
	 *            the column
	 * @return the next color
	 */
	private static Color getNextColor(Color currentColor, int xBlock) {
		int red = currentColor.getRed();
		int green = currentColor.getGreen();
		int blue = currentColor.getBlue();
		int blockNumber = comparison.getHeight() / blockHeight;
		int rgbDiffWithStep = 256 / (blockNumber - 1) -1;

		switch (xBlock) {
		case 1:
			for (int i = 0; i < rgbDiffWithStep; i++) {
				if (red == 255) {
					red = 0;
				}
				red++;
			}
			break;
			
		case 2:
			for (int i = 0; i < rgbDiffWithStep; i++) {
				if (red == 255) {
					red = 0;
				}
				red++;
				if (green == 255) {
					green = 0;
				}
				green++;
			}
			break;

		case 3:
			for (int i = 0; i < rgbDiffWithStep; i++) {
				if (green == 255) {
					green = 0;
				}
				green++;
			}
			break;
			
		case 4:
			for (int i = 0; i < rgbDiffWithStep; i++) {
				if (green == 255) {
					green = 0;
				}
				green++;
				if (blue == 255) {
					blue = 0;
				}
				blue++;
			}
			break;

		case 5:
			for (int i = 0; i < rgbDiffWithStep; i++) {
				if (blue == 255) {
					blue = 0;
				}
				blue++;
			}
			break;
			
		case 6:
			for (int i = 0; i < rgbDiffWithStep; i++) {
				if (red == 255) {
					red = 0;
				}
				red++;
				if (blue == 255) {
					blue = 0;
				}
				blue++;
			}
			break;
			
		case 7:
			for (int i = 0; i < rgbDiffWithStep; i++) {
				if (red == 255) {
					red = 0;
				}
				red++;
				if (green == 255) {
					green = 0;
				}
				green++;
				if (blue == 255) {
					blue = 0;
				}
				blue++;
			}
			break;

		default:
			// The default case should never happen
			throw new AssertionError();
		}

		currentColor = new Color(red, green, blue);
		return currentColor;
	}

	/**
	 * Prints the difference between the color to compare and the color of the
	 * block onto the block. Identifies the block true the given parameters.
	 * 
	 * 
	 * @param xBlock
	 *            the number of the block in x direction, comes from x value *
	 *            blockWidth
	 * @param yBlock
	 *            the number of the block in y direction, comes from y value *
	 *            blockHeight
	 */
	private static void printColDiff(int xBlock, int yBlock) {
		int x = xBlock * blockWidth;
		int y = yBlock * blockHeight;
		double diff = calculatePixelRgbDiff(x, y, reference, comparison);

		Graphics graphics = comparison.getGraphics();

		if (isDark(comparison.getRGB(x, y))) {
			graphics.setColor(Color.WHITE);
		} else {
			graphics.setColor(Color.BLACK);
		}
		Font font = new Font("VERDANA", Font.PLAIN, 22);
		graphics.setFont(font);

		String diffString = String.format("%.3f", diff);
		graphics.drawString(diffString, x + 50, y + 50);

		graphics.dispose();
	}

	/**
	 * Checks if a color is dark. Used in printColDiff to determine if the
	 * difference should be printed in white or black letters.
	 * 
	 * @param rgb
	 *            the rgb value of the color that may or may not be dark
	 * @return true of the color is dark, false if it's not
	 */
	private static boolean isDark(int rgb) {
		int r = (rgb & 0xff0000) >> 16;
		int g = (rgb & 0xff00) >> 8;
		int b = (rgb & 0xff);

		double luminance = (r * 0.299 + g * 0.587 + b * 0.114) / 256;

		if (luminance < 0.5) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Copied from ImageComparison on the 2015/09/25 Should be the same code,
	 * else it won't work as expected.
	 * 
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
	private static double calculatePixelRgbDiff(int x, int y,
			BufferedImage img1, BufferedImage img2) {

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

}
