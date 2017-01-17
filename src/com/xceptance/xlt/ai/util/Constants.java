package com.xceptance.xlt.ai.util;

/***
 * Used constants for the program. 
 * @author Thomas Volkmann 
 */
public class Constants 
{	
	// State of the network to work in training or in classification mode.
	public static boolean NETWORK_MODE = true;
	// Determine if the name of the current test case is used for folder creation or not.
	public static boolean TESTCASE_BOUND = true;
	// Name of the not attached network folder.
	public static String TESTCASE_BOUND_NAME = "unbound";
	// Desired percentage value for the self test, until this barrier is confirmed the network will 
	// still learn (use several images which where already seen)
	public static String INTENDED_PERCENTAGE_MATCH = "0.80";
	// Color will also be used for image comparison, not recommended for websides with many images
	// or consequently changing content 
	public static boolean USE_COLOR_FOR_COMPARISON = false;
	// Parameter for enabling down scaling from screenshots or not, this can drastically change 
	// the performance of the algorithm
	public static boolean USE_ORIGINAL_SIZE = false;
	// Fixed Size for the histogram creation in Metric
	public static final int BINSIZE = 10;
	// Distance from current point for grouping
	public static int THRESHOLD = 20; 
	// Points cloud minimum value
	public static int MINGROUPSIZE = 200;	
	// procedural value for the difference level, to compare images
	public static int PERCENTAGE_DIFFERENCE = 10;		
	// value for the learning algorithm allowed values are between 0.0 - 1.0
	// default is 0.2
	public static final String LEARNING_RATE = "0.2";
	// image format for saving
	public static String FORMAT = "png";
	// value for the height of the image
	public static int IMAGE_HEIGHT = 800;
	// value for the width of the image
	public static int IMAGE_WIDTH = 600;
	// allowed file extensions for loading from folder
	static final String[] EXTENSIONS = new String[]	{"jpg",	"png", "bmp" , "jpeg"};
	// time to wait until the website is loaded and the screenshot is taken
    public static final int WAITINGTIME = 1000;
    
}
