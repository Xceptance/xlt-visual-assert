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
		public BufferedImage baselineImage;
		public BufferedImage compareImage;

		public File markedFileAsResult;

		public boolean result;

		public TestCompare compare(final Algorithm algorithm, final String baseline, final String compare) throws IOException
		{
			baselineImage = loadImage(baseline);
			compareImage = loadImage(compare);
			markedFileAsResult = getTempFile("marked", ".png");

			final ImageComparison imagecomparison = new ImageComparison(Algorithm.MATCH);
			result = imagecomparison.isEqual(baselineImage, compareImage, null, markedFileAsResult, null);	

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
			//			FileUtils.deleteQuietly(f);
		}
	}
}
