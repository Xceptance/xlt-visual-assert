package com.xceptance.xlt.visualassertion.util;

import java.awt.*;
import java.awt.image.BufferedImage;

import com.xceptance.xlt.visualassertion.algorithm.ComparisonAlgorithm;
import org.junit.Assert;

public class ImageComparison
{
    private Point[] lastDifferences = null;

    private BufferedImage reference;

    private BufferedImage lastCompareImage;

    private boolean resized = false;

    // only used to draw borders in difference file if on image compare one of the images had different sizes and image
    // must be adepted
    private final int initialWidth = 0, initialHeight = 0;

    public ImageComparison(final BufferedImage reference)
    {
        this.reference = reference;
    }

    public boolean isEqual(final BufferedImage compareImage, final BufferedImage mask, final ComparisonAlgorithm algorithm)
    {
        resized = false;

        lastCompareImage = ImageHelper.copyImage(compareImage);
        BufferedImage maskCopy = ImageHelper.copyImage(mask);

        final int maxWidth = Math.max(reference.getWidth(), lastCompareImage.getWidth());
        final int maxHeight = Math.max(reference.getHeight(), lastCompareImage.getHeight());

        final int minWidth = Math.min(reference.getWidth(), lastCompareImage.getWidth());
        final int minHeight = Math.min(reference.getHeight(), lastCompareImage.getHeight());

        if (maxWidth != minWidth || maxHeight != minHeight)
        {
            resized = true;
            reference = ImageHelper.adaptImageSize(reference, maxWidth, maxHeight);
            lastCompareImage = ImageHelper.adaptImageSize(lastCompareImage, maxWidth, maxHeight);
            maskCopy = ImageHelper.adaptImageSize(maskCopy, maxWidth, maxHeight);

        }
        final BufferedImage maskedReference = ImageHelper.overlayMaskImage(reference, maskCopy, ImageHelper.BLACK.getRGB());
        final BufferedImage maskedCompareImage = ImageHelper.overlayMaskImage(lastCompareImage, maskCopy, ImageHelper.BLACK.getRGB());

        switch (algorithm.getType())
        {
        case EXACTMATCH:
            lastDifferences = ImageHelper.compareImages(maskedReference, maskedCompareImage);
            break;

        case COLORFUZZY:
            lastDifferences = ImageHelper.colorFuzzyCompare(maskedReference, maskedCompareImage, algorithm.getColorTolerance());
            break;

        case PIXELFUZZY:
            lastDifferences = ImageHelper.fuzzyCompare(maskedReference, maskedCompareImage, algorithm.getColorTolerance(),
                    algorithm.getPixelTolerance(), algorithm.getFuzzyBlockSize());
            break;
        }

        if(lastDifferences == null){
            Assert.fail("The dimensions of the two images don't match!");
        }

        if (lastDifferences.length != 0)
        {
            return false;
        }

        return true;
    }

    public boolean isEqual(final BufferedImage compareImage, final MaskImage mask, final ComparisonAlgorithm algorithm)
    {
        return isEqual(compareImage, mask.getMask(), algorithm);
    }

    public BufferedImage getMarkedImageWithAMarker(final int markingSizeX, final int markingSizeY)
    {
        return ImageHelper.markDifferencesWithAMarker(lastCompareImage, lastDifferences, markingSizeX, markingSizeY);
    }

    public BufferedImage getMarkedImageWithBoxes(final int markingSizeX, final int markingSizeY)
    {
        return ImageHelper.markDifferencesWithBoxes(lastCompareImage, lastDifferences, markingSizeX, markingSizeY);
    }

    public BufferedImage getDifferenceImage()
    {
        if (lastDifferences.length == 0)
            return null;

        // create a difference picture based on reference and paint it black
        BufferedImage difference = ImageHelper.createPlainImage(reference, Color.BLACK);

        // mark differences in greyscale
        Color greyscale;
        int x, y, diffColor;
        double pixelColorDiff;
        for (Point point : lastDifferences)
        {
            x = point.x;
            y = point.y;
            pixelColorDiff = ImageHelper.calculatePixelRGBDiff(reference.getRGB(x, y), lastCompareImage.getRGB(x, y));

            diffColor = (int) Math.round(255 * pixelColorDiff);
            greyscale = new Color(diffColor, diffColor, diffColor, 255);
            difference.setRGB(x, y, greyscale.getRGB());
        }

        // draw borders on the differences if compared images differed in size
        if (resized)
            difference = ImageHelper.markImageBorders(difference, initialWidth, initialHeight);

        return difference;
    }
}
