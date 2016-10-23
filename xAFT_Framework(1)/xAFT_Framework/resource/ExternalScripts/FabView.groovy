import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.io.File;
import org.openqa.selenium.WebDriverException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

// Method will remove last appended 0's from a date "2015-07-17-06.49.20.816000"
// text will be the required date, numOfChars will be the number of charcters want to be removed

def removeCharsFromString(def selenium, def log4j, String text, String numOfChars)
{
	int i = Integer.parseInt(numOfChars);
	return text.substring(0, text.length()-i);
}


// This method will give the row number for a particular text present in Inhibit history table
// locator is the xpath for the column where to be searched
// text is the input string to be searched

public static int getRowNumOfText(def selenium, def log4j,String locator,String text)
{
		int count=0;
		boolean flag=true;
		List<WebElement> elements=selenium.findElements(By.xpath(locator));
		
		for(WebElement ele:elements){
			
			if(ele.getText().contains(text)){
				flag=false;
				break;
			}
			count++;
		}
		if(flag)
			return -1;
		
		return count;
	}

// Find whether a string contains a particular substring
// source = The source string from where to be searched
// searchItem =  Item to be searched

def strFind(def selenium, def log4j,def source,def searchItem)
{
	log4j.info(source)
		if(source.trim().toLowerCase().contains(searchItem.toLowerCase().trim())){
			return true
		}
		else{
			return false
		}
}


//It will work only if page have single table
def getColumnNumber(def webdriver, def log4j,String locator,def headerName){
	log4j.info("Calling getColumnNumber with parameters:HeaderName:"+headerName);
	int columncount=-1
	//List<WebElement> elements=webdriver.findElements(By.xpath("//th"))
	
	List<WebElement> elements=webdriver.findElements(By.xpath(locator))
	
	for(int count=0;count<elements.size();count++){
		WebElement element=elements.get(count)
		if(element.getText().equalsIgnoreCase(headerName)){
			columncount=count
			break
		}
	}
	columncount =columncount+1	
	log4j.info("Got the column number of HeaderName:"+headerName+" is: "+columncount);
	return columncount
}

//To get the column content from the fabview inhibit history details table
def getColumnContent(def webdriver, def log4j,String locator,def headerName,def separator){
	log4j.info("Calling getColumnContent with parameters:HeaderName:"+headerName);
	String requiredString=""
	int columncount1=-1
	columnCount1=getColumnNumber(webdriver,log4j,locator,headerName)
	log4j.info("Got the column number of HeaderName:"+headerName+" is: "+columnCount1);
	log4j.info("//tr[th[text()='"+headerName+"']]/../following-sibling::tbody/tr/td["+columnCount1+"]");
	List<WebElement> elements=webdriver.findElements(By.xpath("//tr[th[text()='"+headerName+"']]/../following-sibling::tbody/tr/td["+columnCount1+"]"))
	
	for(WebElement ele:elements){
		requiredString=requiredString+separator+ele.getText()
	}
	requiredString=requiredString.replaceFirst(separator,"")
	log4j.info("The content of the header: "+headerName+" is:"+requiredString);
	return requiredString
}
//To get the column content from the fabview inhibit history details table
def getColumnContentOfHeader(def webdriver, def log4j,String locator, def headerName,def separator){
	log4j.info("Calling getColumnContent with parameters:HeaderName:"+headerName);
	String requiredString=""
	int columncount1=-1
	columnCount1=getColumnNumber(webdriver,log4j,locator,headerName)
	log4j.info("Got the column number of HeaderName:"+headerName+" is: "+columnCount1);
	log4j.info("//tr[th[text()='"+headerName+"']]/../following-sibling::tbody/tr/td["+columnCount1+"]");
	List<WebElement> elements=webdriver.findElements(By.xpath("//tr[th[text()='"+headerName+"']]/../following-sibling::tbody/tr/td["+columnCount1+"]"))
	
	for(WebElement ele:elements){
		requiredString=requiredString+separator+ele.getText();
		break;
	}
	requiredString=requiredString.replaceFirst(separator,"")
	log4j.info("The content of the header: "+headerName+" is:"+requiredString);
	return requiredString
}



//To save the exported file from inhibit details page
public static void pressAltandSkeys(def webdriver, def log4j) throws Exception{
	Robot robot;
	robot=new Robot();
	robot.keyPress(KeyEvent.VK_ALT);
	Thread.sleep(1500);
	robot.keyPress(KeyEvent.VK_S);
	
	robot.keyRelease(KeyEvent.VK_S);
	robot.keyRelease(KeyEvent.VK_ALT);
	log4j.info("Alt + S key are pressed" );
}

