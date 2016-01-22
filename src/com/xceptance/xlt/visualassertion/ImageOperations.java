package com.xceptance.xlt.visualassertion;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;

/**
 * Provides methods to perform operations on images. Methods are scaling, increasing, copying, overlay, erosion,
 * dilation and closing. Written for use in ImageComparison. The methods were made to work with binary images.
 * <p>
 * Regarding erosion, dilation and closing: The background/ foreground colors can be manually set, defaults are below.
 * The structure element is always a rectangle filled with ones.
 * <p>
 * The constructor without parameters initializes these defaults:
 * <p>
 * Foreground color: Black from the java.awt.Color class, Color.BLACK <br>
 * Background color: Transparent white with rgb values of 255, 255, 255 and 0. <br>
 * <p>
 * The scaling, which is included in closeImage, is always done by a factor of 10. It should be analogous to the
 * markingX/ markingY values in ImageComparison.
 * 
 * @author damian
 */
public class ImageOperations
{

    private final int rgbForegroundColor;

    private final int rgbBackgroundColor;

    private int scalingFactor;

    /**
     * Sets the background/ foreground colors for erosion/ dilation and the derivative closing method.
     * <p>
     * Background color: Transparent white. <br>
     * Foreground color: Black.
     */
    protected ImageOperations()
    {
        this.rgbBackgroundColor = new Color(255, 255, 255, 0).getRGB();
        this.rgbForegroundColor = Color.BLACK.getRGB();
        this.scalingFactor = 10;
    }

    /**
     * Manually sets the background and foreground colors for the erosion and dilation methods and derivative methods.
     * 
     * @param rgbBackgroundColor
     *            the rgb value of the background color for erosion/ dilation
     * @param rgbForegroundColor
     *            the rgb value of the foreground color erosion/ dilation
     */
    protected ImageOperations(final int rgbBackgroundColor, final int rgbForegroundColor, final int compressionFactor,
                              final double structElementScale)
    {
        this.rgbBackgroundColor = rgbBackgroundColor;
        this.rgbForegroundColor = rgbForegroundColor;
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
     * @return the scaled image
     */
    protected BufferedImage scaleDownMaskImage(final BufferedImage img, final int newWidth, final int newHeight)
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
     * @return the scaled image
     */
    protected BufferedImage scaleUpMaskImage(final BufferedImage img, final int newWidth, final int newHeight)
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
    protected BufferedImage increaseImageSize(final BufferedImage img, final int width, final int height)
    {
        final BufferedImage newImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final int[] newImgArray = ((DataBufferInt) newImg.getRaster().getDataBuffer()).getData();
        int index;
        for (int w = img.getWidth(); w <= width; w++)
        {
            for (int h = img.getHeight(); h <= height; h++)
            {
                index = (h - 1) * newImg.getWidth() + w - 1;
                newImgArray[index] = 0;
            }
        }
        final Graphics g = newImg.createGraphics();
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
    protected BufferedImage copyImage(final BufferedImage source)
    {
        // Creates a fresh BufferedImage that has the same size and content of
        // the source image
        final BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics g = copy.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return copy;
    }

    /**
     * Overlays the black areas of one image over another image. Doesn't actually use transparency.
     * 
     * @param image
     * @param overlay
     *            the image that will be layed over the other image
     * @return
     */
    protected BufferedImage overlayMaskImage(final BufferedImage image, final BufferedImage overlay)
    {
        final int[] imageArray = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        final int[] overlayArray = ((DataBufferInt) overlay.getRaster().getDataBuffer()).getData();

        // Go through every pixel of the image

        for (int i = 0; i < imageArray.length; i++)
        {

            // And set it to black if the overlay image is black
            if (overlayArray[i] == rgbForegroundColor)
            {
                imageArray[i] = overlayArray[i];
            }
        }
        return image;
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
     * @return the eroded image
     */
    protected BufferedImage erodeImage(final BufferedImage img, int structElementWidth, int structElementHeight)
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
     * @return the dilated image
     * @throws IOException
     */
    protected BufferedImage dilateImage(final BufferedImage img, int structElementWidth, int structElementHeight) throws IOException
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
     * @return the closed image
     * @throws IOException
     */
    protected BufferedImage closeImage(BufferedImage img, final int structElementWidth, final int structElementHeight) throws IOException
    {

        final int scaledWidth = (int) Math.ceil(img.getWidth() / scalingFactor);
        final int scaledHeight = (int) Math.ceil(img.getHeight() / scalingFactor);

        // Scale the image for performance reasons.
        BufferedImage shrunkImg = scaleDownMaskImage(img, scaledWidth, scaledHeight);

        // Close it
        shrunkImg = dilateImage(shrunkImg, structElementWidth, structElementHeight);
        shrunkImg = erodeImage(shrunkImg, structElementWidth, structElementHeight);
        // Scale the image back
        img = scaleUpMaskImage(shrunkImg, img.getWidth(), img.getHeight());

        return img;
    }
}
