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
import java.text.*;

public class MRA
	{
		//Method for rejecting pop up alert
		public void dismissAlert(def webdriver, def log4j)throws Exception
		{
			Alert myAlert = webdriver.switchTo().alert();
			myAlert.dismiss();
		}


		//Method for accepting pop up alert
		public void acceptAlert(def webdriver, def log4j)throws Exception
		{
		
					Alert myAlert = webdriver.switchTo().alert();
					myAlert.accept();
		}
		
		
		//Method to count number of Checkboxes on Design Based Overlay page for start wizard
		public int checkboxCount(def webdriver, def log4j)throws Exception
		{	
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='FeatureSelection']")));
			return webdriver.findElements(By.xpath("//input[@id='FeatureSelection']")).size();
		}
		
		
		//Method to count number of Product Group Data Wafer Layouts page to verify product group data
		public int productGroupData(def webdriver, def log4j)throws Exception
		{	
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form[@name='waferSearch0']/../following-sibling::td")));
			return webdriver.findElements(By.xpath("//form[@name='waferSearch0']/../following-sibling::td")).size();
		}
		
		
		//this method is to count number of status of a device
		public int deviceStatus(def webdriver, def log4j)throws Exception
		{	
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//select[@id='findStatus']/option")));
			return webdriver.findElements(By.xpath("//select[@id='findStatus']/option")).size();
		}
		
		
		//this method is to count number of device present at a particular page
		public int deviceId(def webdriver, def log4j)throws Exception
		{
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//select[@id='productID']/option")));
			return webdriver.findElements(By.xpath("//select[@id='productID']/option")).size();
		}
		
		
		//this method is to count number of specbook views
		public int linkSpecView(def webdriver, def log4j)throws Exception
		{
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'View SpecBook')]")));
			return webdriver.findElements(By.xpath("//a[contains(text(),'View SpecBook')]")).size();
		}
		
	
		
		public void clickButton(def webdriver,def log4j)
		{
			WebElement element = webdriver.findElement(By.xpath("//input[@id='generaterecipe']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='generaterecipe']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void downloadSpeckbook(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='downloadSPECBOOK']"));
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='downloadSPECBOOK']")));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public static String getFilePath(def Selenium, def log4j, String filePath)
		{
				File f=new File(filePath);
				return f.getCanonicalPath();
		}
		
		
		public void viewMR(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//a[contains(text(),'View MR')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'View MR')]")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void batchJobStatus(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//a[contains(text(),'Batch Job Status')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'Batch Job Status')]")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void batchMode(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//a[contains(text(),'Batch Mode')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'Batch Mode')]")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void settingsOther(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//a[contains(text(),'Settings')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'Settings')]")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void addReticle(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@id='addreticle']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='addreticle']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void reticleButton(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//table[@class='listtable']/tbody/tr/td[contains(text(),'7130AL0RXB1')]/../td/input[@value='Add Reticle']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table[@class='listtable']/tbody/tr/td[contains(text(),'7130AL0RXB1')]/../td/input[@value='Add Reticle']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void logicalOperator(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@id='add']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='add']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void mergeReticle(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@id='mergereticle']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='mergereticle']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void uploadSites(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@id='uploadsites']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='uploadsites']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void uploadSitesSpreadSheet(def webdriver,def log4j,def text)
		{
			
			WebElement element =webdriver.findElement(By.xpath("//html/body/table/tbody/tr/td/form/table/tbody/tr/td/input[@name='mrSpreadsheetRET']"));
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//html/body/table/tbody/tr/td/form/table/tbody/tr/td/input[@name='mrSpreadsheetRET']")));
			element.sendKeys(text);
		}
		
		
		public void uploadSpecbook(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//html/body/table/tbody/tr/td/div/form/div/table/tbody/tr/td/input[@name='uploadSPECBOOK']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//html/body/table/tbody/tr/td/div/form/div/table/tbody/tr/td/input[@name='uploadSPECBOOK']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void downloadVerity4i(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@name='downloadVerity4i']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='downloadVerity4i']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void verity4iUpload(def webdriver,def log4j,def text)
		{
			log4j.info("Typing settingsVerity4iFile in text area");
			WebElement element = webdriver.findElement(By.xpath("//input[@id='settingsVerity4iFile']"))
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='settingsVerity4iFile']")));
			element.sendKeys(text);
		}
		
		
		public void btnUploadVerity4i(def webdriver,def log4j)
		{
			
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@name='uploadVerity4i']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='uploadVerity4i']")));
			executor.executeScript("arguments[0].click();", element);
			
		}
		
		
		public void scannerUpload(def webdriver,def log4j,def text)
		{
			log4j.info("Action is type and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@id='settingsScannerFile']"))
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='settingsScannerFile']")));
			element.sendKeys(text);
		}
		
		
		public void scannerMapFile(def webdriver,def log4j,def text)
		{
			log4j.info("Typing Scanner Map file path in text area");
			WebElement element = webdriver.findElement(By.xpath("//input[@id='scannerMapFile']"));
			log4j.info("Text Area Found and entering the text file path");
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@id='scannerMapFile']")));
			element.sendKeys(text);
		}
		

		public void nextButton(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//input[@name='next']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='next']")));
			executor.executeScript("arguments[0].click();", element);
		}
		public void backButton(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='back']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='back']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void specBackLink(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//a[@href='javascript:history.back()' and contains(text(),'Back')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[@href='javascript:history.back()' and contains(text(),'Back')]")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void viewLog(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='ProdViewLogBtn']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='ProdViewLogBtn']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void retrieveMR(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//form[@action='/mra/servlet/FileDownload']/input[@name='Retrieve MR']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//form[@action='/mra/servlet/FileDownload']/input[@name='Retrieve MR']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void genarateReport(def webdriver,def log4j)
		{
			WebElement element = webdriver.findElement(By.xpath("//a[contains(text(),'Generate Reports')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(),'Generate Reports')]")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void avlblWfrLayout(def webdriver,def log4j)
		{
			WebElement element = webdriver.findElement(By.xpath("//li/a[contains(text(),'Available Wafer Layouts')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			log4j.info("test");
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//li/a[contains(text(),'Available Wafer Layouts')]")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void layerCheckbox(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='LayerSelection' and @value='0RX']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='LayerSelection' and @value='0RX']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void clickHere(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//td[contains(text(),'Click')]/a[text()='here']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//td[contains(text(),'Click')]/a[text()='here']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void downloadScanner(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='download']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='download']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void scannerDownload(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='downloadScanner']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='downloadScanner']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void uploadScanner(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//tr[@id='scannerMapControls']/td/input[@value='Upload Scanner Map' and @name='upload']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@id='scannerMapControls']/td/input[@value='Upload Scanner Map' and @name='upload']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
		
		public void uploadScannerMap(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//tr[@id='scannerMapControls']/td/input[@name='uploadScanner']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[@id='scannerMapControls']/td/input[@name='uploadScanner']")));
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
		
		
		//Verify for the file exists or not.
		public String isFileExists(def webdriver, def log4j,def path) 
		{
		File file = new File(path);
			 return file.exists();
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
		
		
		public boolean isAvailable(def webdriver, def log4j,def listBox,def type,def text,def delimeter)throws Exception
		{
		WebElement select=null;
		String[] test1=null;
		String[] testDataValues=text.replace("\n","").split(delimeter);
		if(type.equalsIgnoreCase("id")){
			select = webdriver.findElement(By.id(listBox));
		}else if(type.equalsIgnoreCase("xpath")){
			select = webdriver.findElement(By.xpath(listBox));
		}else if(type.equalsIgnoreCase("name")){
			select = webdriver.findElement(By.name(listBox));
		}
		List<WebElement> options = select.findElements(By.tagName("option"));
		List<String> numbers=new ArrayList<String>();
		for(int count=0;count<options.size();count++){
			String value=options.get(count).getText();
			log4j.debug("Listbox option value: "+value);
			numbers.add(value.trim());
		}
//		if(numbers.size()==testDataValues.length){
				for(int i=0;i<testDataValues.length;i++){
				log4j.debug("verifying value :"+ testDataValues[i]);
//				log4j.debug("verifying value :"+ numbers.get(i));
					if(!numbers.contains(testDataValues[i])){
						return false;
					}
					}
//		}else{
//			return false;
//		}
		return true;
}
		
		
	
	//NEW
	
	public void checkCentering(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='doSiteCentering']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='doSiteCentering']")));
			executor.executeScript("arguments[0].click();", element);
		}
		
	public void checkClustering(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='doSiteClustering']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='doSiteClustering']")));
			executor.executeScript("arguments[0].click();", element);
		}
	
	
	public void checkGrouping(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='doSiteGrouping']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='doSiteGrouping']")));
			executor.executeScript("arguments[0].click();", element);
		}
	
	public void checkPatternRecognition(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='doPatternRecognition']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='doPatternRecognition']")));
			executor.executeScript("arguments[0].click();", element);
		}
	public void checkremoteMode(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='remoteMode']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='remoteMode']")));
			executor.executeScript("arguments[0].click();", element);
		}
	
	public void checksyntheticGA(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//input[@name='syntheticGA']"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='syntheticGA']")));
			executor.executeScript("arguments[0].click();", element);
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
	
	public void uploadSpreadsheet(def webdriver,def log4j,String data)
		{		
			WebElement element = webdriver.findElement(By.xpath("//input[@name='mrSpreadsheetGF_DTI_chiplet_llc']"));
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='mrSpreadsheetGF_DTI_chiplet_llc']")));	
			element.sendKeys(data);			
			
/*			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("By.xpath("//input[@name='mrSpreadsheetGF_DTI_chiplet_llc'].setAttribute('type','str')";
	*/	
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
	
	
	public void WritePropertiesFile(def webdriver,def log4j,String key,String Data) throws Exception
	{
	File file = new File(".\\resource\\GlobalFoundries\\Ems.properties");
	Properties pro = new Properties();
	pro.load(new FileInputStream(file));
	pro.setProperty(key, Data);
	FileWriter fw=new FileWriter(file);
	Set set=pro.keySet();
	Iterator i=set.iterator();
	StringBuffer s=new StringBuffer();
	while(i.hasNext()){
		String rkey=(String)i.next();
		s.append(rkey+"="+pro.getProperty(rkey)+"\n");
	}
	fw.write(s+"");
	fw.close();
	}
	
	public String ReadPropertiesFile(def webdriver,def log4j,String key)
		{
		
		String requiredValue="EMPTY";
		try{
		// File file = new File("D:\\MTQA\\GF_Applications\\MRA\\testData\\Helper.properties");
		File file = new File(".\\resource\\GlobalFoundries\\Ems.properties");
		
		FileInputStream fileInput = new FileInputStream(file);
		Properties properties = new Properties();
		properties.load(fileInput);	
		//System.out.println(properties.getProperty("MRA_UploadFunctionality5"));
		requiredValue=properties.getProperty(key);
		}catch(Exception e)
			{
				e.printStackTrace();
			}
			if(null==requiredValue){
				return "EMPTY";
			}else{
			
				return requiredValue;
			}
	
	}
	public String stringConcat(def webdriver,def log4j,String str1,String str2,String str3)throws Exception
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

	// Fetch only integer part from the MR number or OR number
	public int getMRNumber(def webdriver,def log4j,String str)throws Exception
	{
		String mainString=str.substring(2, str.length());
		log4j.info("String="+mainString);	
		int mrNum=Integer.valueOf(mainString).intValue();
		return mrNum ;
	}
	//Method to count number of checked Checkboxes on Design Based Overlay page for start wizard
	public int chkboxCount(def webdriver, def log4j)throws Exception
		{	
			WebDriverWait wait = new WebDriverWait(webdriver, 40);
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//table//tr[@style]")));
			return webdriver.findElements(By.xpath("//table//tr[@style]")).size();
		}
	
	// Compare two batch files
	public boolean compareBatchFiles(def webdriver, def log4j, String fileContent)throws Exception
	{
		
		WebElement element = webdriver.findElement(By.xpath("//textarea[@id='editBatchFile']"));
		boolean flag=false;
		WebDriverWait wait = new WebDriverWait(webdriver, 40);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//textarea[@id='editBatchFile']")));
		String str=element.getText();
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
	//Click on button pre-fill
	public void clickBtnPreFill(def webdriver, def log4j)throws Exception
	{
		WebElement element = webdriver.findElement(By.xpath("//input[@name='prefill']"));
		JavascriptExecutor executor = (JavascriptExecutor)webdriver;
		WebDriverWait wait = new WebDriverWait(webdriver, 40);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//input[@name='prefill']")));
		executor.executeScript("arguments[0].click();", element);
	}
}

	
	
