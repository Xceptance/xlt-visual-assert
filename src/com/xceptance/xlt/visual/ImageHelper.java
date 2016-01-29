package com.xceptance.xlt.visual;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageHelper
{
    // transparent white
    protected static Color WHITE_TRANSPARENT = new Color(255, 255, 255, 0);

    // black
    protected static Color BLACK = new Color(0, 0, 0);

    protected static int SCALING_FACTOR = 10;

    /**
     * Creates another image, which is a copy of the source image
     * 
     * @param source
     *            the image to copy
     * @return a copy of that image
     */
    protected static BufferedImage copyImage(BufferedImage source)
    {
        // Creates a fresh BufferedImage that has the same size and content of
        // the source image

        // if source has an image type of 0 then it will raise an error
        int imageType = source.getType();
        if (imageType == 0)
        {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }

        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), imageType);
        Graphics g = copy.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();

        return copy;
    }

    protected static BufferedImage createEmptyImage(BufferedImage source)
    {
        int imageType = source.getType();
        if (imageType == 0)
        {
            imageType = BufferedImage.TYPE_INT_ARGB;
        }

        return new BufferedImage(source.getWidth(), source.getHeight(), imageType);
    }

    protected static BufferedImage drawRect(BufferedImage image, Color c, int fromX, int fromY, int width, int height)
    {
        BufferedImage copy = copyImage(image);

        Graphics2D g = copy.createGraphics();
        g.setColor(c);
        g.fillRect(fromX, fromY, width, height);
        g.dispose();

        return copy;
    }

    protected static BufferedImage fillImage(BufferedImage image, Color c)
    {
        return drawRect(image, c, 0, 0, image.getWidth(), image.getHeight());
    }

    /**
     * The method weights the red, green and blue values and determines the difference as humans would see it. Based on
     * a comparison algorithm from http://www.compuphase.com/cmetric.htm . The algorithm is based on experiments with
     * people, not theoretics. It is thereby not certain.
     * 
     * @param rgb1
     *            color number 1
     * @param rgb2
     *            color number 2
     * @return the difference between the colors as percent from 0.0 to 1.0
     */
    protected static double calculatePixelRGBDiff(final int rgb1, final int rgb2)
    {
        final double MAXDIFF = 721.2489168102785;

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

        final double cDiff = Math.sqrt(rWeight * rDiff * rDiff + gWeight * gDiff * gDiff + bWeight * bDiff * bDiff);

        final double cDiffInPercent = cDiff / MAXDIFF;

        return cDiffInPercent;
    }

    protected static int[][] convertCoordinateListsTo2dArray(final ArrayList<Integer> xCoords, final ArrayList<Integer> yCoords)
    {
        int s = xCoords.size();

        int[][] pixels = null;
        if (s > 0)
        {
            pixels = new int[s][2];
            for (int i = 0; i < s; i++)
            {
                pixels[i][0] = xCoords.get(i).intValue();
                pixels[i][1] = yCoords.get(i).intValue();
            }
        }

        return pixels;
    }

    /**
     * Calculates how many pixel there can be in the current block. Necessary in case the block would go over the
     * border.
     * 
     * @param pixelPerBlock
     *            how many pixels every block has
     * @param n
     *            the block we're currently in
     * @param overallSpan
     *            the width/ height that shoudn't be passed
     * @return
     */
    protected static int calcPixSpan(final int pixelPerBlock, final int n, final int overallSpan)
    {
        if (pixelPerBlock * (n + 1) > overallSpan)
            return overallSpan % pixelPerBlock;

        return pixelPerBlock;
    }

    /**
     * Exact pixel by pixel compare. Images must have the same size
     * 
     * @param img1
     * @param img2
     * @return {@link int[][]} or null where int[i][0] = x, int[i][1] = y
     */
    protected static int[][] imageCompare(final BufferedImage img1, final BufferedImage img2)
    {
        final ArrayList<Integer> xCoords = new ArrayList<Integer>();
        final ArrayList<Integer> yCoords = new ArrayList<Integer>();

        for (int x = 0; x < img1.getWidth(); x++)
        {
            for (int y = 0; y < img1.getHeight(); y++)
            {
                // if the RGB values of 2 pixels differ
                if (img1.getRGB(x, y) != img2.getRGB(x, y))
                {
                    xCoords.add(x);
                    yCoords.add(y);
                }
            }
        }

        return convertCoordinateListsTo2dArray(xCoords, yCoords);
    }

    /**
     * Method for the pixel based comparison with colTolerance. So it will compare pixel by pixel, but it will have a
     * certain tolerance for color differences.
     * 
     * @param img1
     *            the reference image
     * @param img2
     *            the image that will be compared with the reference image
     * @return an array with the coordinates of the pixels where there were differences, null if there were no
     *         differences
     */
    protected static int[][] colorFuzzyCompare(final BufferedImage img1, final BufferedImage img2, double colorTolerance)
    {
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
                final double difference = calculatePixelRGBDiff(img1.getRGB(x, y), img2.getRGB(x, y));
                if (difference > colorTolerance)
                {
                    xCoords.add(x);
                    yCoords.add(y);
                }
            }
        }

        return convertCoordinateListsTo2dArray(xCoords, yCoords);
    }

    /**
     * Compares the img2 image to the img1 image using the fuzzyness parameters defined in the ImageComparison
     * constructor. Uses pixelPerBlockXY, pixTolerance and colTolerance.
     * 
     * @param img1
     *            the reference image
     * @param img2
     *            the image that will be compared with the reference image
     * @param pixelTolerance
     * @return an array with the coordinates of the pixels where there were differences, null if there were no
     *         differences
     */
    protected static int[][] fuzzyCompare(final BufferedImage img1, final BufferedImage img2, double colorTolerance, double pixelTolerance,
                                          int fuzzyBlockDimension)
    {
        /* Method for the regular fuzzy comparison */

        final ArrayList<Integer> xCoords = new ArrayList<Integer>();
        final ArrayList<Integer> yCoords = new ArrayList<Integer>();
        final ArrayList<Integer> xCoordsTemp = new ArrayList<Integer>();
        final ArrayList<Integer> yCoordsTemp = new ArrayList<Integer>();

        final int imageWidth = img1.getWidth();
        final int imageHeight = img1.getHeight();

        // Create blocks, go through every block
        final int xBlock = imageWidth / fuzzyBlockDimension;
        final int yBlock = imageHeight / fuzzyBlockDimension;

        for (int x = 0; x < xBlock; x++)
        {
            for (int y = 0; y < yBlock; y++)
            {
                final int subImageWidth = calcPixSpan(fuzzyBlockDimension, x, imageWidth);
                final int subImageHeight = calcPixSpan(fuzzyBlockDimension, y, imageHeight);
                final int differencesAllowed = (int) Math.floor(subImageWidth * subImageHeight * pixelTolerance);
                int differencesPerBlock = 0;

                // Go through every pixel in that block
                for (int w = 0; w < subImageWidth; w++)
                {
                    for (int h = 0; h < subImageHeight; h++)
                    {
                        final int xCoord = x * fuzzyBlockDimension + w;
                        final int yCoord = y * fuzzyBlockDimension + h;

                        // calculate the difference and draw the differenceImage
                        // if needed
                        final double difference = calculatePixelRGBDiff(img1.getRGB(xCoord, yCoord), img2.getRGB(xCoord, yCoord));

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
                if (differencesPerBlock > differencesAllowed)
                {
                    xCoords.addAll(xCoordsTemp);
                    yCoords.addAll(yCoordsTemp);
                }

                // clear the temporary coordinates
                xCoordsTemp.clear();
                yCoordsTemp.clear();
            }
        }

        return convertCoordinateListsTo2dArray(xCoords, yCoords);
    }

    /**
     * Scales a binary image up to the given size. Does not innately preserve Width/ Height ratio. Used in closeImage.
     * Divides the bigger image into blocks. If there are some pixels leftover, the last blocks gets them, no matter how
     * many they are. It sets a pixel to the foreground color if any any pixel in the corresponding block had the
     * foreground color.
     * 
     * @param img
     * @param newWidth
     * @param newHeight
     * @param scalingFactor
     * @param rgbForegroundColor
     * @return the scaled image
     */
    protected static BufferedImage scaleDownMaskImage(final BufferedImage img, final int newWidth, final int newHeight, int scalingFactor,
                                                      int rgbForegroundColor)
    {

        final BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        boolean hasForegroundColor;

        // Go through every pixel of the scaled image
        for (int w = 0; w < scaledImage.getWidth(); w++)
        {
            for (int h = 0; h < scaledImage.getHeight(); h++)
            {
                hasForegroundColor = false;

                // Check if the corresponding block in the image to scale has a
                // black pixel
                for (int x = w * scalingFactor; x < (w + 1) * scalingFactor; x++)
                {
                    for (int y = h * scalingFactor; y < (h + 1) * scalingFactor; y++)
                    {
                        // Check if it isn't over the border
                        if (x < img.getWidth() && y < img.getHeight())
                        {
                            if (img.getRGB(x, y) == rgbForegroundColor)
                            {
                                hasForegroundColor = true;
                                break;
                            }
                        }
                    }
                }

                // And set the pixel of the scaled image black if the
                // corresponding block had any black pixel
                if (hasForegroundColor)
                {
                    scaledImage.setRGB(w, h, rgbForegroundColor);
                }
            }
        }
        return scaledImage;
    }

    /**
     * Scales a binary image up to the given size. Does not innately preserve Width/ Height ratio. Used in closeImage.
     * Divides the bigger image into blocks. Sets all the pixels in the block to the foreground color if the
     * corresponding pixel has the foreground color. If there are some pixels leftover, the last blocks gets them, no
     * matter how many there are.
     * 
     * @param img
     * @param newWidth
     * @param newHeight
     * @param rgbForegroundColor
     * @param scalingFactor
     * @return the scaled image
     */
    protected static BufferedImage scaleUpMaskImage(final BufferedImage img, final int newWidth, final int newHeight,
                                                    int rgbForegroundColor, int scalingFactor)
    {

        final BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        // Go through every pixel of the image to scale
        for (int w = 0; w < img.getWidth(); w++)
        {
            for (int h = 0; h < img.getHeight(); h++)
            {
                // Check if it has the foreground color
                if (img.getRGB(w, h) == rgbForegroundColor)
                {
                    // And set every pixel in the corresponding block true if it
                    // does
                    for (int x = w * scalingFactor; x < w * scalingFactor + scalingFactor; x++)
                    {
                        for (int y = h * scalingFactor; y < h * scalingFactor + scalingFactor; y++)
                        {
                            // So long as it doesn't go over the border
                            if (x < scaledImage.getWidth() && y < scaledImage.getHeight())
                            {
                                scaledImage.setRGB(x, y, rgbForegroundColor);
                            }
                        }
                    }
                }
            }
        }

        return scaledImage;
    }

    /**
     * Increases an images width and height, the old image will be in the top left corner of the new image; the rest
     * will be transparent black
     * 
     * @param img
     * @param width
     * @param height
     * @return the resulting image
     */
    protected static BufferedImage increaseImageSize(final BufferedImage img, final int width, final int height)
    {
        final BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // final int[] newImgArray = ((DataBufferInt) newImg.getRaster().getDataBuffer()).getData();
        // int index;
        // for (int w = img.getWidth(); w <= width; w++)
        // {
        // for (int h = img.getHeight(); h <= height; h++)
        // {
        // index = (h - 1) * newImg.getWidth() + w - 1;
        // newImgArray[index] = 0;
        // }
        // }
        final Graphics g = newImg.createGraphics();

        // TODO: START untested filling method, compare with commented out code above
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, width, height);
        // TODO: END

        g.drawImage(img, 0, 0, null);
        g.dispose();
        return newImg;
    }

    protected static BufferedImage adeptImageSize(BufferedImage img, int width, int height)
    {
        if (img.getWidth() != width || img.getHeight() != height)
        {
            return increaseImageSize(img, width, height);
        }

        return img;
    }

    /**
     * Overlays the black areas of one image over another image. Doesn't actually use transparency.
     * 
     * @param image
     * @param overlay
     *            the image that will be layed over the other image
     * @param rgbForegroundColor
     * @return
     */
    protected static BufferedImage overlayMaskImage(final BufferedImage image, final BufferedImage overlay, int rgbForegroundColor)
    {
        BufferedImage copy = copyImage(image);

        // Go through every pixel of the image
        for (int x = 0; x < copy.getWidth(); x++)
        {
            for (int y = 0; y < copy.getHeight(); y++)
            {
                if (overlay.getRGB(x, y) == rgbForegroundColor)
                {
                    copy.setRGB(x, y, rgbForegroundColor);
                }
            }
        }

        return copy;
    }

    /**
     * Creates and returns an erosion image, using the alorithm from morphological image processing.
     * <p>
     * Assumes the structuring element is filled with ones and thereby only needs it's width and height. The origin is
     * placed in the middle of the structuring element. If width and/ or height are even, they are incremented to make
     * sure there is a middle pixel.
     * 
     * @param img
     *            the image to erode
     * @param structElementWidth
     * @param structElementHeight
     * @param rgbForegroundColor
     * @param rgbBackgroundColor
     * @return the eroded image
     */
    protected static BufferedImage erodeImage(final BufferedImage img, int structElementWidth, int structElementHeight,
                                              int rgbForegroundColor, int rgbBackgroundColor)
    {

        final BufferedImage erosionedImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

        boolean fits;

        // The origin of the structuring element will be it's middle pixel
        // Therefore make sure there is a middle pixel, ie make width and height
        // uneven.
        if ((structElementWidth % 2) == 0)
        {
            structElementWidth++;
        }
        if ((structElementHeight % 2) == 0)
        {
            structElementHeight++;
        }

        // Metaphorically places the structure element
        // In every possible position
        for (int w = 0; w < img.getWidth(); w++)
        {
            for (int h = 0; h < img.getHeight(); h++)
            {

                fits = true;

                // The origin of the structuring element is it's middle pixel
                for (int x = w - (structElementWidth / 2); x <= w + (structElementWidth / 2); x++)
                {
                    for (int y = h - (structElementHeight / 2); y <= h + (structElementHeight / 2); y++)
                    {

                        // As long as the pixels not over the border
                        if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight())
                        {

                            // Assumes all the pixels in the structureImage are
                            // 1. If the pixel does not have the right color
                            // black, set fits false, set the pixel in the
                            // erosionImage to the foreground color and break
                            // the loop
                            if (img.getRGB(x, y) != rgbForegroundColor)
                            {
                                fits = false;
                                erosionedImage.setRGB(w, h, rgbBackgroundColor);
                                break;
                            }
                        }
                    }
                }

                // After every pixel was checked and if fits is true
                // Set the pixel in the erosionImage black
                // Some room for performance increase with a better break?
                if (fits)
                {
                    erosionedImage.setRGB(w, h, rgbForegroundColor);
                }
            }
        }
        return erosionedImage;
    }

    /**
     * Creates and returns a dilation image using the algorithm for morphological image processing.
     * <p>
     * Assumes the structuring element is filled with ones and thereby only needs it's width and height. The origin is
     * placed in the middle of the structuring element. If width and/ or height are even, they are incremented to make
     * sure there is a middle pixel.
     * <p>
     * Uses the rgbForegroundColor and rgbBackgroundColor instance variables to determine the background and the
     * foreground color.
     * 
     * @param img
     *            the image to dilate
     * @param structElementWidth
     * @param structElementHeight
     * @param rgbForegroundColor
     * @param rgbBackgroundColor
     * @return the dilated image
     */
    protected static BufferedImage dilateImage(final BufferedImage img, int structElementWidth, int structElementHeight,
                                               int rgbForegroundColor, int rgbBackgroundColor)
    {

        final BufferedImage dilationImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
        boolean hits;

        // The origin of the structuring element will be it's middle pixel
        // Therefore make sure there is a middle pixel, ie make width and height
        // uneven.
        if ((structElementWidth % 2) == 0)
        {
            structElementWidth++;
        }
        if ((structElementHeight % 2) == 0)
        {
            structElementHeight++;
        }

        // Metaphorically places the structure element
        // In every possible position
        for (int w = 0; w < img.getWidth(); w++)
        {
            for (int h = 0; h < img.getHeight(); h++)
            {

                hits = false;

                // Check every pixel of the structured element
                // against the pixel it metaphorically overlaps
                // There might be some room for performance increase here
                // The origin of the structuring element is it's middle pixel
                for (int x = w - (structElementWidth / 2); x <= w + (structElementWidth / 2); x++)
                {
                    for (int y = h - (structElementHeight / 2); y <= h + (structElementHeight / 2); y++)
                    {

                        // As long as the pixels don't go over the border
                        if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight())
                        {

                            // Assumes all the pixels in the structureImage are
                            // 1. If the pixel is black, set hits true, set the
                            // pixel in the dilationImage to the foreground
                            // color
                            // and break the loop
                            if (img.getRGB(x, y) == rgbForegroundColor)
                            {
                                hits = true;
                                dilationImage.setRGB(w, h, rgbForegroundColor);
                            }
                        }
                    }
                }

                // After every pixel was checked and if hits is false
                // Set the pixel in the dilationImage to he background color
                if (!hits)
                {
                    dilationImage.setRGB(w, h, rgbBackgroundColor);
                }
            }
        }
        return dilationImage;
    }

    /**
     * Shrinks an image using the shrinkImage method, closes it and scales it back up again for performance reasons.
     * Depending on the images size and the compressionfactor, it may still be very performance heavy. Closes an image
     * using the dilation and erosion methods.
     * 
     * @param img
     *            the image to close
     * @param structElementWidth
     *            the width of the structure element for dilation and erosion
     * @param structElementHeight
     *            the height of the structure element for dilation and erosion
     * @param scalingFactor
     * @param rgbForegroundColor
     * @param rgbBackgroundColor
     * @return the closed image
     */
    protected static BufferedImage closeImage(BufferedImage img, final int structElementWidth, final int structElementHeight,
                                              int rgbForegroundColor, int rgbBackgroundColor)
    {

        final int scaledWidth = (int) Math.ceil(img.getWidth() / SCALING_FACTOR);
        final int scaledHeight = (int) Math.ceil(img.getHeight() / SCALING_FACTOR);

        // Scale the image for performance reasons.
        BufferedImage shrunkImg = scaleDownMaskImage(img, scaledWidth, scaledHeight, SCALING_FACTOR, rgbForegroundColor);

        // Close it
        shrunkImg = dilateImage(shrunkImg, structElementWidth, structElementHeight, rgbForegroundColor, rgbBackgroundColor);
        shrunkImg = erodeImage(shrunkImg, structElementWidth, structElementHeight, rgbForegroundColor, rgbBackgroundColor);
        // Scale the image back
        img = scaleUpMaskImage(shrunkImg, img.getWidth(), img.getHeight(), rgbForegroundColor, SCALING_FACTOR);

        return img;
    }

    /**
     * Method to mark areas around the detected differences. Goes through every pixel that was different and marks the
     * marking block it is in, unless it was marked already. <br>
     * If markingX of markingY are 1, it will simply mark the detected differences. Works directly on imgOut.
     * 
     * @param pixels
     *            the array with the differences.
     * @param markingSizeX
     * @param markingSizeY
     * @return
     */
    protected static BufferedImage markDifferences(final BufferedImage image, final int[][] pixels, int markingSizeX, int markingSizeY,
                                                   Color c)
    {
        BufferedImage copy = copyImage(image);

        if (pixels == null)
            return null;

        // Check if markingX or markingY are 1. If they are, just mark every
        // different pixel,
        // don't bother with rectangles
        if (markingSizeX == 1 || markingSizeY == 1)
        {
            int x, y;
            for (int i = 0; i < pixels.length; i++)
            {
                x = pixels[i][0];
                y = pixels[i][1];
                colorPixel(copy, x, y, c);
            }

            return copy;
        }

        final int imageWidth = copy.getWidth();
        final int imageHeight = copy.getHeight();
        // And if markingX and markingY are above one, paint rectangles!
        // Normal case
        final int blocksX = imageWidth / markingSizeX;
        final int blocksY = imageHeight / markingSizeY;

        final boolean[][] markedBlocks = new boolean[blocksX + 1][blocksY + 1];

        int xBlock, yBlock, subImageWidth, subImageHeight;

        for (int x = 0; x < pixels.length; x++)
        {
            xBlock = pixels[x][0] / markingSizeX;
            yBlock = pixels[x][1] / markingSizeY;

            subImageWidth = calcPixSpan(markingSizeX, xBlock, imageWidth);
            subImageHeight = calcPixSpan(markingSizeY, yBlock, imageHeight);

            if (!markedBlocks[xBlock][yBlock])
            {
                drawBorders(copy, xBlock, yBlock, markingSizeX, markingSizeY, subImageWidth, subImageHeight, c);
                markedBlocks[xBlock][yBlock] = true;
            }
        }

        return copy;
    }

    /**
     * Colors a certain pixel using getComplementaryColor. Works directly on imgOut.
     * 
     * @param x
     *            the x coordinate of the pixel to color
     * @param y
     *            the y coordinate of the pixel to color
     */
    protected static void colorPixel(final BufferedImage image, final int x, final int y, Color c)
    {
        Color currentColor = new Color(image.getRGB(x, y));
        Color newColor;

        if (c == null)
        {
            newColor = getComplementary(currentColor);
        }
        else
        {
            newColor = c;
        }

        image.setRGB(x, y, newColor.getRGB());
    }

    /**
     * Colors the borders of a certain rectangle. Used to mark blocks. Uses the colorPixel method and subImageHeight/
     * subImageWidth. <br>
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
     * @param subImageWidth
     * @param subImageHeight
     */
    protected static void drawBorders(final BufferedImage image, final int currentX, final int currentY, final int width, final int height,
                                      int subImageWidth, int subImageHeight, Color c)
    {
        int x, y;

        for (int a = 0; a < subImageWidth; a++)
        {
            x = currentX * width + a;
            y = currentY * height;
            colorPixel(image, x, y, c);

            y = currentY * height + subImageHeight - 1;
            colorPixel(image, x, y, c);
        }

        for (int b = 1; b < subImageHeight - 1; b++)
        {
            x = currentX * width;
            y = currentY * height + b;
            colorPixel(image, x, y, c);

            x = currentX * width + subImageWidth - 1;
            colorPixel(image, x, y, c);
        }
    }

    /**
     * Returns red unless the currentColor is mainly red. Used to determine the marking color. The names not really
     * right.
     * 
     * @param currentColor
     * @return the color with which to mark
     */
    protected static Color getComplementary(final Color currentColor)
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
        else if ((currentColor.getRed() / nonRedSum) > redLimit)
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
     * Fully marks the bottom and right borders of an image transparent (transparent white). Used in isEqual to mark the
     * previously not existent parts of an image.
     * 
     * @param img
     *            the image to mark
     * @param startW
     *            the width from which to start marking
     * @param startH
     *            the height from which the marking starts
     * @return the marked image
     */
    protected static BufferedImage markImageBorders(final BufferedImage img, final int startW, final int startH)
    {
        BufferedImage copy = copyImage(img);

        final Color markTransparentWhite = new Color(255, 255, 255, 0);
        final Graphics2D g = copy.createGraphics();

        g.setColor(markTransparentWhite);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1.0f));

        if (startW < copy.getWidth())
        {
            g.fillRect(startW, 0, copy.getWidth() - startW, copy.getHeight());
        }
        if (startH < copy.getHeight())
        {
            g.fillRect(0, startH, copy.getWidth(), copy.getHeight() - startH);
        }
        g.dispose();

        return copy;
    }
}
