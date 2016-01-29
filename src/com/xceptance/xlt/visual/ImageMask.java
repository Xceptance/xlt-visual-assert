package com.xceptance.xlt.visual;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.xceptance.xlt.visual.alogrithm.ComparisonAlgorithm;
import com.xceptance.xlt.visual.mask.RectangleMask;

public class ImageMask
{
    private BufferedImage reference;

    private BufferedImage mask;

    public ImageMask(BufferedImage referenceImage, BufferedImage maskImage)
    {
        this.reference = ImageHelper.copyImage(referenceImage);

        if (maskImage == null)
        {
            // create a new mask with same dimensions as reference image
            this.mask = ImageHelper.createEmptyImage(referenceImage);

            // fill the mask with transparent white
            this.mask = ImageHelper.fillImage(this.reference, ImageHelper.WHITE_TRANSPARENT);
        }
        else
        {
            this.mask = ImageHelper.copyImage(maskImage);
        }
    }

    public ImageMask(BufferedImage referenceImage)
    {
        this(referenceImage, null);
    }

    public BufferedImage getMask()
    {
        return ImageHelper.copyImage(mask);
    }

    public void train(BufferedImage image, ComparisonAlgorithm algorithm, RectangleMask markerMask)
    {
        int[][] differences = null;

        switch (algorithm.getType())
        {
            case PIXELFUZZY:
                differences = ImageHelper.fuzzyCompare(reference, image, algorithm.getColorTolerance(), algorithm.getPixelTolerance(),
                                                       algorithm.getFuzzyBlockSize());
                break;

            case COLORFUZZY:
                differences = ImageHelper.colorFuzzyCompare(reference, image, algorithm.getColorTolerance());
                break;

            case EXACTMATCH:
                differences = ImageHelper.imageCompare(reference, image);
                break;
        }

        mask = maskDifferences(mask, differences, markerMask, ImageHelper.BLACK);
    }

    /**
     * Very close to markDifferences. Goes through every pixel that was different and masks the marking block it is in,
     * unless it was marked already. Works directly on the mask image.
     * 
     * @param pixels
     *            the array with the differences.
     * @return
     */
    private BufferedImage maskDifferences(final BufferedImage image, final int[][] pixels, RectangleMask markerMask, Color maskingColor)
    {
        BufferedImage copy = ImageHelper.copyImage(image);

        if (pixels == null)
            return copy;

        // This method doesn't need a separate if for markingX/ markingY = 1,
        // the colorArea method works for them

        int halfMarkingSizeX = markerMask.getWidth() / 2;
        int halfMarkingSizeY = markerMask.getHeight() / 2;

        int markerMaskWidth = markerMask.getWidth();
        int markerMaskHeight = markerMask.getHeight();

        int x, y;
        Graphics2D g = copy.createGraphics();
        g.setColor(maskingColor);

        for (int i = 0; i < pixels.length; i++)
        {
            x = Math.max(0, pixels[i][0] - halfMarkingSizeX);
            y = Math.max(0, pixels[i][1] - halfMarkingSizeY);

            g.fillRect(x, y, markerMaskWidth, markerMaskHeight);

        }
        g.dispose();

        return copy;
    }

    public void closeMask(int structureElementWidth, int structureElementHeight)
    {
        mask = ImageHelper.closeImage(mask, structureElementWidth, structureElementHeight, ImageHelper.BLACK.getRGB(),
                                      ImageHelper.WHITE_TRANSPARENT.getRGB());
    }
}
