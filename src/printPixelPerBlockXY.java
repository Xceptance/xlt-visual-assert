import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SystemUtils;

import VisualComparison.ImageComparison;

public class printPixelPerBlockXY {

	/**
	 * Should illustrate how the pixelPerBlockXY parameter works. Creates a
	 * single picture. That picture is a table, to the right the line shifts
	 * more, to the bottom the pixelPerBlockXY parameter grows. The current
	 * pixelPerBlock value is also printed to the right of the blocks. //TODO
	 * 
	 * @param args
	 */

	private static File fileOutput = new File(
			"/home/daniel/Pictures/showPixelsPerBlockP.png");
	private final static File tempDirectory = SystemUtils.getJavaIoTmpDir();
	private static File fileMarked = new File(tempDirectory + "/marked.png");
	private static File fileTrash = new File(tempDirectory + "/trash.png");

	private static int lineWidth = 50;
	private static int lineShift = 5;
	private static Color backgroundColor = Color.WHITE;
	private static Color lineColor = Color.BLACK;

	final static double pixTolerance = 0.2;
	final static int markingX = 1;
	final static int markingY = 1;
	final static int shiftPixelPerBlock = 5;

	// 100 * 100 pixels per block
	final static int blockWidthAndHeight = 100;
	// 11 blocks per row and column
	final static int blockPerImage = 11;

	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		BufferedImage outputImage = new BufferedImage(blockWidthAndHeight
				* blockPerImage, blockWidthAndHeight * blockPerImage,
				BufferedImage.TYPE_INT_ARGB);
		BufferedImage[] imagesInARow = new BufferedImage[blockPerImage];
		BufferedImage[] rows = new BufferedImage[blockPerImage];
		BufferedImage referenceImage = initializeReferenceImage();

		// Do your thing
		for (int iRow = 0; iRow < blockPerImage; iRow++) {

			for (int iColumn = 0; iColumn < blockPerImage; iColumn++) {

				// create the image to compare
				BufferedImage imgToComp = initializeImgToCompare(iColumn);

				// Get the marked image and save it to the right position
				int pixelPerBlockXY = shiftPixelPerBlock * iRow + 1;
				ImageComparison imagecomparison = new ImageComparison(markingX,
						markingX, pixelPerBlockXY, 0.00, pixTolerance, false,
						false, 3, 3, false, "FUZZY");
				boolean result = imagecomparison.isEqual(referenceImage,
						imgToComp, fileTrash, fileMarked, fileTrash);
				if (!result) {
					imagesInARow[iColumn] = ImageIO.read(fileMarked);
				} else {
					imagesInARow[iColumn] = imgToComp;
				}

				imagesInARow[iColumn] = drawBlockBorders(imagesInARow[iColumn]);
				if (pixelPerBlockXY != 1) {
					imagesInARow[iColumn] = drawPixelPerBlockRect(
							imagesInARow[iColumn], pixelPerBlockXY);
				}
			}

			// merge the images in a row
			rows[iRow] = new BufferedImage(blockWidthAndHeight * blockPerImage,
					100, BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < imagesInARow.length; i++) {
				Graphics graphicsRow = rows[iRow].getGraphics();
				graphicsRow.drawImage(imagesInARow[i], blockWidthAndHeight * i,
						0, null);
				graphicsRow.dispose();
			}
		}

		// merge the rows
		Graphics graphicsOutputImage = outputImage.getGraphics();
		for (int i = 0; i < rows.length; i++) {
			graphicsOutputImage.drawImage(rows[i], 0, blockWidthAndHeight * i,
					null);
		}
		graphicsOutputImage.dispose();

		ImageIO.write(outputImage, "PNG", fileOutput);
		fileMarked.delete();
		fileTrash.delete();

	}

	private static BufferedImage drawPixelPerBlockRect(BufferedImage img,
			int pixelPerBlockXY) {
		Graphics graphics = img.getGraphics();
		graphics.setColor(Color.GREEN);

		for (int h = 0; h < img.getHeight(); h = h + pixelPerBlockXY) {
			for (int w = 0; w < img.getWidth(); w = w + pixelPerBlockXY) {

				graphics.drawRect(w, h, pixelPerBlockXY, pixelPerBlockXY);
			}
		}
		graphics.dispose();

		return img;
	}

	private static BufferedImage drawBlockBorders(BufferedImage img) {

		// Paint a rectangle around the block to make it easier to read
		Graphics graphics = img.getGraphics();
		graphics.setColor(Color.BLUE);
		graphics.drawRect(0, 0, img.getWidth() - 1, img.getHeight() - 1);
		graphics.dispose();

		return img;
	}

	private static BufferedImage initializeReferenceImage() {
		BufferedImage reference = new BufferedImage(blockWidthAndHeight,
				blockWidthAndHeight, BufferedImage.TYPE_INT_ARGB);

		for (int w = 0; w < reference.getWidth(); w++) {
			for (int h = 0; h < reference.getHeight(); h++) {
				if (h < lineWidth) {
					reference.setRGB(w, h, lineColor.getRGB());
				} else {
					reference.setRGB(w, h, backgroundColor.getRGB());
				}
			}
		}

		return reference;
	}

	private static BufferedImage initializeImgToCompare(int iColumn) {
		BufferedImage imageToCompare = new BufferedImage(blockWidthAndHeight,
				blockWidthAndHeight, BufferedImage.TYPE_INT_ARGB);

		for (int w = 0; w < imageToCompare.getWidth(); w++) {
			for (int h = 0; h < imageToCompare.getHeight(); h++) {
				if (iColumn * lineShift <= h
						&& h <= iColumn * lineShift + lineWidth) {
					imageToCompare.setRGB(w, h, lineColor.getRGB());
				} else {
					imageToCompare.setRGB(w, h, backgroundColor.getRGB());
				}
			}
		}

		return imageToCompare;
	}
}
