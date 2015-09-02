package VisualComparison.TimageComparison;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import VisualComparison.ImageComparison;

public class TImageComparisonTraining {
	static BufferedImage reference, newImage;
	ImageComparison training = new ImageComparison (10, 10, 0.00, true);
	ImageComparison imgCompare = new ImageComparison (10, 10, 0.00, false);
	File directory = org.apache.commons.lang3.SystemUtils.getJavaIoTmpDir();
	File outPutfile = new File(directory+"/test.png");
	File maskFile = new File(directory+"/mask.png");

	
	@BeforeClass
	public static void setUp () {
		reference = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
		paintWhite(reference);
		newImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
	}
	
//	test a run with the training mode without any differences between the images
	@Test
	public void trainWithNoDifference() throws IOException{
		setUpFileAndPicture();
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
//	a single block is colored different and should be recognized by the training mode
	@Test
	public void trainWithSingleDifference() throws IOException{
		setUpFileAndPicture();
		paintArea(newImage, 0, 0, 10, 10);
		fuzzyAssertBlock();
	}
	
//	two areas are colored different and should be recognized by the training mode
	@Test
	public void trainWithMultipleDifferences() throws IOException{
		setUpFileAndPicture();
		paintArea(newImage, 50, 30, 23, 27);
		paintArea(newImage, 78, 83, 10, 45);
		fuzzyAssertBlock();
	}
	
//	after a first round in training mode, additional differences are created to see if the training works
//	properly over multiple runs
	@Test
	public void trainOverMultipleRounds() throws IOException{
		setUpFileAndPicture();
		paintArea(newImage, 0, 0, 10, 10);
		fuzzyAssertBlock();
		paintArea(newImage, 50, 50, 25, 25);
		fuzzyAssertBlock();
	}
	
//	test if training mode works properly for the exact pixel comparison
	@Test
	public void trainExactlyEqual() throws IOException {
		setUpFileAndPicture();
		paintArea(newImage, 80, 80, 1, 1);
		exactlyAssertBlock();
		paintArea(newImage, 74, 154, 7, 1);
		exactlyAssertBlock();
	}
	
//	test if training mode works properly for the fuzzy pixel comparison
	@Test
	public void trainPixelFuzzyEqual() throws IOException {
		setUpFileAndPicture();
		paintArea(newImage, 48, 97, 3, 2);
		pixelFuzzyAssertBlock();		
	}
	
	@After
	public void deleteFiles() {
		outPutfile.delete();
		maskFile.delete();
	}
	
//	method for painting the images white
	public  static void  paintWhite (BufferedImage img) {
		int rgb = Color.WHITE.getRGB();
		for (int x = 0; x<img.getWidth(); x++) {
			for (int y = 0; y<img.getHeight(); y++) {
				img.setRGB(x, y, rgb);
			}
		}
	}
	
//	method for coloring specific areas of an image to create differences
	public void paintArea (BufferedImage img, int x, int y, int width, int height) {
		int rgb = Color.BLUE.getRGB();
		for (int a = 0; a<width; a++){
			for (int b = 0; b<height; b++) {
				img.setRGB(x + a, y + b, rgb);
			}
		}
	}

//	this assertion block checks if the fuzzy training was completed succesfully
	public void fuzzyAssertBlock () throws IOException {
		Assert.assertFalse(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.fuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.fuzzyEqual(reference, newImage, maskFile, outPutfile));
	}
	
//	this assertion block checks if the exact training was completed succesfully
	public void exactlyAssertBlock() throws IOException {
		Assert.assertFalse(imgCompare.exactlyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.exactlyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.exactlyEqual(reference, newImage, maskFile, outPutfile));
	}
	
//	this assertion block checks if the pixel fuzzy training was completed succesfully
	public void pixelFuzzyAssertBlock() throws IOException {
		Assert.assertFalse(imgCompare.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(training.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
		Assert.assertTrue(imgCompare.pixelFuzzyEqual(reference, newImage, maskFile, outPutfile));
	}

//	setup method for each test
	public void setUpFileAndPicture() {
		paintWhite(newImage);
	}

}
