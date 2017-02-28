package com.xceptance.xlt.ai.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.ai.NetworkTester;
import com.xceptance.xlt.ai.NetworkTrainer;

public class Validator_OneElement 
{
	@BeforeClass
	public static void setup()
	{
		URL location = NetworkTrainer.class.getProtectionDomain().getCodeSource().getLocation();
        File file = new File(location.getPath()).getParentFile();
        
        // /xlt-visual-assert/config
        String propertieFile = file.toString() + File.separator + "config" + File.separator + "ai.properties";
        
        // /xlt-visual-assert/src/test/com/xceptance/xlt/ai
        String testFolderPath = file.toString() + File.separator + "src" + File.separator + 
        						"test" + File.separator + "com" + File.separator + "xceptance" + 
        						File.separator + "xlt" + File.separator + "ai" + File.separator;
               
        // image with one group
    	String testFolderName 	= "Test_Image_OneElement";		
		String completteFolderName 	= testFolderPath + testFolderName; 
        
		String[] argTR = 
			{
					completteFolderName,
					testFolderPath,
					propertieFile,
					testFolderName
			};
		
		String[] argTE =
			{				
					completteFolderName + ".network",
					completteFolderName
			};
		
		NetworkTrainer.main(argTR);
		NetworkTester.main(argTE);
	}
	
	@Test
	public void foundOneElement()
	{
		assertTrue(NetworkTrainer.im.getCurator().get(0).getMetricCurator().metricList.size() == 1);
	}
	
	@Test
	public void checkAverageMetric()
	{
		assertTrue(NetworkTester.an.getAverageMetric().size() == 1);
	}
}