//To click the export link in inhibit history details page
public static void safeActionClickById(def webdriver, def log4j,String id,String timeOut) throws Exception{
try{
	int tOut=Integer.parseInt(timeOut);
	WebElement ele=new WebDriverWait(webdriver, tOut).until(ExpectedConditions.elementToBeClickable(By.cssSelector(id)));
	new Actions(webdriver).moveToElement(ele).click().perform();
	
	log4j.info("Clicked on inhibitHistory element:" +id);
	}catch(Exception e){
		throw new Exception(e);
	}
}

public static void safeActionClickByXapth(def webdriver, def log4j,String xpath,String timeOut) throws Exception{
try{
	int tOut=Integer.parseInt(timeOut);
	WebElement ele=new WebDriverWait(webdriver, tOut).until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
	new Actions(webdriver).moveToElement(ele).click().perform();
	
	log4j.info("Clicked on inhibitHistory element:" +xpath);
	}catch(Exception e){
		throw new Exception(e);
	}
}

//Delete the exported file if exists
public static void deleteFileIfExists(def webdriver, def log4j,String file) throws Exception{
	try{
		File f= new File(file);
		if(f.exists())
		f.delete();
	}catch(Exception e){
	throw new Exception(e);
	}
}

public static void selectMainWindow(def webdriver, def log4j) throws Exception{
try{ 
	String[] windows=(String[]) webdriver.getWindowHandles().toArray();
		webdriver.switchTo().window(windows[0]);
		
	}catch (WebDriverException e) {
			log4j.info(e.message());
			throw new WebDriverException(e);
	} catch (Exception e) {
			throw new Exception(e);
	}
}

public static void closeCurrentWindow(def webdriver, def log4j,String windowNum) throws Exception{
try{ 
	int winNum=Integer.parseInt(windowNum);
	String[] windows=(String[]) webdriver.getWindowHandles().toArray();
	for(int i=0;i<windows.length;i++){
		webdriver.switchTo().window(windows[i]);
		if(i==winNum){
			webdriver.close();
			break;
			}
		}
	}catch (WebDriverException e) {
			throw new WebDriverException(e);
	} catch (Exception e) {
			throw new Exception(e);
	}
}

// Fetch the current year
public static int getYear(def selenium, def log4j) 
{
	int year = Calendar.getInstance().get(Calendar.YEAR);
	log4j.info(year);
    return year;
}

public static int compareDate(def webdriver, def log4j,String Date1, String Date1Format,String Date2, String Date2Format){
        try {
                    Date d1 = new SimpleDateFormat(Date1Format).parse(Date1);
                    Date d2 = new SimpleDateFormat(Date2Format).parse(Date2);
                                        
                    return d1.compareTo(d2);
        } catch (ParseException e) {
                    log4j.info("Exception occured while parsing" + e.getMessage());
                    return -2;
        } 
}


//Below method is used to find whether column is present or not in history page 
// Sprint-4 Raja krishna Dasari
//locator = xpath of table header which consists of column names
public static boolean searchColumnExists(def selenium, def log4j,String locator,String colName)
{
		int count=0;
		boolean flag=false;
		List<WebElement> elements=selenium.findElements(By.xpath(locator));
		
		for(WebElement ele:elements){
			
			if(ele.getText().contains(colName)){
				flag=true;
				break;
			}
			count++;
		}
		return flag;
	}

//Below method is used to find whether the provided cell data is present or not in a specific column in history page 
// Sprint-4 Raja krishna Dasari
//locator = xpath of specific column in a table
//text = expected cell data
public static boolean isTextPresentInColumn(def selenium, def log4j,String locator,String text)
{
		boolean flag = false;
		
		List<WebElement> elements=selenium.findElements(By.xpath(locator));
		
		if(!elements.isEmpty()){
			log4j.info("elements are not empty");
			for(WebElement ele:elements){
			log4j.info("element text from UI "+ele.getText());
			if(ele.getText().contains(text)){
				log4j.info("In if condition");
				flag = true;
				}
				else
				{
					log4j.info("In if else condition");
					flag = false;
					break;
				}
			}
			return flag;
		}
		else
		log4j.info("elements are empty");
		return flag;
}