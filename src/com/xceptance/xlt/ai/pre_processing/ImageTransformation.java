package com.xceptance.xlt.ai.pre_processing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.xceptance.xlt.ai.core.FloatPoint;
import com.xceptance.xlt.ai.corner.Fast9;
import com.xceptance.xlt.ai.corner.FastCornersDetector;
import com.xceptance.xlt.ai.core.FeaturePoint;
import com.xceptance.xlt.ai.corner.FastCornersDetector.Algorithm;
import com.xceptance.xlt.ai.image.AverageMetric;
import com.xceptance.xlt.ai.image.Convolution;
import com.xceptance.xlt.ai.image.ConvolutionKernel;
import com.xceptance.xlt.ai.image.FastBitmap;
import com.xceptance.xlt.ai.image.PatternHelper;
import com.xceptance.xlt.ai.util.Constants;
import com.xceptance.xlt.ai.util.Helper;

/***
 * Main entry point to load, analyze and compare images. 
 * Load Images out of an folder and hold them for analyze.
 * Analyze to find the ROI's (region of interest) in the image. They are saved in groups and each point is a {@link FeaturePoint}.
 * Make use of machine learning in the {@link #computeAverageMetric} method. 
 * @author Thomas Volkmann 
 */
public class ImageTransformation 
{
	/***
	 * Constructor for first initialization, in this case the program is run for the first time.
	 * Load the images out of a folder and start the analyze with {@link #applyTransformation()}
	 * @param path String to the folder.
	 */
	public ImageTransformation(BufferedImage img, String tagName, Map<Integer, AverageMetric> averageMet, boolean trainingFlag, boolean useOriginalSize, int width, int height)
	{		
		maxSize 		= 0;
		trainingFlag 	= true;
		averMet 		= averageMet;
		this.trainingFlag 	= trainingFlag;
		process(img, tagName, useOriginalSize, width, height);
		//load(path, useOriginalSize, width, height);
		applyTransformation();
	}
	
	/***
	 * Constructor if there already is a network which can be used for further learning or comparison.
	 * Load the images out of a folder and start the analyze with {@link #applyTransformation()}
	 * If the flag is on false there is just a comparison to the already learned average metric.
	 * @param path String to the folder.
	 * @param averMetr AverageMetric which is saved in the network. 
	 * @param flag Boolean for further training or comparison.
	 */
	public ImageTransformation(String path, String tagName, boolean useOriginalSize, int width, int height)
	{
		averMet 			= new HashMap<Integer, AverageMetric>();
		maxSize 			= 0;
		//process(img, tagName, useOriginalSize, width, height);
		load(path, useOriginalSize, width, height);
		applyTransformation();
	}
	
	/***
	 * ArrayList of all pre-processed images.
	 * Get the current MetricCurator for an image. 
	 * @return pp ArrayList of MetricCurator for every image.
	 */
	public ArrayList<PreProcessing> getCurator()
	{
		return pp;
	}
	
	/***
	 * Dictionary which hold the average metric from all seen images.
	 * @return averMet Dictionary of the found average metric
	 */
	public Map<Integer, AverageMetric> getAverageMetric()
	{
		return averMet;
	}
	
