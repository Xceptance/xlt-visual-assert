package test.com.xceptance.xlt.visual;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.lang.ClassUtils;

public abstract class ImageTest
{
    protected File resolveFile(final String name)
    {
        final String path = ClassUtils.getPackageName(this.getClass()).replace(".", "/");
        return new File("src/" + path + "/" + name);
    }

    protected BufferedImage load(File f)
    {
        try
        {
            return ImageIO.read(f);
        }
        catch (IOException e)
        {
            throw new RuntimeException("File " + f.getAbsolutePath() + " not found!", e);
        }
    }

    protected void write(BufferedImage image, String fileName)
    {
        try
        {
            ImageIO.write(image, "PNG", new File("c:/temp/" + fileName));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected BufferedImage load(String f)
    {
        return load(resolveFile(f));
    }

    protected boolean imageEqual(BufferedImage img1, BufferedImage img2)
    {
        if (img1 == null && img2 == null)
            return true;

        if (img1 == null || img2 == null)
            return false;

        for (int x = 0; x < img1.getWidth(); x++)
        {
            for (int y = 0; y < img1.getHeight(); y++)
            {
                // if the RGB values of 2 pixels differ
                if (img1.getRGB(x, y) != img2.getRGB(x, y))
                {
                    return false;
                }
            }
        }

        return true;
    }

    protected BufferedImage createTestImageGradient(final Color startColor, final int r, final int g, final int b) throws IOException
    {
        final BufferedImage img = new BufferedImage(300, 13, BufferedImage.TYPE_INT_RGB);

        // white
        final Graphics graphics = img.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
        graphics.dispose();

        int sR = startColor.getRed();
        int sG = startColor.getGreen();
        int sB = startColor.getBlue();

        for (int w = 1; w < img.getWidth(); w = w + 3)
        {
            sR = sR + r;
            sG = sG + g;
            sB = sB + b;

            if (sR > 255 || sB > 255 || sG > 255)
            {
                break;
            }

            img.setRGB(w, 7, new Color(sR, sG, sB).getRGB());
        }

        return img;
    }

    protected BufferedImage createTestImage2DGradient(final Color startColor, final Color constant) throws IOException
    {
        final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

        img.setRGB(0, 0, startColor.getRGB());

        for (int w = 0; w < img.getWidth(); w++)
        {
            for (int h = 0; h < img.getWidth(); h++)
            {
                int color = 0;
                if (constant == Color.RED)
                {
                    color = new Color(0, w, h).getRGB();
                }
                else if (constant == Color.GREEN)
                {
                    color = new Color(w, 0, h).getRGB();
                }
                else if (constant == Color.BLUE)
                {
                    color = new Color(w, h, 0).getRGB();
                }
                img.setRGB(w, h, color);
            }
        }
        return img;
    }

}
