package com.xceptance.xlt.ai.image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
        
    /***
     * Name tag of the image.
     */
	private final String tagName;
}
