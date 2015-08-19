import java.io.*;
import java.util.HashMap;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import net.coobird.thumbnailator.Thumbnails;

public class ImageComparison {
	private BufferedImage imgOut = null;
	private int pixelPerBlockX;
	private int pixelPerBlockY;
	private double threshold;

	public ImageComparison(int pixelPerBlockX, int pixelPerBlockY, double threshold) {
		this.pixelPerBlockX = pixelPerBlockX;
		this.pixelPerBlockY = pixelPerBlockY;
		this.threshold = threshold;
	}
	
	public boolean exactlyEqual(String path1, String path2, String pathOut) throws IOException {
		return exactlyEqual(ImageIO.read(new File(path1)), ImageIO.read(new File(path2)), pathOut);
	}
	
	public boolean exactlyEqual(File file1, File file2, String pathOut) throws IOException {
		return fuzzyEqual(ImageIO.read(file1), ImageIO.read(file2), pathOut);
	}
	
	public boolean exactlyEqual(Image img1, Image img2, String pathOut) throws IOException {
		return fuzzyEqual(imageToBufferedImage(img1), imageToBufferedImage(img2), pathOut);
	}
	
	public boolean fuzzyEqual(String path1, String path2, String pathOut) throws IOException {
		return fuzzyEqual(ImageIO.read(new File(path1)), ImageIO.read(new File(path2)), pathOut);
	}

	public boolean fuzzyEqual(File file1, File file2, String pathOut) throws IOException {
		return fuzzyEqual(ImageIO.read(file1), ImageIO.read(file2), pathOut);
	}
	
	public boolean fuzzyEqual(Image img1, Image img2, String pathOut) throws IOException {
		return fuzzyEqual(imageToBufferedImage(img1), imageToBufferedImage(img2), pathOut);
	}
	
	public boolean pixelFuzzyEqual (BufferedImage img1, BufferedImage img2, String pathOut) throws IOException {
		/*Method for pixel-based fuzzy comparison*/
		
		boolean equal = true;
		if ((img1.getWidth() != img2.getWidth()) || (img1.getHeight() != img2.getHeight())) {
			img2 = adaptImageSize(img1,img2);
		}
		
		imgOut = imageToBufferedImage(img2);

		
		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
		int rgbRed = (255 << 24) | (255 << 16) | (0 << 8) | 0; 						//
		for (int x = 0; x<imagewidth; x++) {
			for(int y = 0;y<imageheight; y++) {
				if (calculatePixelRgbDiff(x, y, img1, img2) > threshold) {
					imgOut.setRGB(x, y, rgbRed);									//Paint it red
					equal = false;
				}
			}
		}
		
		if (!equal) {
			if(pathOut != null && !pathOut.isEmpty())
				saveImage(imgOut,pathOut);
		}
		return equal;
	}

	public boolean exactlyEqual (BufferedImage img1, BufferedImage img2, String pathOut) throws IOException {
		/*Method for the exact comparison of two images*/
		//img1: reference Image, img2: screenshot
		boolean exactlyEqual = true;
		if ((img1.getWidth() != img2.getWidth()) || (img1.getHeight() != img2.getHeight())) {
			img2 = adaptImageSize(img1,img2);
		}
		
		
		imgOut = imageToBufferedImage(img2);
	
		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
		int rgbRed = (255 << 24) | (255 << 16) | (0 << 8) | 0; 			
		for (int x = 0; x<imagewidth; x++) {							
			for(int y = 0;y<imageheight; y++) {
					if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
						exactlyEqual = false;														
						imgOut.setRGB(x, y, rgbRed);					
				}
			}
		}
		if (!exactlyEqual) {
			if(pathOut != null && !pathOut.isEmpty())
				saveImage(imgOut,pathOut);
		}
		