	/***
	 * Compute the pattern for the network in relevance to the average metric.
	 * Provide the data for the neural network and transform the image in a way it is linear seperable.
	 * This step is important so the perceptron network can learn the patterns and distinguish the different images.
	 * @return foundPattern ArrayList of {@link PatternHelper} of all found pattern if there was more than one image in the folder.
	 */
	public ArrayList<PatternHelper> computeAverageMetric(int percentageDifference, boolean useColor, boolean useOriginalSize)
	{
		int groupSize 								= 0;
		double boundingBoxSize						= 0.0;
		double distanceMin							= 0.0;
		double distanceMax							= 0.0;
		double histoRedMean							= 1.0;
		double histoGreenMean						= 1.0;
		double histoBlueMean						= 1.0; 
		FloatPoint centerOfGravity 					= new FloatPoint(0,0);		
		boolean isEmptyFlag 						= true;
		ArrayList<PatternHelper> foundPattern 		= new ArrayList<>();
		PatternHelper pattern;
		
		// if no network was loaded
		if (!averMet.isEmpty())
		{
			isEmptyFlag = false;
		}
		
		// main loop for all images which are stored in PreProcessing
		for (int index = 0; index < pp.size(); ++index)
		{
			maxSize = pp.get(index).getMetricCurator().metricList.size();
			pattern = new PatternHelper(pp.get(index).getMetricCurator().getTagName());
			for (int inde = 0; inde < averMet.keySet().size(); ++inde)
			{
				pattern.addElementToPattern(0);
			}			
			// loop to create the pattern
			// also compare the pattern in respective to found or not found
			for (int ind = 0; ind < maxSize; ++ind)
			{
				int key 			= 0;
				groupSize 		 	= pp.get(index).getMetricCurator().metricList.get(ind).getGroupSize();
				boundingBoxSize 	= pp.get(index).getMetricCurator().metricList.get(ind).getBoundingBoxDistance();
				distanceMin 		= pp.get(index).getMetricCurator().metricList.get(ind).getMinDistanceToZero();
				distanceMax 		= pp.get(index).getMetricCurator().metricList.get(ind).getMaxDistanceToZero();		
				centerOfGravity.Add(pp.get(index).getMetricCurator().metricList.get(ind).getCenterOfGravity());
				
				if (useColor)
				{
					histoRedMean		= pp.get(index).getMetricCurator().metricList.get(ind).getImageStatistic().getHistogramRed().getMean();
					histoGreenMean		= pp.get(index).getMetricCurator().metricList.get(ind).getImageStatistic().getHistogramGreen().getMean();
					histoBlueMean		= pp.get(index).getMetricCurator().metricList.get(ind).getImageStatistic().getHistogramBlue().getMean();
				}
	
				Iterator<Integer> iter = averMet.keySet().iterator();
				
				// first run take all inputs as new average metric and set the pattern to 1s for learning and weighting
				if (isEmptyFlag)
				{
					averMet.put(ind, new AverageMetric(groupSize, boundingBoxSize, distanceMin, distanceMax, centerOfGravity, histoRedMean, histoGreenMean, histoBlueMean));
					recognizeFlag = true;
				}
				// if training is activated and the average metric is not empty analyze the new metric in relevance to the average metric					
				else if (trainingFlag && !isEmptyFlag)
				{						
					while (iter.hasNext())
					{
						// check for the independent metrics (in respect of screen coordinates) which are groupSize and BoundingBox 
						// and if the new metric match the average metric (with a percentage difference) the average metric get an update
						// and mark them as recognize in the pattern indicated as 1
						key = iter.next();
						if (Helper.isInRange(averMet.get(key).getAverageGroupSize(), groupSize, percentageDifference) && 
							Helper.isInRange(averMet.get(key).getAverageBoundingBoxSize(),boundingBoxSize , percentageDifference) 
							&&
							Helper.isInRange(averMet.get(key).getAverageHistogramRedMean(), histoRedMean, percentageDifference) &&
							Helper.isInRange(averMet.get(key).getAverageHistogramGreenMean(), histoGreenMean, percentageDifference) &&
							Helper.isInRange(averMet.get(key).getAverageHistogramBlueMean(), histoBlueMean, percentageDifference)
							)
						{								
							averMet.get(key).update(groupSize, boundingBoxSize, distanceMin, distanceMax, centerOfGravity, histoRedMean, histoGreenMean, histoBlueMean);
							recognizeFlag = true;
							break;
						}
					}												
					// expand the pattern and update average metric for further comparing, set the pattern to 0 for not recognized 
					if (!recognizeFlag)
					{
						averMet.put(key + 1, new AverageMetric(groupSize, boundingBoxSize, distanceMin, distanceMax, centerOfGravity, histoRedMean, histoGreenMean, histoBlueMean));
						recognizeFlag = false;
					}		
				}
				// alternative path if the network has already learned and now just compare the new metrics to the found average metric
				else 
				{
					while (iter.hasNext())
					{
						key = iter.next();
						if (Helper.isInRange(averMet.get(key).getAverageGroupSize(), groupSize, percentageDifference) && 
							Helper.isInRange(averMet.get(key).getAverageBoundingBoxSize(), boundingBoxSize , percentageDifference) 
							&& 
							Helper.isInRange(averMet.get(key).getAverageHistogramRedMean(), histoRedMean, percentageDifference)&&
							Helper.isInRange(averMet.get(key).getAverageHistogramGreenMean(), histoGreenMean, percentageDifference) &&
							Helper.isInRange(averMet.get(key).getAverageHistogramBlueMean(), histoBlueMean, percentageDifference)
							)
						{
							recognizeFlag = true;
							break;
						}						
					}
				}
				// set the pattern in respect to the recognizeFlag
				if (pattern.getSize() < averMet.keySet().size())
				{
					pattern.addElementToPattern(recognizeFlag ? 1 : 0);
				}
				else
				{
					pattern.setElement(key, recognizeFlag ? 1 : 0);
				}
				recognizeFlag = false;								
			} 
			groupSize 			= 0;
			boundingBoxSize 	= 0.0;
			distanceMin 		= 0.0;
			distanceMax 		= 0.0;
			centerOfGravity.x 	= 0;
			centerOfGravity.y 	= 0;
			histoRedMean		= 1.0;
			histoGreenMean		= 1.0;
			histoBlueMean		= 1.0; 
			recognizeFlag 		= false;
			isEmptyFlag 		= false;
			foundPattern.add(pattern);			
		}
		return foundPattern;
	}
	
