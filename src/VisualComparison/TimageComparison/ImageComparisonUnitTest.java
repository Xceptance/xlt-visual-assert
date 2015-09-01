package VisualComparison.TimageComparison;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;


public class ImageComparisonUnitTest {
	BufferedImage reference, newImage;
	ImageComparison imgCompare = new ImageComparison(2, 2, 0.00, false);
	int x;
	File maskFile;
	File outPutfile;
	
	@Before 
	public void setup() {
//		sets up a new reference image for each test
		reference = new BufferedImage(8, 10, BufferedImage.TYPE_INT_ARGB);
		paintBlack(reference);
	}

	@Test
	public void referenceMoreWidth()  throws IOException{
//		reference image is wider than the new screenshots
		x = 0;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (6, 10, BufferedImage.TYPE_INT_ARGB);
		paintBlack(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceLessWidth() throws IOException{
//		reference image is less wide than the new screenshot
		x = 1;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (10, 10, BufferedImage.TYPE_INT_ARGB);
		paintBlack(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceMoreHeight() throws IOException{
//		reference image is higher than the new screenshot
		x = 2;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (8, 8, BufferedImage.TYPE_INT_ARGB);
		paintBlack(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceLessHeight() throws IOException{
//		reference image is less high than the new screenshot
		x = 3;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (8, 12, BufferedImage.TYPE_INT_ARGB);
		paintBlack(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceBothMore() throws IOException{
//		reference picture is wider and higher
		x = 4;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (6, 8, BufferedImage.TYPE_INT_ARGB);
		paintBlack(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test 
	public void referenceBothLess() throws IOException{
//		new screenshot is wider and higher
		x = 5;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (10, 12, BufferedImage.TYPE_INT_ARGB);
		paintBlack(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void referenceBroadFormat() throws IOException{
//		width and height of the pictures are switched
		x = 6;
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage (10, 8, BufferedImage.TYPE_INT_ARGB);
		paintBlack(newImage);
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}

	public void paintBlack (BufferedImage img) {
//		method for painting the images black
		for (int x = 0; x<img.getWidth(); x++) {
			for (int y = 0; y<img.getHeight(); y++) {
				img.setRGB(x, y, 0);
			}
		}
	}
	
	public void paintWhite (BufferedImage img) {
//		method for painting the images white
		for (int x = 0; x<img.getWidth(); x++) {
			for (int y = 0; y<img.getHeight(); y++) {
				img.setRGB(x, y, Color.WHITE.getRGB());
			}
		}
	}
}