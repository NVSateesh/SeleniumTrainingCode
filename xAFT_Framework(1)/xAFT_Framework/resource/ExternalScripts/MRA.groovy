import java.io.*;
import org.openqa.selenium.*;
import java.util.*;
import java.lang.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import java.util.Scanner;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;
import java.text.*;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Random;

public class MRA
	{		
		//This method perform click operation on specified Key
		public boolean clickOnKey(def webdriver, def log4j, String keyName)throws Exception
		{
			Robot robot;
			robot=new Robot();
			boolean flag=true;
			
			if(keyName.toUpperCase()=="DELETE" || keyName.toUpperCase()=="DEL"){
				log4j.info("Performing click operation on '"+keyName+"' key");
				robot.keyPress(KeyEvent.VK_DELETE);
				robot.keyRelease(KeyEvent.VK_DELETE);
			log4j.info("Performed click operation on '"+keyName+"' key");
			}else if(keyName.toUpperCase()=="ENTER"){
				robot.keyPress(KeyEvent.VK_ENTER);
				robot.keyRelease(KeyEvent.VK_ENTER);
				log4j.info("Performed click operation on '"+keyName+"' key");
				
			}else if(keyName.toUpperCase()=="ESCAPE"){
				robot.keyPress(KeyEvent.VK_ESCAPE);
				robot.keyRelease(KeyEvent.VK_ESCAPE);
				log4j.info("Performed click operation on '"+keyName+"' key");
				
			}			
			else{
				log4j.info("Given "+keyName+" key is not found ");
				flag=false;
			}
			
			
			return flag;
		}
		
		// Compare two batch files
		public boolean compareBatchFiles(def webdriver, def log4j, String fileContent, String locator)throws Exception
		{
			
			WebElement element = webdriver.findElement(By.xpath(locator));
			boolean flag=false;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locator)));
			String str=element.getAttribute("value");
			log4j.info("file content is: "+fileContent);
			log4j.info("Element value is: "+str);
			String[] fileString=fileContent.split("\n");
			String[] elementString=str.split("\n");
			if(elementString.length==fileString.length)
			{
				log4j.info("The fileString and element have same number of lines");
			}else
			{
				log4j.warn("The fileString and element have different number of lines");
			}
			log4j.info("Comparing content......");
			for(int lineCount=0;lineCount<elementString.length;lineCount++)
			{
				String eleString=elementString[lineCount];
				String fString=fileString[lineCount];
				eleString=eleString.substring(0,eleString.indexOf("{")+1).replace(" ","");
				fString=fString.substring(0,fString.indexOf("{")+1).replace(" ","");
				log4j.info("Line number:::"+lineCount);
				log4j.info("Element string:::"+eleString);
				log4j.info("File string:::"+fString);
				
				if(eleString.equals(fString))
				{
					flag=true;
				}else
				{ 
					flag=false;
					break;
				}
			}	
			return flag;
		}
		
		// Fetch only integer part from the MR number or OR number
		public int getMRNumber(def webdriver,def log4j,String str)throws Exception
		{
			String mainString=str.substring(2, str.length());
			log4j.info("String="+mainString);	
			int mrNum=Integer.valueOf(mainString).intValue();
			log4j.info("Output="+mrNum);
			return mrNum ;
		}
		
		//Concat three string
		public String stringConcat(def webdriver,def log4j,String str1,String str2,String str3)throws Exception
		{      
			log4j.info("concatString is="+str1+str2+str3);	
			String mainString=str1+str2+str3;
			System.out.println(mainString);
			return mainString;
		}	
		
		//Verify for the file exists or not.
		public boolean isFileExists(def webdriver, def log4j, String path) 
		{
			File file = new File(path);
			return file.exists();
		}
		
		//Get the absolute path
		public static String getFilePath(def Selenium, def log4j, String filePath)
		{
				File f=new File(filePath);
				return f.getCanonicalPath();
		}
		
		//Concat strings with a delimeter
		public static String concatGivenStrings(def webdriver, def log4j,String...str)
		{ 
			log4j.info("concatString is="+str);
			String res="";
			for(String s:str){
			res=res+System.getProperty("file.separator")+s;
			}
			log4j.info("concatString is="+res);
			return res.substring(1,res.length());
		}
		
		//Click on a link using XPath
		public void jsClickByXpath(def driver, def log4j, String _xpath)
		{
			((JavascriptExecutor)driver).executeScript("return arguments[0].click()", driver.findElement(By.xpath(_xpath)));
		}
		
		//To split two values
		public static String getTextbyIndex(def webdriver, def log4j, String str, String index)
		{
			int i=Integer.parseInt(index);
			str=str.trim();
			return str.replace("(", "").replace(")","").split(",")[i];
		}
		
		// Add quotes to the second string for a DB Query
		public String getModifiedStr(def webdriver, def log4j, String str1, String str2)
		{
			return str1+"'"+str2+"'";
		}
		
		//Calculate WafermapVector
		public double calculateWafermap(def webdriver, def log4j, String offsetx, String steppingx)
		{
			double x=Double.parseDouble(offsetx);
			double y=Double.parseDouble(steppingx);
			return (x-(y/2))/1000;
		}
		
		//Calculate EyePointvector
		public double calculateEyePointVector(def webdriver, def log4j, String eyeptvector, String stepping)
		{
			double x=Double.parseDouble(eyeptvector);
			double y=Double.parseDouble(stepping);
			return (((x+y)/2)/1000);
		}		
		
		//Get the numbers from a string
		public String getNumFromString(def webdriver, def log4j, String str)
		{
			return str.replaceAll("\\D+","");
		}
		
		//Get FieldSize 
		public int modifyFieldSize(def webdriver, def log4j, String fieldSize)
		{
			double d=Double.parseDouble(fieldSize);
			return (int) (d/1000);
		}
		
		//Convert a string value to double
		public double convertToDouble(def webdriver, def log4j, String str)
		{
			double value = Double.parseDouble(str);
			return value;
		}
		
		//Calculate offset values for Overlay recipe
		public double calculateOffset(def webdriver, def log4j, String offsetx, String steppingx)
		{
			double x=Double.parseDouble(offsetx);
			double y=Double.parseDouble(steppingx);
			return (x-(y/2));
		}
		
		//Write given data into file
		//Make sure "MRA_Automation_Downloads" has been created under "screenShots" folder
		public String writeToFile(def webdriver, def log4j,String data)throws Exception
		{
			Random rand = new Random();
			String fPath="./screenShots/MRA_Automation_Downloads/editBatch"+rand.nextInt(99)+rand.nextInt(99)+".json";
			FileWriter fw=new FileWriter(fPath);
			fw.write(data);
			fw.close();
			return fPath;
		}
		
		//Delete the exported file if exists
		public void deleteFileIfExists(def webdriver, def log4j,String file) throws Exception{
			try{
				File f= new File(file);
				if(f.exists())
				f.delete();
			}catch(Exception e){
				throw new Exception(e);
			}
		}
	
		//Fetch data from a text area and modify it
		public void modifyBatchFileData(def webdriver,def log4j,String srcStr, String destStr, String xpath)throws Exception
		{
			WebElement element = webdriver.findElement(By.xpath(xpath));
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
			String str=element.getAttribute("value");
			log4j.info("_______________ The batch file string is :"+str);
			String data = str.replace(srcStr,destStr);
			log4j.info("_______________ The data after replacing a string is :"+data);
			element.clear();
			element.sendKeys(data);
			//((JavascriptExecutor)webdriver).executeScript("arguments[0].value = arguments[1];", element, "");
			//((JavascriptExecutor)webdriver).executeScript("arguments[0].value = arguments[1];", element, data);
			clickOnKey(webdriver,log4j,"ENTER");
		}
		
		//This method wait until the given element to be invisible 
		public void waitUntilInvisibilityOfElementByXpath(def webdriver, def log4j,String xpath)
		{
			WebDriverWait wait=new WebDriverWait(webdriver,300);
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
		}
		
		//To replace Device,Layer,VisibleLayer in RecipePath
		public static String getRecipePath(def webdriver, def log4j, String mainString, String device, String layer, String visibleLayer)
		{
			return mainString.replace("%DEVICE%", device).replace("%Layer%", layer).replace("%VisibleLayer%", visibleLayer);
		}
		
		//This method wait until the given element to be clickabele 
		public void waitUntilElementToBeClickable(def webdriver, def log4j,String xpath)
		{
			WebDriverWait wait=new WebDriverWait(webdriver,150);
			wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
		}
		
		//To get MR number from batch mode link
		public static String getBatchMRNum(def webdriver, def log4j, String s1, String s2)
		{
		 log4j.info("String is"+s1);
		 log4j.info("String is"+s2);
		 return (s1.substring(((s1.indexOf(s2))+s2.length()+1),s1.length())).split("_")[0];
		}
		
		
		//
		public void uploadSites(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@id='uploadsites']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='uploadsites']")));
			executor.executeScript("arguments[0].click();", element);
		}		
		
		//method to get last modified or recent file in a directory
		public String lastFileModified(def webdriver, def log4j,def path) 
		{
			File dir = new File(path);
			File[] files = dir.listFiles();
			File lastModifiedFile = files[0];
		   
			for (int i = 1; i < files.length; i++) 
			{
				if (lastModifiedFile.lastModified() < files[i].lastModified()) 
				{
				  lastModifiedFile = files[i];
				}
			}
				 return lastModifiedFile;
		}		
		
				
		//method to verify xml file data
		public String getNodeValue(String xmlPath, String xPath)
		{
			String requiredValue="EMPTY";
			try
			{
				DocumentBuilderFactory xmlFactory =  DocumentBuilderFactory.newInstance();
				DocumentBuilder xmlDocBuilder = xmlFactory.newDocumentBuilder();
				Document xmlDoc = xmlDocBuilder.parse(xmlPath);
				XPath xpath = XPathFactory.newInstance().newXPath();
				XPathExpression xpathExpression = xpath.compile(xPath);
				Node  changeThisNode = (Node)xpathExpression.evaluate(xmlDoc,XPathConstants.NODE);
				requiredValue=changeThisNode.getTextContent();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(null==requiredValue)
			{
				return "EMPTY";
			}else
			{
				return requiredValue;
			}
		}
	
		public boolean editBatchFile(def webdriver,def log4j,String subString) throws Exception
		{
			String var="Empty";
			log4j.info("Macharhe:::::::::"+webdriver.findElement(By.xpath("//textarea[@id='editBatchFile']")).getText());
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//textarea[@id='editBatchFile']")));
			var=webdriver.findElement(By.xpath("//textarea[@id='editBatchFile']")).getText();
			if(var.indexOf(subString)!=-1)
			{     
					//System.out.println(main+"string contains"+subString);
					return true;
			      
			      }
			    else{
			        return false;
			    }		
			
		}
			

		public void getElementScreenShot(def webdriver, def log4j,def element,def type,def path){
			log4j.info("getElementScreenShot________________>>>>>>>>>>>>>>>"+path);
			try{
				WebElement ele =null;
				if(type.equalsIgnoreCase("id")){
					ele  = webdriver.findElement(By.id(element));
				}else if(type.equalsIgnoreCase("xpath")){
					ele = webdriver.findElement(By.xpath(element));
				}else if(type.equalsIgnoreCase("name")){
					ele  = webdriver.findElement(By.name(element));
				}
				log4j.info("done");
				log4j.info("done");
				log4j.info("done");
				log4j.info("done");
				File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
				log4j.info("done1");
				BufferedImage  fullImg = ImageIO.read(screenshot);
				log4j.info("done2");
				Point point = ele.getLocation();
				log4j.info("done3");
				int eleWidth = ele.getSize().getWidth();
				log4j.info("done4");
				log4j.info("width: "+eleWidth);
				int eleHeight = ele.getSize().getHeight();
				BufferedImage eleScreenshot= fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
				ImageIO.write(eleScreenshot, "png", screenshot);
			   // FileUtils.copyFile(screenshot, new File(path));
				log4j.info(element +" screen shot is placed into the location: "+path);
			}catch(Exception e){
				log4j.error("Problem occured while capturing the "+element+" image. Exception is: "+e.getMessage());
			}	
		}	


		public boolean viewLogFile(def webdriver,def log4j,String subString) throws Exception
		{
			String var="Empty";
				
			log4j.info("Macharhe:::::::::"+webdriver.findElement(By.xpath("//td[@id='contentRight']/div[1]")).getText());
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[@id='contentRight']/div[1]")));
			var=webdriver.findElement(By.xpath("//td[@id='contentRight']/div[1]")).getText();
			if(var.indexOf(subString)!=-1){
			      
					//System.out.println(main+"string contains"+subString);
					return true;
			      
			      }
			    else{
			        return false;
			    }		
			
		}
		public String stringConcat(def webdriver,def log4j,String str1,int str2,String str3)throws Exception
		{      
			log4j.info("concatString is="+str1+str2+str3);	
			String mainString=str1+str2+str3;
			System.out.println(mainString);
			return mainString;
		}
		
		public String fileConcat(def webdriver,def log4j,String str1,String str2,String str3,String str4,String str5)throws Exception
		{      
			log4j.info("concatString is="+str1+str2+str3+str4+str5);	
			String mainString=str1+str2+"/"+str3+"/"+str4+str5;
			System.out.println(mainString);
			return mainString;
		}

	
		//Fetch data from a text area
		public void editBatchFileData(def webdriver,def log4j,String srcStr, String destStr)throws Exception
		{
			WebElement element = webdriver.findElement(By.xpath("//textarea[@id='editBatchFile']"));
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//textarea[@id='editBatchFile']")));
			String str=element.getText();
			log4j.info("_______________ The batch file string is :"+str);
			String data = str.replace(srcStr,destStr);
			log4j.info("_______________ The data after replacing a string is :"+data);
			element.clear();
			((JavascriptExecutor)webdriver).executeScript("arguments[0].value = arguments[1];", element, data);
		}

		//Method to count number of checked Checkboxes on Design Based Overlay page for start wizard
		public int chkboxCount(def webdriver, def log4j)throws Exception
			{	
				WebDriverWait wait = new WebDriverWait(webdriver, 40);
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tr[@style]")));
				return webdriver.findElements(By.xpath("//table//tr[@style]")).size();
			}
		
			
		//Click on button pre-fill
		public void clickBtnPreFill(def webdriver, def log4j)throws Exception
		{
			WebElement element = webdriver.findElement(By.xpath("//input[@name='prefill']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='prefill']")));
			executor.executeScript("arguments[0].click();", element);
		}

		
		
		//Verifying whether string is empty or not
		public static Boolean stringIsEmpty(def Selenium, def log4j, String val)
		{
		   
			  log4j.info("list is not empty"+val);
			  return val.isEmpty();
			 
		}
		
		//To split two values
		public static String splitString(def driver, def log4j, String mainString, String index){
			int i=Integer.parseInt(index);
			String temp=mainString.substring(1,mainString.length()-1);
			String[] temp1=temp.split(",");
			return temp1[i];
		}
		
			
				
		//To split two values and it Trails the zeros after decimal
		public String getTextbyIndex1(def webdriver, def log4j,String str,String index){
			int i=Integer.parseInt(index);
			str=str.trim();
			double d=Double.parseDouble(str.replace("(", "").replace(")","").split(",")[i]);
			return new BigDecimal(Double.toString(d)).stripTrailingZeros().toPlainString();
		}
		
			
		public static boolean isContentPresent(def webdriver, def log4j,String str1, String str2){
			
			String[] s1=str1.split("\n");
			
			for(int i=0;i<s1.length;i++){
				log4j.info(s1[i]);
				if(!str2.contains(s1[i])){
					return false;
				}
			}
			return true;
		}
		public static boolean waitUntilJStoLoad(def driver,def log4j) {
			
			WebDriverWait wait = new WebDriverWait(driver, 90);

			// wait for jQuery to load
			ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
			  @Override
			  public Boolean apply(def driver1) {
				try {
				  return ((boolean)((JavascriptExecutor)driver1).executeScript("return jQuery.active==0"));
				}
				catch (WebDriverException e) {
				  return true;
				}
			  }
			};

			// wait for Javascript to load
			ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
			  @Override
			  public Boolean apply(def driver1) {
				return ((JavascriptExecutor)driver1).executeScript("return document.readyState").toString().equals("complete");
			  }
			};

		  return wait.until(jQueryLoad) && wait.until(jsLoad);
		}
			
		public boolean verifyEditBatchContent(def webdriver, def log4j,String eBatchCont, String mfData){
			
			String[] fData=mfData.split("\n");
			for(String d:fData){
				String[] f=d.split(",");
				if(!eBatchCont.contains("\"pdName\" : \""+f[0]+"\""))
					return false;
				if(!eBatchCont.contains("\"strucIdentifier\" : \""+f[1]+"\""))
					return false;
				if(!eBatchCont.contains("\"samplingPlanName\" : \""+f[2]+"\""))
					return false;
				if(!eBatchCont.contains("\"samplingValue\" : \""+f[2]+"\""))
					return false;
				}
			
			return true;
		}
		
		
		public boolean verifyString(def webdriver, def log4j,String str1, String str2){
		
			return str1.contains(str2);
		}
		
		
				
		public int getSizeOfGivenText(String text,String delimiter){
				
			return text.split(delimiter).length;
		}
		
		public String getMFData(String text, String row, String index){
			String[] str=text.split("\n");
			int r=Integer.parseInt(row);
			int i=Integer.parseInt(index);
			return str[r].split(",")[i];
		}
		public static String getSelectedMFeatures(def webdriver, def log4j){
			waitUntilJStoLoad(webdriver,log4j);
			String res="";
			List<WebElement> eleList=webdriver.findElements(By.xpath("//tr[contains(@class,'hightlightRow ng-scope rowSelected')]"));
			for(int i=1;i<=eleList.size();i++){
				String pd=webdriver.findElement(By.xpath("(//tr[contains(@class,'hightlightRow ng-scope rowSelected')])["+i+"]/td[contains(@data-title,'Process Definition')]")).getText();
				String strct=webdriver.findElement(By.xpath("(//tr[contains(@class,'hightlightRow ng-scope rowSelected')])["+i+"]/td[contains(@data-title,'Structure')]")).getText();
				Select s=new Select(webdriver.findElement(By.xpath("(//tr[contains(@class,'hightlightRow ng-scope rowSelected')])["+i+"]/td[contains(@data-title,'Wafer Sampling Plan')]/div/select[@name='selSamplingPlan']")));
				String WSPlan=s.getFirstSelectedOption().getText();
				//s=new Select(webdriver.findElement(By.xpath("(//tr[contains(@class,'hightlightRow ng-scope rowSelected')])["+i+"]/td[contains(@data-title,'Location Selection Rule')]/div/select")));
				//String locationSR=s.getFirstSelectedOption().getText();
				res= res+"\n"+pd+","+strct+","+WSPlan;
				
			}
			res=res.replaceFirst("\n", "");
			
			return res;
		}
		
		public static boolean verifySBFilteredDataInMFTable(def webdriver, def log4j, String sBFData) throws Exception{
			waitUntilJStoLoad(webdriver,log4j);
			sBFData=sBFData.replaceFirst(";", "").replace("\n","");
			String[] fData=sBFData.split(";");
			for(int i=0;i<fData.length;i++){
			log4j.info("Filtered data -"+fData[i]);
				String[] d=fData[i].split(",");
				webdriver.findElement(By.xpath("(//input[@name='pdName' and @type='text'] )[1]")).clear();
				webdriver.findElement(By.xpath("(//input[@name='pdName' and @type='text'] )[1]")).sendKeys(d[0]);
				waitUntilJStoLoad(webdriver,log4j);
				webdriver.findElement(By.xpath("//input[@name='strucType' and @type='text']")).clear();
				webdriver.findElement(By.xpath("//input[@name='strucType' and @type='text']")).sendKeys(d[1]);
				waitUntilJStoLoad(webdriver,log4j);
				Thread.sleep(2000);
				Select s=new Select(webdriver.findElement(By.xpath("//select[@name='selSamplingPlanfilter']")));
				s.selectByValue(d[2]);
				waitUntilJStoLoad(webdriver,log4j);
				Thread.sleep(6000);
				List<WebElement> nRows=webdriver.findElements(By.xpath("//div[@id='startWizardOverlayTable']/table/tbody/tr[contains(@class,'hightlightRow ng-scope')]"));
				if(nRows.size()>0)
				for(int j=1;j<=nRows.size();j++){
					if(!webdriver.findElement(By.xpath("//table/tbody/tr["+j+"][contains(@class,'hightlightRow ng-scope rowSelected')]/td/input[@name='cdsemInfoTableCheckGroup']")).isSelected()){
						log4j.error(d[i]+" Metrology feature is not selected");
						return false;
					}
				}
			
			}
			webdriver.findElement(By.xpath("(//input[@name='pdName' and @type='text'] )[1]")).clear();
			webdriver.findElement(By.xpath("//input[@name='strucType' and @type='text']")).clear();
			
			return true;
		}
		
		
		
		
}