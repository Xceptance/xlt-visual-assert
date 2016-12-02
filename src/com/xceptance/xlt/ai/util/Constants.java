package com.xceptance.xlt.ai.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/***
 * Used constants for the program. 
 * @author Thomas Volkmann 
 */
public class Constants 
{
	// Desired percentage value for the self test, until this barrier is confirmed the network will 
	// still learn (use several images which where already seen)
	public static final String INTENDED_PERCENTAGE_MATCH = "0.80";
	// Color will also be used for image comparison, not recommended for websides with many images
	// or consequently changing content 
	public static final boolean USE_COLOR_FOR_COMPARISON = false;
	// Parameter for enabling down scaling from screenshots or not, this can drastically change 
	// the performance of the algorithm
	public static final boolean USE_ORIGINAL_SIZE = false;
	// Fixed Size for the histogram creation in Metric
	public static final int BINSIZE = 10;
	// Distance from current point for grouping
	public static int THRESHOLD = 20; 
	// Points cloud minimum value
	public static int MINGROUPSIZE = 200;	
	// procedural value for the difference level, to compare images
	public static final int PERCENTAGE_DIFFERENCE = 10;		
	// value for the learning algorithm allowed values are between 0.0 - 1.0
	// default is 0.2
	public static final String LEARNING_RATE = "0.2";
	// working directory	
	public static final Path CURRENT_RELATIVE_PATH = Paths.get("");
	// absolute Path for string concatenation 
	public static final String ABSOLUTE_PATH = CURRENT_RELATIVE_PATH.toAbsolutePath().toString();
	// image format for saving
	public static final String FORMAT = "png";
	// value for the height of the image
	public static final int IMAGE_HEIGHT = 800;
	// value for the width of the image
	public static final int IMAGE_WIDTH = 800; 
	static final String[] EXTENSIONS = new String[]	{"jpg",	"png", "bmp" , "jpeg"};
}