		return exactlyEqual;
	}
	
	public boolean fuzzyEqual(BufferedImage img1, BufferedImage img2, String pathOut) throws IOException {
		/*Method for the regular fuzzy comparison*/
		
		boolean fuzzyEqual = true;
		if ((pixelPerBlockX == 1) && (pixelPerBlockY == 1)) {
			if (threshold == 0.00) {
				return exactlyEqual(img1, img2, pathOut);
			}
			else {
				return pixelFuzzyEqual(img1, img2, pathOut);
			}
			
		}
		if ((img1.getWidth() != img2.getWidth()) || (img1.getHeight() != img2.getHeight())) {
			img2 = adaptImageSize(img1,img2);
		}

		imgOut = img2;
		Graphics2D outImgGraphics = imgOut.createGraphics();
		outImgGraphics.setColor(Color.RED);
		
		int subImageHeight;
		int subImageWidth;
		
		int imagewidth = img1.getWidth();
		int imageheight = img1.getHeight();
//		String debug;

		int blocksx = (int) Math.ceil((float) imagewidth
				/ (float) pixelPerBlockX);
		int blocksy = (int) Math.ceil((float) imageheight
				/ (float) pixelPerBlockY);
		
		for (int y = 0; y < blocksy; y++) {
			for (int x = 0; x < blocksx; x++) {
				subImageWidth=calcPixSpan(pixelPerBlockX,x,imagewidth);
				subImageHeight=calcPixSpan(pixelPerBlockY,y,imageheight);

				BufferedImage sub1 = img1.getSubimage(x * pixelPerBlockX, y * pixelPerBlockY,
									subImageWidth, subImageHeight);
				BufferedImage sub2 =  img2.getSubimage(x * pixelPerBlockX, y * pixelPerBlockY,
						subImageWidth, subImageHeight);
				double[] avgRgb1 = calculateAverageRgb(sub1);
				double[] avgRgb2 = calculateAverageRgb(sub2);
				
				if (getRgbDifference(avgRgb1, avgRgb2) > threshold) {
					outImgGraphics.drawRect(x * pixelPerBlockX, y * pixelPerBlockY,
							pixelPerBlockX - 1, pixelPerBlockY - 1);
					fuzzyEqual = false;
				}
			}
		}

		if (!fuzzyEqual) {
			if(pathOut != null && !pathOut.isEmpty())
				saveImage(imgOut,pathOut);
		}
		
		return fuzzyEqual;
	}

	private int calcPixSpan(int pixelPerBlock, int n, int overallSpan) {
		if (pixelPerBlock * (n + 1) > overallSpan)
			return overallSpan % pixelPerBlock;
		else
			return pixelPerBlock;
	}
	
	private BufferedImage adaptImageSize(BufferedImage img1, BufferedImage img2) throws IOException {
		int scalePixelWidth;
		int scalePixelHeight;		
		//System.out.println("1 Real Width:" +img1.getWidth() + " Real Height:" +img1.getHeight());
		//System.out.println("2 Real Width:" +img2.getWidth() + " Real Height:" +img2.getHeight());

		if(((float)img2.getWidth()/(float)img1.getWidth()) < ((float)img2.getHeight()/(float)img1.getHeight())){
			scalePixelWidth = img1.getWidth();
			scalePixelHeight = (int) (img2.getHeight() * Math.ceil((float)img1.getWidth()/(float)img2.getWidth()));
			//System.out.println("If : Scale Width:" +scalePixelWidth + " Scale Height:" +scalePixelHeight);
		}else {
			scalePixelHeight = img1.getHeight();
			scalePixelWidth = (int) (img2.getWidth() * Math.ceil((float)img1.getHeight()/(float)img2.getHeight()));
			//System.out.println("Else: Scale Width:" +scalePixelWidth + " Scale Height:" +scalePixelHeight);
		}
		//System.out.println("1 Real Width:" +img1.getWidth() + " Real Height:" +img1.getHeight());
		//System.out.println("2 Real Width:" +Thumbnails.of(img2).size(scalePixelWidth, scalePixelHeight).asBufferedImage().getWidth() + " Real Height:" +Thumbnails.of(img2).size(scalePixelWidth, scalePixelHeight).asBufferedImage().getHeight());

		return Thumbnails.of(img2).size(scalePixelWidth, scalePixelHeight).asBufferedImage();
	}
	
	private double calculatePixelRgbDiff(int x, int y, BufferedImage img1, BufferedImage img2) {
		double maxDifference = 3 * 255;
		Color color1 = new Color(img1.getRGB(x, y));
		Color color2 = new Color(img2.getRGB(x, y));
		double difference = Math.abs(color1.getBlue() - color2.getBlue())
							+ Math.abs(color1.getRed() - color2.getRed())
							+ Math.abs(color1.getGreen() - color2.getGreen());
		
//		double difference = Math.abs(img1.getRGB(x, y) - img2.getRGB(x, y));
		return difference/maxDifference;
	}
	
	private double[] calculateAverageRgb (BufferedImage img) {
		double[] averageRgb = {0, 0, 0};
		int imageHeight = img.getHeight();
		int imageWidth = img.getWidth();
		
		for (int y = 0; y<imageHeight; y++) {
			for (int x = 0; x<imageWidth; x++) {
				Color color = new Color (img.getRGB(x, y));
				averageRgb[0] = averageRgb[0] + (double) color.getRed();
				averageRgb[1] = averageRgb[1] + (double) color.getGreen();
				averageRgb[2] = averageRgb[2] + (double) color.getBlue();
			}
		}
		
		double pixels = imageWidth * imageHeight;
		averageRgb[0] = averageRgb[0] / pixels;
		averageRgb[1] = averageRgb[1] / pixels;
		averageRgb[2] = averageRgb[2] / pixels;
		
		
		return averageRgb;
	}
	
	private double getRgbDifference(double [] Rgb1, double [] Rgb2) {
		double maxDiff = 3 * 255;
		double diff = Math.abs(Rgb1[0] - Rgb2[0]) +
					Math.abs(Rgb1[1] - Rgb2[1]) +
					Math.abs(Rgb1[2] - Rgb2[2]);
		
		return diff/maxDiff;
	}
	
	private BufferedImage imageToBufferedImage(Image img) {
		BufferedImage bi = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = bi.createGraphics();
		g2.drawImage(img, null, null);
		return bi;
	}

	private void saveImage(Image img, String filename) {
		
		BufferedImage bi = imageToBufferedImage(img);
		File f = new File(filename);
		try {
			ImageIO.write(bi,"png", f);
		}
		catch (IOException io) {
		}
	}
}
