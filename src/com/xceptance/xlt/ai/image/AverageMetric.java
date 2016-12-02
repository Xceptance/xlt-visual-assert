package com.xceptance.xlt.ai.image;

import java.io.Serializable;

import com.xceptance.xlt.ai.core.FloatPoint;

/***
 * Representation of the merged metrics of different Images, which where analyzed and compared.
 * This class represent one found group and the comparison in an Image and in reference to all other found groups.
 * Through the possibility to update and weight the seen data this class represent a part of machine learning.
 * @author Thomas Volkmann
 *
 */
public class AverageMetric implements Serializable
{
	/**
	 * Auto generated serial number.
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Constructor for the average metric.
	 * @param GroupSize Size of the found group. 
	 * @param BoundingBox Amount of distance between the two points which are at the opposite side of the cloud.
	 * @param DistMin Distance from the point closest to coordinate origin (0.0 Screen coordinates)
	 * @param DistMax Distance from the point farthest away from coordinate origin (0.0 Screen coordinates)
	 * @param CenterOfGrav Average center of all group elements.
	 */
	public AverageMetric(int GroupSize, double BoundingBox, double DistMin, double DistMax, FloatPoint CenterOfGrav, double histoRedMean, double histoGreenMean, double histoBlueMean)
	{
		averageGroupSize 			= GroupSize;
		averageBoundingBoxSize		= BoundingBox;
		averageDistanceMin			= DistMin;
		averageDistanceMax			= DistMax;
		averageCenterOfGravity 		= CenterOfGrav;
		averageHistoRedMean			= histoRedMean;
		averageHistoGreenMean		= histoGreenMean;
		averageHistoBlueMean		= histoBlueMean;
		itemCounter 				= 2;
	}
	
	/***
	 * Compute the average value in respective the already seen data. 
	 * @param GroupSize Size of the found group. 
	 * @param BoundingBox Amount of distance between the two points which are at the opposite side of the cloud.
	 * @param DistMin Distance from the point closest to coordinate origin (0.0 Screen coordinates)
	 * @param DistMax Distance from the point farthest away from coordinate origin (0.0 Screen coordinates)
	 * @param CenterOfGrav Average center of all group elements.
	 */
	public void update(int GroupSize, double BoundingBox, double DistMin, double DistMax, FloatPoint CenterOfGrav, double histoRedMean, double histoGreenMean, double histoBlueMean)
	{		
		averageGroupSize 			+= GroupSize;
		averageBoundingBoxSize		+= BoundingBox;
		averageDistanceMin			+= DistMin;
		averageDistanceMax			+= DistMax;
		averageHistoRedMean			+= histoRedMean;
		averageHistoGreenMean		+= histoGreenMean;
		averageHistoBlueMean		+= histoBlueMean;
		averageCenterOfGravity.Add(CenterOfGrav);
		
		averageGroupSize 			/= itemCounter;		
		averageBoundingBoxSize		/= itemCounter;
		averageDistanceMin			/= itemCounter;
		averageDistanceMax			/= itemCounter;
		averageHistoRedMean			= (histoRedMean   == 0 ? averageHistoRedMean : averageHistoRedMean/itemCounter);
		averageHistoGreenMean		= (histoGreenMean == 0 ? averageHistoGreenMean : averageHistoGreenMean/itemCounter);
		averageHistoBlueMean		= (histoBlueMean  == 0 ? averageHistoBlueMean : averageHistoBlueMean/itemCounter);
		averageCenterOfGravity.Divide(itemCounter);		
	}
	
	/***
	 * Get the average group size.
	 * @return averageGroupSize.
	 */
	public int getAverageGroupSize()
	{
		return averageGroupSize;
	}
	
	/***
	 * Get the average bounding box.
	 * @return averageBoundingBoxSize.
	 */
	public double getAverageBoundingBoxSize()
	{
		return averageBoundingBoxSize;
	}
	
	/***
	 * Get the average distance minimum.
	 * @return averageDistanceMin.
	 */
	public double getAverageDistanceMin()
	{
		return averageDistanceMin;
	}

	/***
	 * Get the average distance maximum.
	 * @return averageDistanceMax.
	 */
	public double getAverageDistanceMax() 
	{
		return averageDistanceMax;
	}

	/***
	 * Get the average center of the group.
	 * @return averageCenterOfGravity.
	 */
	public FloatPoint getAverageCenterOfGravity() 
	{
		return averageCenterOfGravity;
	}
		
	/***
	 * Get the average mean histogram from all red values.
	 * @return averageHistoRedMean.
	 */
	public double getAverageHistogramRedMean()
	{
		return averageHistoRedMean;
	}
	
	/***
	 * Get the average mean histogram from all green values.
	 * @return averageHistoRedMean.
	 */
	public double getAverageHistogramGreenMean()
	{
		return averageHistoGreenMean;
	}

	/***
	 * Get the average mean histogram from all blue values.
	 * @return averageHistoRedMean.
	 */
	public double getAverageHistogramBlueMean()
	{
		return averageHistoBlueMean;
	}
	
	/***
	 * Average group size value in int.
	 */
	private int averageGroupSize;
	
	/***
	 * Average bounding box size.
	 */
	private double averageBoundingBoxSize;

	/***
	 * Average minimum distance to coordinate origin (0.0 screen coordinates).
	 */
	private double averageDistanceMin;
	
	/***
	 * Average maximum distance to coordinate origin (0.0 screen coordinates).
	 */
	private double averageDistanceMax;
	
	/***
	 * Average center point of the group.
	 */
	private FloatPoint averageCenterOfGravity;
	
	/***
	 * Average histogram mean value from red elements.
	 */
	private double averageHistoRedMean;
	
	/***
	 * Average histogram mean value from green elements.
	 */
	private double averageHistoGreenMean;
	
	/***
	 * Average histogram mean value from blue elements.
	 */
	private double averageHistoBlueMean;
	
	/***
	 * Value for dividing and create the average value.
	 */
	private int itemCounter;	
}
