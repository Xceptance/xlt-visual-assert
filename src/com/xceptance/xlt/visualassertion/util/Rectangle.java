package com.xceptance.xlt.visualassertion.util;

/**
 * Represents a mask in form of a rectangle given by width and height.
 */
public class Rectangle
{
    private int x;
    private int y;
    private int width;
    private int height;

    /**
     * Constructs a Rectangle with the given width and height. The coordinates are set to [0,0]
     * @param width Width of the rectangle
     * @param height Height of the rectangle
     */
    public Rectangle(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Constructs a Rectangle with the given coordinates x and y, as well as the given width and height.
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width of the rectangle
     * @param height Height of the rectangle
     */
    public Rectangle(int x, int y, int width, int height){
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
