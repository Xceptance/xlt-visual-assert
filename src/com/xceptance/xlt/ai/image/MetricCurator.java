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

	/***
	 * Constructor.
	 * @param tag String name of the image
	 */
	public MetricCurator(String tag) 
	{
		tagName 	= tag;
		metricList 	= new ArrayList<>();
	}

	/***
	 * Get the name of the image represented by the MetricCurator.
	 * @return tagName String name of the image
	 */
	public String getTagName()
	{
		return tagName;
	}
	
	/**
     * Save metricCurator to specified file.
     * @param fileName File name to save MetricCurator into.
     */
    public void Save(String fileName)
    {
        try 
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(this);
            out.close();
        } 
        catch (FileNotFoundException ex) 
        {
            ex.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * Load metricCurator from specified file.
     * @param fileName File name to load network from.
     * @return Returns instance of MetricCurator class with all properties initialized from file.
     */
    public MetricCurator Load(String fileName)
    {
        MetricCurator mc = null;
        try 
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            mc = (MetricCurator)in.readObject();
            in.close();
        } 
        catch (FileNotFoundException ex) 
        {
            ex.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return mc;
    }
    
    /***
     * Name tag of the image.
     */
	private final String tagName;
}
