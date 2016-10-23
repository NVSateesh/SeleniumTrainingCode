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
import java.text.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import com.thoughtworks.selenium.Selenium;
import org.openqa.selenium.WebDriverBackedSelenium;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
	public class LinkCount
	{
		//Method to count number of links for Wafer Layout revision
		public int linkCountRevision(def webdriver, def log4j)throws Exception
		{
			return webdriver.findElements(By.xpath("//td[contains(text(),'Revision') or contains(text(),'Rev')]/following-sibling::td/div/ul/li/a")).size();
		}
		
		//Method to count number of links for Wafer Layout Name
		public int linkCountName(def webdriver, def log4j)throws Exception
		{
			return webdriver.findElements(By.xpath("//td[contains(text(),'Name') or contains(text(),'Layer Name')]/following-sibling::td/div/button/../ul/li/a")).size();
		}
		
		//Method to count number of links for Wafer Layout Test Area
		public int linkCountTestArea(def webdriver, def log4j)throws Exception
		{
			return webdriver.findElements(By.xpath("//td[contains(text(),'Test Area')]/following-sibling::td/div/button/../ul/li/a")).size();
		}
		
		public void buttonTestArea(def webdriver,def log4j)
		{
			log4j.info("Action is clickButton and the parameter is ");
			WebElement element = webdriver.findElement(By.xpath("//td[contains(text(),'Test Area')]/following-sibling::td/div/button/span[1]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);
		}
		
		public void typeRC(def webdriver, def log4j, def value){
		
			Selenium selenium = new WebDriverBackedSelenium(webdriver, "");
			selenium.type("//div[@class='modal hide in']//h4/..//input[@data-bind='value: from_radius']",value);
		
		}
		
		public boolean isContainsText(def webdriver, def log4j, String str1, String str2){
		
			return str1.contains(str2);
		
		}
		public void clearTextGroovy(def webdriver,def log4j) throws Exception{
			Robot robot = new Robot();
			
			robot.keyPress(KeyEvent.VK_END);
			robot.keyRelease(KeyEvent.VK_END);
			
			for(int i=1;i<=10;i++){
			   log4j.info("Pressing Backspace");
				robot.keyPress(KeyEvent.VK_BACK_SPACE);
				robot.keyRelease(KeyEvent.VK_BACK_SPACE);
			}
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_A);
			try{
			Thread.sleep(1000);
			}catch(Exception e){
			}
			robot.keyRelease(KeyEvent.VK_A);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			try{
			Thread.sleep(1000);
			}catch(Exception e){
			}
			robot.keyPress(KeyEvent.VK_DELETE);
			robot.keyRelease(KeyEvent.VK_DELETE);
			try{
			Thread.sleep(1000);
			}catch(Exception e){
			}
			
		}
		//Method for handling pop up which accepts text input
		public void promptAlert(def webdriver, def log4j,def text)throws Exception
		{
			log4j.info("promptAlert:::::::"+text);
			Alert myAlert = webdriver.switchTo().alert();
			myAlert.sendKeys(text);
			myAlert.accept();	
		}
		
		//Method for handling pop up which accepts text input
		public void promptAlertCancel(def webdriver, def log4j,def text)throws Exception
		{
			log4j.info("promptAlert:::::::"+text);
			Alert myAlert = webdriver.switchTo().alert();
			myAlert.sendKeys(text);
			myAlert.dismiss();
			webdriver.switchTo().defaultContent();		
		}
		
		//Method to suffix unique number after text
		public void promptAlert(def webdriver, def log4j,def text,def uniqueNumber)throws Exception
		{
			Alert myAlert = webdriver.switchTo().alert();
			myAlert.sendKeys(text+uniqueNumber);
			myAlert.accept();
		}
		
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
		//Method for accepting pop up alert
		public String getAlertText(def webdriver, def log4j)throws Exception
		{
			Alert myAlert = webdriver.switchTo().alert();
			String PopupText=myAlert.getText();
			myAlert.accept();
			return PopupText;
		}
	    
		public void fitWaferButton(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//div[@class='right-pane']// button[contains(text(),'Fit Wafer')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);
		}
		public void editEdge(def webdriver,def log4j)
	   {
		  log4j.info("Action is clickButton and the parameter is ");
	      WebElement element=webdriver.findElement(By.xpath("//div/button[contains(text(),'Edit Edge') and contains(text(),'Flat Clearance')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);
	   }
	   
		public void createSort(def webdriver,def log4j)
	   {
		  log4j.info("Action is clickButton and the parameter is ");
	      WebElement element=webdriver.findElement(By.xpath("//i/../../button[contains(text(),'Create New Sort Map')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);
	   }
	   
	   public void createWet(def webdriver,def log4j)
	   {
		  log4j.info("Action is clickButton and the parameter is ");
	      WebElement element=webdriver.findElement(By.xpath("//i/../../button[contains(text(),'Create New WET Map')]"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);
	   }
		
		public void newLoad(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//div/p/span[contains(text(),'NEW_LOAD')]/../.."));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);
		}
		
		public void selectName(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//td[text()='Name']/../td/div/button"));
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);
		}
		
		public void addRegion(def webdriver,def log4j)
		{
			
			WebElement element = webdriver.findElement(By.xpath("//a[contains(text(),'add region name')]"));
	//		new Actions(webdriver).moveToElement(element).click().perform();
			JavascriptExecutor executor = (JavascriptExecutor)webdriver;
			executor.executeScript("arguments[0].click();", element);	
		}
		//To Get file path by providing partial path to the directory
		public static String getFilePath(def Selenium, def log4j, String filePath)
		{
				File f=new File(filePath);
				return f.getCanonicalPath();
		}
		
		//To return Recent modified file in a directory for example recent downloaded file
		
		public File lastFileModified(def webdriver, def log4j,def path) 
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
		
		//This method is to parse a .csv file and return particular String from that file.
		//This is used in Export functionality for UCS application.
		
		public String getMasksetSk(def webdriver, def log4j,def filePath) throws IOException
		{
			String content;
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			ArrayList<Object> lines = new ArrayList<Object>();
			String line = null;
			while ((line = reader.readLine()) != null) 
			{
				lines.add(line);
			}
		
				String Str = new String((String) lines.get(1));
			//System.out.println(lines.get(1));
			//System.out.println(Str.substring(0, 8) );
			return content = Str.substring(0, 8);
		}
		
		//This method is to parse a .csv file and return particular String from that file.
		//This is used in Export functionality for UCS application.
		
		public String getwaferLayout(def webdriver, def log4j,def filePath) throws IOException
		{
			String content1;
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			ArrayList<Object> lines = new ArrayList<Object>();
			String line = null;
			while ((line = reader.readLine()) != null) 
			{
				lines.add(line);
			}
		
				String Str = new String((String) lines.get(1));
			//System.out.println(lines.get(1));
			//System.out.println(Str.substring(9, 16) );
			return content1 = Str.substring(9, 16);
		}
		
		public String fetchwaferLayout(def webdriver, def log4j,def filePath) throws IOException
		{
			String content1;
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			ArrayList<Object> lines = new ArrayList<Object>();
			String line = null;
			while ((line = reader.readLine()) != null) 
			{
				lines.add(line);
			}
		
				String Str = new String((String) lines.get(1));
			//System.out.println(lines.get(1));
			//System.out.println(Str.substring(9, 15) );
			return content1 = Str.substring(9, 15);
		}
		public static int compareDate(def webdriver, def log4j,String Date1, String Date1Format,String Date2, String Date2Format){
        try {
                    Date d1 = new SimpleDateFormat(Date1Format).parse(Date1);
                    Date d2 = new SimpleDateFormat(Date2Format).parse(Date2);
                                        
                    return d2.compareTo(d1);
        } catch (ParseException e) {
                    log4j.info("Exception occured while parsing" + e.getMessage());
                    return -2;
        } 
	}
	public static int getRandomNum(def webdriver, def log4j,String digits){
	
		int digit=Integer.parseInt(digits);
		Random r=new Random();
		int r1=(int) Math.pow(10, digit);
		
		return r.nextInt(r1);
	}
	
	public static void selectNewWindow(def webdriver, def log4j) throws Exception{
		try{ 
			String[] windows=(String[]) webdriver.getWindowHandles().toArray();
			webdriver.switchTo().window(windows[windows.length-1]);
			
		}catch (WebDriverException e) {
				log4j.info(e.message());
				throw new WebDriverException(e);
		} catch (Exception e) {
				throw new Exception(e);
		}
	}
	

	}