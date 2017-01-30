package com.xceptance.xlt.ai.image;

import java.io.Serializable;
import java.util.ArrayList;

/***
 * Helper class for storing the found pattern.
 * 
 * @author Thomas Volkmann
 *
 */
public class PatternHelper implements Serializable
{
	/**
	 * Auto generated serial number.
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Constructor.
	 * @param tagName String name of the image.
	 */
	public PatternHelper(String tagName)
	{
		this.tagName = tagName;
		patternList = new ArrayList<>();		
	}
	
	/***
	 * Add one element to the pattern Array.
	 * @param element int value of the pattern.
	 */
	public void addElementToPattern(int element)
	{
		patternList.add(element);
	}
	
	/***
	 * Secure the size of the pattern array.
	 * @param size int size to reserve.
	 */
	public void ensureCapacitySize(int size)
	{
		patternList.ensureCapacity(size);
	}
	
	/***
	 * Get access to the pattern list.
	 * @return patternList ArrayList pattern of the image.
	 */
	public ArrayList<Integer> getPatternList()
	{
		return patternList;
	}
	
	/***
	 * Set the value of one element at a given position,
	 * @param index int value index key.
	 * @param element int value to set.
	 */
	public void setElement(int index, int element)
	{
		patternList.set(index, element);
	}
	
	/***
	 * Get one specific element of the list.
	 * @param index int value index key.
	 * @return int element.
	 */
	public int getElement(int index)
	{
		return patternList.get(index);
	}
	
	/***
	 * Size of the pattern list.
	 * @return int Size.
	 */
	public int getSize()
	{
		return patternList.size();
	}
	
	/***
	 * Get the name of the image.
	 * @return String image name.
	 */
	public String getTagName()
	{
		return tagName;
	}	
	
	/***
	 * Holds the value for the found pattern (0,1).
	 */
	private ArrayList<Integer> patternList;
	
	/***
	 * Image name.
	 */
	private String tagName;
}
