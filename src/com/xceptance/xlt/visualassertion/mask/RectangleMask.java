package com.xceptance.xlt.visualassertion.mask;

/**
 * Represents a mask in form of a rectangle given by width and height.
 */
public class RectangleMask
{
    private int width;

    private int height;

    public RectangleMask(int width, int height)
    {
        this.width = width;
        this.height = height;

    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
}
