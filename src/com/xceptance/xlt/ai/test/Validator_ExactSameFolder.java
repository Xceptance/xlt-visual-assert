package com.xceptance.xlt.ai.test;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.ai.NetworkTester;
import com.xceptance.xlt.ai.NetworkTrainer;
import com.xceptance.xlt.ai.image.PatternHelper;
import com.xceptance.xlt.ai.util.Constants;

/**
 * Test if all values in both tools are equal. If the same Folder is selected all values must be the same.
 * Validate that both tools work in an appropriate way.
 * @author tvolkmann
 *
 */
public class Validator_ExactSameFolder 
{	
	@BeforeClass
	public static void setup()
	{
		String[] argTR = 
			{
					"/home/tvolkmann/Master/30 Tage Bilder/Colehaan",
					"/home/tvolkmann/Master",
					"/home/tvolkmann/Master/xlt-visual-assert/config/ai.properties",
					"unnamed"
			};
		
		String[] argTE =
			{
					"/home/tvolkmann/Master/unnamed.network",
					"/home/tvolkmann/Master/30 Tage Bilder/Colehaan"
			};
		
		NetworkTrainer.main(argTR);
		NetworkTester.main(argTE);
	}
	
	@Test
	public void CreateTrainerAndTester() 
	{
		assertTrue(NetworkTrainer.an != null);
		assertTrue(NetworkTrainer.im != null);	
			
		assertTrue(NetworkTester.an != null);
		assertTrue(NetworkTester.im != null);
	}
	
	@Test
	public void NetworkAverageMetric()
	{	
		assertTrue(NetworkTrainer.an.getInputsCount() == NetworkTester.an.getInputsCount());
		
		int size = NetworkTrainer.an.getAverageMetric().keySet().size();
		
		for (int index = 0; index < size; ++index)
		{
			assertTrue(NetworkTrainer.an.getAverageMetric().get(index).getAverageGroupSize() == NetworkTester.an.getAverageMetric().get(index).getAverageGroupSize());
			assertTrue(NetworkTrainer.an.getAverageMetric().get(index).getAverageBoundingBoxSize() == NetworkTester.an.getAverageMetric().get(index).getAverageBoundingBoxSize());
			assertTrue(NetworkTrainer.an.getAverageMetric().get(index).getAverageHistogramRedMean() == NetworkTester.an.getAverageMetric().get(index).getAverageHistogramRedMean());
			assertTrue(NetworkTrainer.an.getAverageMetric().get(index).getAverageHistogramGreenMean() == NetworkTester.an.getAverageMetric().get(index).getAverageHistogramGreenMean());
			assertTrue(NetworkTrainer.an.getAverageMetric().get(index).getAverageHistogramBlueMean() == NetworkTester.an.getAverageMetric().get(index).getAverageHistogramBlueMean());
		}
	}
	
	@Test
	public void ActivationNetworkParameter()
	{
		assertTrue(NetworkTrainer.an.getLayer().inputsCount == NetworkTester.an.getLayer().inputsCount);
		assertTrue(NetworkTrainer.an.getLayer().getActivationNeuron().getInputCount() == NetworkTester.an.getLayer().getActivationNeuron().getInputCount());
		assertTrue(NetworkTrainer.an.threshold == NetworkTester.an.getLayer().getActivationNeuron().getThreshold());
		assertTrue(NetworkTrainer.an.getLayer().getActivationNeuron().getWeight() == NetworkTester.an.getLayer().getActivationNeuron().getWeight());		
	}
	
	@Test
	public void PatternCalculatedResult()
	{
		ArrayList<PatternHelper> patternListTrainer = NetworkTrainer.im.updateInternalPattern(NetworkTrainer.im.getAverageMetric(), NetworkTrainer.im.getCurator());
		ArrayList<PatternHelper> patternListTester  = NetworkTester.im.updateInternalPattern(NetworkTester.im.getAverageMetric(), NetworkTester.im.getCurator());
		
		for (int index = 0; index < patternListTrainer.size(); ++index)
		{
			assertTrue(NetworkTester.an.checkForRecognitionAsString(patternListTrainer.get(index).getPatternList()).equals(
					   NetworkTester.an.checkForRecognitionAsString(patternListTester.get(index).getPatternList())));
			System.out.println(NetworkTester.an.checkForRecognitionAsString(patternListTrainer.get(index).getPatternList()) +" "+ NetworkTester.an.checkForRecognitionAsString(patternListTester.get(index).getPatternList()));
		}
	}
	
	@Test
	public void CompareNeuronWeights()
	{
		int size = NetworkTester.an.getLayer().getActivationNeuron().getNeurons().size();
		
		for (int index = 0; index < size; ++index)
		{
			assertTrue(NetworkTester.an.getLayer().getActivationNeuron().getNeurons().get(index).getWeight() == NetworkTrainer.an.neurons.get(index).getWeight());
		}
	}
	
	@Test
	public void PatternComparator()	
	{
		ArrayList<PatternHelper> patternListTrainer = NetworkTrainer.im.updateInternalPattern(NetworkTrainer.im.getAverageMetric(), NetworkTrainer.im.getCurator());
		ArrayList<PatternHelper> patternListTester  = NetworkTester.im.updateInternalPattern(NetworkTester.im.getAverageMetric(), NetworkTester.im.getCurator());
		
		assertTrue(patternListTrainer != null);
		assertTrue(patternListTester  != null);
		assertTrue(patternListTrainer.size() == patternListTester.size());	
		
		int size = patternListTrainer.size();	
		PatternComparatorPatternSize(patternListTrainer, patternListTester, size);
		PatternComparatorPatternName(patternListTrainer, patternListTester, size);
		PatternComparatorPatternValue(patternListTrainer, patternListTester, size);
	}

	private void PatternComparatorPatternSize(ArrayList<PatternHelper> patternListTrainer, ArrayList<PatternHelper> patternListTester, int size)
	{
		for (int index = 0; index < size; ++index)
		{
			assertTrue(patternListTrainer.get(index).getSize() == patternListTester.get(index).getSize());		
		}	
	}

	private void PatternComparatorPatternName(ArrayList<PatternHelper> patternListTrainer, ArrayList<PatternHelper> patternListTester, int size)
	{		
		for (int index = 0; index < size; ++index)
		{
			assertTrue(patternListTrainer.get(index).getSize() == patternListTester.get(index).getSize());		
		}	
	}
	
	private void PatternComparatorPatternValue(ArrayList<PatternHelper> patternListTrainer, ArrayList<PatternHelper> patternListTester, int size)
	{	
		int patternSize = 0;
		for (int index = 0; index < size; ++index)
		{			
			assertTrue(patternListTrainer.get(index).getPatternList().size() == patternListTester.get(index).getPatternList().size());
			patternSize = patternListTrainer.get(index).getPatternList().size();
			for (int ind = 0; ind < patternSize; ++ind)
			{
				assertTrue(patternListTrainer.get(index).getPatternList().get(ind).intValue() == patternListTester.get(index).getPatternList().get(ind).intValue());  
			}
		}	
	}
}
