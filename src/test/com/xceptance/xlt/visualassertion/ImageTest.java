package test.com.xceptance.xlt.visualassertion;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ClassUtils;
import org.junit.After;
import org.junit.Assert;

import com.xceptance.xlt.visualassertion.ImageComparison;
import com.xceptance.xlt.visualassertion.ImageComparison.Algorithm;

public abstract class ImageTest
{
	final ArrayList<File> tempFiles = new ArrayList<File>();

	protected File getTempFile(final String prefix, final String suffix)
	{
		final File f = new File(FileUtils.getTempDirectoryPath(), prefix + "-" + UUID.randomUUID().toString() + suffix);	
		tempFiles.add(f);

		return f;
	}

	protected File resolveFile(final String name)
	{
		final String path = ClassUtils.getPackageName(this.getClass()).replace(".", "/");
		return new File( "src/" + path + "/" + name);
	}

	protected BufferedImage loadImage(final String name) throws IOException
	{
		final BufferedImage p1 = ImageIO.read(resolveFile(name));
		return p1;
	}
	protected BufferedImage loadImage(final File f) throws IOException
	{
		final BufferedImage p1 = ImageIO.read(f);
		return p1;
	}
	/**
	 * Save the image into a temp file
	 * @param img the image to write
	 * @return the file that was written
	 * @throws IOException
	 */
	protected File saveImage(final BufferedImage img) throws IOException
	{
		final File f = this.getTempFile("baseline", ".png");
		ImageIO.write(img, "PNG", f);
		tempFiles.add(f);
		return f;
	}	

	public class TestCompare
	{
		private Algorithm algorithm;

		private  BufferedImage baselineImage;
		private BufferedImage toCompareToImage;

		private File maskFile;
		private File markedFileAsResult;
		private File differenceFile;

		private boolean result;

		private int markingSizeX = 10;
		private int markingSizeY = 10;

		private final int fuzzyBlockDimension = 10;
		private double colorTolerance = 0.1;
		private final double pixTolerance = 0.1;
		private final boolean trainingMode = false;
		private final boolean closeMask = false;
		private final int structElementWidth = 10;
		private final int structElementHeight = 10;
		private final boolean differenceImage = false;

		private boolean wasExecuted = false;

		public TestCompare()
		{

		}

		private TestCompare execute()
		{
			if (wasExecuted)
			{
				return this;
			}

			wasExecuted = true;

			try 
			{
				markedFileAsResult = getTempFile("marked", ".png");

				final ImageComparison imagecomparison = new ImageComparison(algorithm, markingSizeX, markingSizeY, fuzzyBlockDimension, colorTolerance, pixTolerance, trainingMode, closeMask, structElementWidth, structElementHeight, differenceImage);
				result = imagecomparison.isEqual(baselineImage, toCompareToImage, maskFile, markedFileAsResult, differenceFile);	
			}
			catch(final IOException ioe)
			{
				Assert.fail(ioe.getMessage());
			}

			return this;
		}

		public TestCompare match(final String baselineImagePath)
		{
			this.algorithm = Algorithm.MATCH;
			try
			{
				this.baselineImage = loadImage(baselineImagePath);
			}
			catch (final IOException e)
			{
				Assert.fail(e.getMessage());
			}

			return this;
		}
		public TestCompare colorFuzzy(final String baselineImagePath)
		{
			this.algorithm = Algorithm.COLORFUZZY;
			try
			{
				this.baselineImage = loadImage(baselineImagePath);
			}
			catch (final IOException e)
			{
				Assert.fail(e.getMessage());
			}

			return this;
		}
		public TestCompare colorFuzzy(final File baselineFile)
		{
			this.algorithm = Algorithm.COLORFUZZY;
			try
			{
				this.baselineImage = loadImage(baselineFile);
			}
			catch (final IOException e)
			{
				Assert.fail(e.getMessage());
			}

			return this;
		}
		public TestCompare to(final String toCompareToImagePath)
		{
			try
			{
				this.toCompareToImage = loadImage(toCompareToImagePath);
			}
			catch (final IOException e)
			{
				Assert.fail(e.getMessage());
			}
			return this;
		}
		public TestCompare to(final File toCompareFile)
		{
			try
			{
				this.toCompareToImage = loadImage(toCompareFile);
			}
			catch (final IOException e)
			{
				Assert.fail(e.getMessage());
			}
			return this;
		}
		public TestCompare isEqual()
		{
			execute();
			Assert.assertTrue(result);
			return this;
		}
		public TestCompare isNotEqual()
		{
			execute();
			Assert.assertFalse(result);
			return this;
		}
		public TestCompare hasMarking(final String markFilePath)
		{
			try
			{
				Assert.assertTrue(compareFiles(markFilePath, markedFileAsResult));
			}
			catch (final IOException e)
			{
				Assert.fail("Problems reading images: " + e.getMessage());
			}
			return this;
		}
		public TestCompare hasNoMarking()
		{
			Assert.assertFalse(markedFileAsResult.exists());
			return this;
		}
		public TestCompare mark(final int x, final int y)
		{
			this.markingSizeX = x;
			this.markingSizeY = y;
			return this;
		}

		public TestCompare colorDifference(final double diff)
		{
			this.colorTolerance = diff;
			return this;
		}
	}



	protected File createTestImageGradient(final Color startColor, final int r, final int g, final int b) throws IOException
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

			if (sR > 255 || sB > 255|| sG > 255)
			{
				break;
			}

			img.setRGB(w, 7, new Color(sR, sG, sB).getRGB());
		}

		return saveImage(img);
	}

	protected File createTestImage2DGradient(final Color startColor) throws IOException
	{
		final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

		img.setRGB(0, 0, startColor.getRGB());

		for (int w = 1; w < img.getWidth(); w++)
		{
			for (int h = 1; h < img.getWidth(); h++)
			{
				img.setRGB(w, h, new Color(w, h, 0).getRGB());
			}
		}
		return saveImage(img);
	}

	protected Color addColors(final Color a, final Color b)
	{
		final Color c = new Color(a.getRed() + b.getRed(), a.getGreen() + b.getGreen(), a.getBlue() + b.getBlue());
		System.out.println(c.toString());
		return c;
	}

	protected boolean compareFiles(final String expected, final File actual) throws IOException
	{
		return FileUtils.contentEquals(resolveFile(expected), actual);
	}

	@After
	public void cleanFiles()
	{
		for (final File f : tempFiles)
		{
			// FileUtils.deleteQuietly(f);
		}
	}
}
