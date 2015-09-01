package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.After;
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
//		test a run with the training mode without any differences between the images
		x = 0;
		setUpFileAndPicture();
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	@Test
	public void trainWithSingleDifference() throws IOException{
//		a single block is colored different and should be recognized by the training mode
		x = 1;
		setUpFileAndPicture();
		paintArea(newImage, 0, 0, 10, 10);
		fuzzyAssertBlock();
	}
	
	@Test
	public void trainWithMultipleDifferences() throws IOException{
//		two areas are colored different and should be recognized by the training mode
		x = 2;
		setUpFileAndPicture();
		paintArea(newImage, 50, 30, 23, 27);
		paintArea(newImage, 78, 83, 10, 45);
		fuzzyAssertBlock();
	}
	
	@Test
	public void trainOverMultipleRounds() throws IOException{
//		after a first round in training mode, additional differences are created to see if the training works
//		properly over multiple runs
		x = 3;
		setUpFileAndPicture();
		paintArea(newImage, 0, 0, 10, 10);
		fuzzyAssertBlock();
		paintArea(newImage, 50, 50, 25, 25);
		fuzzyAssertBlock();
	}
	
	@Test
	public void trainExactlyEqual() throws IOException {
//		test if training mode works properly for the exact pixel comparison
		x = 4;
		setUpFileAndPicture();
		paintArea(newImage, 80, 80, 1, 1);
		exactlyAssertBlock();
		paintArea(newImage, 74, 154, 7, 1);
		exactlyAssertBlock();
	}
	
	@Test
	public void trainPixelFuzzyEqual() throws IOException {
//		test if training mode works properly for the fuzzy pixel comparison
		x = 5;
		setUpFileAndPicture();
		paintArea(newImage, 48, 97, 3, 2);
		pixelFuzzyAssertBlock();		
	}
	
	@After
	public void deleteFiles() {
		outPutfile.delete();
		maskFile.delete();
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
//		method for coloring specific areas of an image to create differences
		int rgb = Color.BLUE.getRGB();
		for (int a = 0; a<width; a++){
			for (int b = 0; b<height; b++) {
				img.setRGB(x + a, y + b, rgb);
			}
		}
	}
	
	public void fuzzyAssertBlock () throws IOException {
//		this assertion block checks if the fuzzy training was completed succesfully
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	public void exactlyAssertBlock() throws IOException {
//		this assertion block checks if the exact training was completed succesfully
		Assert.assertFalse(imgCompare.exactlyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.exactlyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.exactlyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	public void pixelFuzzyAssertBlock() throws IOException {
//		this assertion block checks if the pixel fuzzy training was completed succesfully
		Assert.assertFalse(imgCompare.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
	public void setUpFileAndPicture() {
//		setup method for each test
		outPutfile = new File("test"+x+".png");
		maskFile = new File("mask"+x+".png");
		newImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		paintWhite(newImage);
	}

}
