package com.xceptance.xlt.visualassertion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.xceptance.xlt.visualassertion.algorithm.ComparisonAlgorithm;
import com.xceptance.xlt.visualassertion.mask.RectangleMask;

public class ImageMask
{
    private final BufferedImage reference;

    private BufferedImage mask;

    public ImageMask(final BufferedImage referenceImage, final BufferedImage maskImage)
    {
        this.reference = ImageHelper.copyImage(referenceImage);

        if (maskImage == null)
        {
            // create a new mask with same dimensions as reference image
            this.mask = ImageHelper.createEmptyImage(referenceImage, BufferedImage.TYPE_INT_ARGB);

            // fill the mask with transparent white
            this.mask = ImageHelper.fillImage(this.mask, ImageHelper.WHITE_TRANSPARENT);
        }
        else
        {
            this.mask = ImageHelper.copyImage(maskImage);
        }
    }

    public ImageMask(final BufferedImage referenceImage)
    {
        this(referenceImage, null);
    }

    public BufferedImage getMask()
    {
        return ImageHelper.copyImage(mask);
    }

    public void train(final BufferedImage image, final ComparisonAlgorithm algorithm, final RectangleMask markerMask)
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
            differences = ImageHelper.compareImages(reference, image);
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
    private BufferedImage maskDifferences(final BufferedImage image, final int[][] pixels, final RectangleMask markerMask, final Color maskingColor)
    {
        final BufferedImage copy = ImageHelper.copyImage(image);

        if (pixels == null)
            return copy;

        // This method doesn't need a separate if for markingX/ markingY = 1,
        // the colorArea method works for them

        final int halfMarkingSizeX = markerMask.getWidth() / 2;
        final int halfMarkingSizeY = markerMask.getHeight() / 2;

        final int markerMaskWidth = markerMask.getWidth();
        final int markerMaskHeight = markerMask.getHeight();

        int x, y;
        final Graphics2D g = copy.createGraphics();
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

    public void closeMask(final int structureElementWidth, final int structureElementHeight)
    {
        mask = ImageHelper.closeImage(mask, structureElementWidth, structureElementHeight, ImageHelper.BLACK.getRGB(),
                ImageHelper.WHITE_TRANSPARENT.getRGB());
    }
}
