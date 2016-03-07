**Screenshot Comparison User Guide:**

**Why Screenshot Comparison:**
The screenshot comparison validates the correctness of a page by it's visual appearance with screenshots. This can be used for 
additional test automation of websites, because some changes e.g. the displacement of page elements are not detected by normal automation.


**What does the visualassertion do:**  
The visualassertion module first takes a screenshot of the current page. This screenshot is either saved as first reference image, if no other 
screenshot for this page in this test case can be found, or compared to an already existing reference image. The comparison is done with a in 
the visualassertion.properties selected algorithm. The goal of the available algorithms is not only to detect a difference between two screenshots
of the same page, but rather to calculate whether a found difference has to be handled as a layout flaw.
When you run the screenshot comparison module it creates the following path: 
%environment%/%testcasename%/%browsername%/%browserversion%/ 
inside of ./results/visualassertion. The names of the images consist of a local index and the name of the action they were taken in. If there are no differences
between reference and current image, the taken screenshot is saved. If there are differences, the screenshot as well as a copy, in which the differences are marked
in a in the properties selectable style, is saved. In this case the test will fail the assertion.

The screenshot comparison can be set to create a difference image in addition to the marked image. The difference image is a greyscale image where the only the differences
are displayed on a black background. The brightness of each pixel depends on the color difference that exists between the two compared images. 
The bigger the difference, the brighter the pixel. 

The screenshot comparison can also be set to run in training mode. In that case, differences will be masked, not marked. 
This means the program will create a mask image for every screenshot. Detected differences will be painted black in the mask image and black areas are 
ignored in later comparisons. If more differences are detected in later training runs, they will also be painted black in addition to the already 
found ones. This means additional training runs adjust the mask further, they will never reset it. Masked areas are painted black in the marked image.

It is possible to manually edit the mask image. You can set areas black in the mask image to mask them or set them transparent to unmask them.


**Training mode:**

If the trainingMode property is true, the screenshot comparison will do a training run. Differences will be masked, not marked and the test case will not fail due to an assertion. 
In future runs, the mask will be put over both the reference screenshot and the new screenshot.
Differences inside the masked areas will not be detected. Masked areas will appear black in the marked image.

If the closeMask property is true, small gaps in the maskImage will be filled and the ignored area is increased. 
For example, you test a homepage with dynamically created pictures of products. You let the screenshot comparison run once with training mode on. 
There are likely some pixels that have not been masked in the first run, but will change with future runs. For example a red pair of shoes and a brown pair of shoes with 
some red stripes or different text content in the same spot. 


**Noteworthy tips, tricks and interactions:**

The firefox webdriver takes screenshots of the full website, the chrome driver does not.  
The chrome webdriver tends to be faster, but needs a bit more waitTime to take stable screenshots.

Differences in size cannot be masked. Therefore, it does not make sense to use the screenshot comparison with a firefox driver on a website that changes it's size very often. 
Differences in size will be shown as transparent in the difference image.

If there are dynamic elements with changing size, for example pictures of products on a constant backgound color, it will take multiple training runs or manual intervention 
to mask the correct area.

The taken reference screenshots cannot easily be transferred from one operating system or one browser to the other. Differences in the rendering often result in screenshots 
with different sizes, minor differences in the content and shifts. The same can be true after browser upgrades. 

If a certain comparison fails again, previously made difference images, marked images and new screenshot images will be overwritten. 

The mouse should be kept at the same place or outside the website window, mouseover popups will ruin the comparison.

Even small changes in text, including formatting, font, size and style can't be accounted for using the fuzzyness parameters.

The pixelPerBlockXY parameter determines the size of the blocks. Their position is determined by their size (first block in the top left corner, the second block below it …). 
Therefore, the pixelPerBlockXY implicitly determines the blocks position. When it changes, their position shifts, which may result in changes.
