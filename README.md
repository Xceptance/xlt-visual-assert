**Screenshot Comparison User Guide:**

**Why Screenshot Comparison:**
The screenshot comparison validates the correctness of a page by it's visual appearance. This can be used for additional test automation of websites, 
because some changes, e.g. the displacement of page elements, are not detected by normal automation. The module should not mereley detect changes, but 
determine if those changes break the layout of the page, or are due to dynamic content changes.


**What does the visualassertion do:**  
The visualassertion module first takes a screenshot of the current page, that is either saved as the first reference image, if no other 
screenshot for this page in this test case can be found, or compared to an already existing reference image. The comparison is done with a in 
the visualassertion.properties selected algorithm. The goal of the available algorithms is not only to detect a difference between two screenshots
of the same page, but rather to calculate whether a found difference has to be handled as a layout flaw. When you run the screenshot comparison module it 
creates the following path: 
%environment%/%testcasename%/%browsername%/%browserversion%/ inside of ./results/visualassertion. 
The names of the images are generated with a local index and the name of the action they were taken in. If there are no differences between reference and current image, 
the taken screenshot is saved. If there are differences, the screenshot as well as a copy, in which the differences are marked in a selectable style (properties), is saved. 
In this case the test will fail the assertion.

The screenshot comparison can be set to create a difference image in addition to the marked image. The difference image only displays the pixels, that where found 
to be different, the rest of the image is black. The brightness of each pixel depends on the color difference that exists between the two compared images. 
The bigger the difference, the brighter the pixel. 

The screenshot comparison can also be set to run in training mode. In that case differences are masked in a separate mask image, but not marked. 
This means the program will create a mask image for every screenshot. Detected differences are painted black in the mask image and black areas are 
ignored in later comparisons, the rest of the mask is transparent. If more differences are detected in later training runs, they will also be painted black in addition to the already 
found ones. This means additional training runs adjust the mask further, they will never reset it. Masked areas are painted black in the marked image.

It is possible to manually edit the mask image. You can set areas black in the mask image to mask them, or set them transparent to unmask them. If the closeMask property 
is true, small gaps between already marked areas in the maskImage will be filled and the ignored area is increased. For example, you test a homepage with dynamic content 
and let the screenshot comparison run once with training mode on. There are likely some pixels that have not been masked in the first run, but will change with future runs, 
e.g. dynamic text that contains the user name and/or address. 


**How to get started**
This module is still a work in progress and is implemented in a script testcase for the posters webshop. The posters local server for the test can be found 
under .\samples\app-server\bin of your XLT folder. After running the server you can run the TGuestOrder script test case in your IDE, which contains the module in different actions. 
Running it the first time creates the result folder structure in your visualassertion project folder. Running it the second time will make the first comparison, 
which will probably fail because the mask has not been trained yet and there is some dynamic content. You can enable mask training and adjust the configuration of the test case 
in the visualassertion.properties file under /config in your project folder. While the mask training is enabled no assertion will be made, but found differences will be
saved in the mask images. See the properties file for additional information about the different configurations. 


**Noteworthy tips, tricks and interactions:**
The firefox webdriver takes screenshots of the full website, the chrome driver does not. The chrome webdriver tends to be faster, but needs a bit more waitTime to take stable screenshots.

Differences in size cannot be masked. Therefore, it does not make sense to use the screenshot comparison with a firefox driver on a website that changes it's size very often. 
Differences in size will be shown as transparent in the difference image.

If there are dynamic elements with changing size, for example pictures of products on a constant backgound color, it will take multiple training runs or manual intervention 
to mask the correct area.

The taken reference screenshots can't easily be transferred from one operating system or one browser to the other. Differences in the rendering often result in screenshots 
with different sizes, minor differences in the content and shifts. The same can be true after browser upgrades. 

If a certain comparison fails again, previously made difference images, marked images and new screenshot images will be overwritten. 

The mouse should be kept at the same place or outside the website window, mouseover popups will ruin the comparison.

Even small changes in text, including formatting, font, size and style can't be accounted for using the fuzzyness parameters.

The pixelPerBlockXY parameter determines the size of the blocks. Their position is determined by their size (first block in the top left corner, the second block below it …). 
Therefore, the pixelPerBlockXY implicitly determines the blocks position. When it changes, their position shifts, which may result in changes.


**Project outlook:**
Future adaptions and aditions to the comparison algorithms are planned, which allow for a more consistent page comparison, that adjusts better to dynamic content changes. 
This is still one of the main problems, because at the moment changes in a webpage might get asserted as error, even though it is a valid layout/content change. 
E.g. additional products in the product grid, change of page size, because of additional text, changed text content.... 
