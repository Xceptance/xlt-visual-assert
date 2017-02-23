// Copyright 2017 Thomas Volkmann
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this
// software and associated documentation files (the "Software"), 
// to deal in the Software without restriction, including without limitation the rights 
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
// and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all 
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
// BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, 
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

package com.xceptance.xlt.ai.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import com.xceptance.xlt.ai.NetworkTester;
import com.xceptance.xlt.ai.NetworkTrainer;

public class Validator_OneImageContained 
{
	@BeforeClass
	public static void setup()
	{
		URL location = NetworkTrainer.class.getProtectionDomain().getCodeSource().getLocation();
        File file = new File(location.getPath()).getParentFile();
        
        // /xlt-visual-assert/config
        String propertieFile = file.toString() + file.separator + "config" + file.separator + "ai.properties";
        
        // /xlt-visual-assert/src/test/com/xceptance/xlt/ai
        String testFolderPath = file.toString() + file.separator + "src" + file.separator + 
        						"test" + file.separator + "com" + file.separator + "xceptance" + 
        						file.separator + "xlt" + file.separator + "ai" + file.separator;
        
        // images for the Exact-Same-Folder (ESF) test
		String testFolderName 		= "Test_Images_ESF";		
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
	public void CreateTrainerAndTester() 
	{
		assertTrue(NetworkTrainer.an != null);
		assertTrue(NetworkTrainer.im != null);	
			
		assertTrue(NetworkTester.an != null);
		assertTrue(NetworkTester.im != null);
	}
	
	
	
}
