import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ImageComparisonUnitTest {
	BufferedImage reference, newImage;
	ImageComparison imgCompare = new ImageComparison(2, 2, 0.00, false);
	int x;
	File maskFile;
	File outPutfile;
	
	@Before 
	public void setup() {
		reference = new BufferedImage(8, 10, BufferedImage.TYPE_INT_ARGB);
		paintBlack(reference);
	}

	@Test
	public void referenceMoreWidth()  throws IOException{
		x = 0;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (6, 10, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceLessWidth() throws IOException{
		x = 1;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (10, 10, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceMoreHeight() throws IOException{
		x = 2;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (8, 8, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceLessHeight() throws IOException{
		x = 3;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (8, 12, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceBothMore() throws IOException{
		x = 4;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (6, 8, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test 
	public void referenceBothLess() throws IOException{
		x = 5;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (10, 12, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceBroadFormat() throws IOException{
		x = 6;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (10, 8, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}

	public void paintBlack (BufferedImage img) {
		for (int x = 0; x<img.getWidth(); x++) {
			for (int y = 0; y<img.getHeight(); y++) {
				img.setRGB(x, y, 0);
			}
		}
	}
	
	public void paintWhite (BufferedImage img) {
		for (int x = 0; x<img.getWidth(); x++) {
			for (int y = 0; y<img.getHeight(); y++) {
				img.setRGB(x, y, Color.WHITE.getRGB());
			}
		}
	}
}
