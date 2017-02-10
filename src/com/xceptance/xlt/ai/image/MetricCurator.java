package com.xceptance.xlt.ai.image;

import java.io.Serializable;
import java.util.ArrayList;

/***
 * Curator of all metrics generated from the image.   
 * @author Thomas Volkmann
 *
 */
public class MetricCurator implements Serializable
{
	public ArrayList<Metric> metricList;	

	/**
	 * Auto generated serial number.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param tag String name of the image
	 */
	public MetricCurator(String tag) 
	{
		tagName 		= tag;
		metricList 		= new ArrayList<>();
	}

	/**
	 * Get the name of the image represented by the MetricCurator.
	 * @return tagName String name of the image
	 */
	public String getTagName()
	{
		return tagName;
	}
	
	@Override
	public boolean equals(Object v) 
	{
		boolean retVal = false;

	    if (v instanceof MetricCurator)
	    {
	    	MetricCurator pH = (MetricCurator) v;
	    	retVal = pH.getTagName().hashCode() == this.getTagName().hashCode();
	    }
	    return retVal;
	}

	@Override
	public int hashCode() 
	{
		return tagName.hashCode();
	}
        
    /***
     * Name tag of the image.
     */
	private final String tagName;
}
