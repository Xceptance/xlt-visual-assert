package test.com.xceptance.xlt.visualassertion;

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

	public class TestCompare
	{
		private String baselineImagePath;
		private String toCompareToImagePath;
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
		private final double colTolerance = 0.1;
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
				baselineImage = loadImage(baselineImagePath);
				toCompareToImage = loadImage(toCompareToImagePath);
				markedFileAsResult = getTempFile("marked", ".png");

				final ImageComparison imagecomparison = new ImageComparison(algorithm, markingSizeX, markingSizeY, fuzzyBlockDimension, colTolerance, pixTolerance, trainingMode, closeMask, structElementWidth, structElementHeight, differenceImage);
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
			this.baselineImagePath = baselineImagePath;

			return this;
		}

		public TestCompare to( final String toCompareToImagePath)
		{
			this.toCompareToImagePath = toCompareToImagePath;
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