	/***
	 * Transformation of the image for further use.
	 * First a {@link Convolution} with a Laplace and Gaussian kernel is performed.
	 * Furthermore the edge detection via {@link FastCornersDetector} with {@link Fast9} is performed.
	 * Next the found corners get sorted after x values and the last step is realized with {@link PreProcessing}.	 
	 */
	private void applyTransformation()
	{
		Convolution conv = new Convolution(ConvolutionKernel.LaplacianOfGaussian);
		FastCornersDetector fcd = new FastCornersDetector(Algorithm.FAST_9);		
		fcd.setSuppression(false);
		List<FeaturePoint> tempList = null;
		for (FastBitmap element : pictureList)
		{
			long startTime = System.nanoTime();
			conv.applyInPlace(element);			
			// apply the fast corner detection to the image
			tempList = fcd.ProcessImage(element);
			// sorting with the comparator set in FeaturePoints, sorting order is ascending x values
			Collections.sort(tempList, new FeaturePoint());			
			pp.add(new PreProcessing(tempList, element));			
			long estimatedTime = System.nanoTime() - startTime;
			System.out.println((double)estimatedTime / 1000000000.0);
		}	
	}

	
	/***
	 * Load all images out of a given folder path..
	 * @param path String full path name to folder
	 */
	private void load(String path, boolean useOriginalSize, int width, int height)
	{
		if (useOriginalSize)
		{
			pictureList = Helper.loadAllImages_FastBitmap(path);
		}
		else
		{
			pictureList = Helper.loadAllImagesScaled_FastBitmap(path, height, width);
		}

		if (pictureList != null)
		{			
			Helper.setImageParameter(pictureList.get(0).getWidth(), pictureList.get(0).getHeight());			
			pp = new ArrayList<>();
			recognizeFlag = false;		
		}
		else
		{
			throw new NullPointerException("folder not found or it was empty");
		}
	}
	
	private void process(BufferedImage img, String tagName, boolean useOriginalSize, int width, int height)
	{
		pictureList = new ArrayList<>();
		if (useOriginalSize)
		{
			pictureList.add(Helper.imageToFastBitmap(img, tagName, 1));
		}
		else
		{
			pictureList.add(Helper.imageToFastImageScaled(img, tagName, 1, height, width));
		}
		
		Helper.setImageParameter(width, height);
		pp = new ArrayList<>();
		recognizeFlag = false;	
	}
	
	/***
	 * List of all loaded images, saved as FastBitmap.
	 */
	private ArrayList<FastBitmap> pictureList;
	
	/***
	 * Maximum size of the pattern.
	 */
	private int maxSize;
	
	/***
	 * Boolean flag for recognition.
	 */
	private boolean recognizeFlag;
	
	/***
	 * Learning modus enabled or disabled. If disabled it will compare to already learned pattern.
	 */
	private boolean trainingFlag;
	
	/***
	 * List of rehashed images.
	 */
	private ArrayList<PreProcessing> pp;
	
	/**
	 * Representation of average metric.
	 */
	private Map<Integer, AverageMetric> averMet;
}
