package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import VisualComparison.ImageComparison;

public class TImageComparisonTraining {
	BufferedImage reference, newImage;
	ImageComparison training = new ImageComparison (10, 10, 0.00, true);
	ImageComparison imgCompare = new ImageComparison (10, 10, 0.00, false);
	int x;
	File maskFile;
	File outPutfile;
	
	@Before
	public void setUp () {
		reference = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		paintWhite(reference);
	}
	
	@Test
	public void trainWithNoDifference() throws IOException{
		x = 0;
		setUpFileAndPicture();
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void trainWithSingleDifference() throws IOException{
		x = 1;
		setUpFileAndPicture();
		paintArea(newImage, 0, 0, 10, 10);
		fuzzyAssertBlock();
	}
	
	@Test
	public void trainWithMultipleDifferences() throws IOException{
		x = 2;
		setUpFileAndPicture();
		paintArea(newImage, 50, 30, 23, 27);
		paintArea(newImage, 78, 83, 10, 45);
		fuzzyAssertBlock();
	}
	
	@Test
	public void trainOverMultipleRounds() throws IOException{
		x = 3;
		setUpFileAndPicture();
		paintArea(newImage, 0, 0, 10, 10);
		fuzzyAssertBlock();
		paintArea(newImage, 50, 50, 25, 25);
		fuzzyAssertBlock();
	}
	
	@Test
	public void trainExactlyEqual() throws IOException {
		x = 4;
		setUpFileAndPicture();
		paintArea(newImage, 80, 80, 1, 1);
		exactlyAssertBlock();
		paintArea(newImage, 74, 154, 7, 1);
		exactlyAssertBlock();
	}
	
	@Test
	public void trainPixelFuzzyEqual() throws IOException {
		x = 5;
		setUpFileAndPicture();
		paintArea(newImage, 48, 97, 3, 2);
		pixelFuzzyAssertBlock();		
	}
	
	public void paintBlack (BufferedImage img) {
//		method for painting the images black
		int rgb = Color.BLACK.getRGB();
		for (int x = 0; x<img.getWidth(); x++) {
			for (int y = 0; y<img.getHeight(); y++) {
				img.setRGB(x, y, rgb);
			}
		}
	}
	
	public void paintWhite (BufferedImage img) {
//		method for painting the images white
		int rgb = Color.WHITE.getRGB();
		for (int x = 0; x<img.getWidth(); x++) {
			for (int y = 0; y<img.getHeight(); y++) {
				img.setRGB(x, y, rgb);
			}
		}
	}
	
	public void paintArea (BufferedImage img, int x, int y, int width, int height) {
		int rgb = Color.BLUE.getRGB();
		for (int a = 0; a<width; a++){
			for (int b = 0; b<height; b++) {
				img.setRGB(x + a, y + b, rgb);
			}
		}
	}
	
	public void fuzzyAssertBlock () throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	public void exactlyAssertBlock() throws IOException {
		Assert.assertFalse(imgCompare.exactlyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.exactlyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.exactlyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	public void pixelFuzzyAssertBlock() throws IOException {
		Assert.assertFalse(imgCompare.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	public void setUpFileAndPicture() {
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
	}

}
