import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.lang.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;


public static int fileSize(def selenium, def log4j,String filePath)throws Exception{
	int fileSizeKB;
	File f=new File(filePath);
	if(f.exists()){
		long fileSize=f.length();
		fileSizeKB=(int)fileSize/1024;
		log4j.info(filePath+" size in KB: "+fileSizeKB);
	}else{
		log4j.warn(filePath+" not found!");
		throw new Exception(filePath+" not found!");
	}
	return fileSizeKB;
}


public boolean isAscending(def webdriver, def log4j,String listBox,String type)throws Exception{
		WebElement select=null;
		if(type.equalsIgnoreCase("id")){
			select = webdriver.findElement(By.id(listBox));
		}else if(type.equalsIgnoreCase("xpath")){
			select = webdriver.findElement(By.xpath(listBox));
		}else if(type.equalsIgnoreCase("name")){
			select = webdriver.findElement(By.name(listBox));
		}
		List<WebElement> options = select.findElements(By.tagName("option"));
		List<Double> numbers=new ArrayList<Double>();
		for(int count=0;count<options.size();count++){
			String value=options.get(count).getText();
			log4j.debug("Listbox option value: "+value);
			String[] specificValue=value.split("\\(");
			numbers.add(Double.parseDouble(specificValue[1].replace("%", "").replace(")", "").trim()));
		}
		Double[] numbers1=numbers.toArray(new Double[numbers.size()]);
		Arrays.sort(numbers1);
		//Arrays.sort(numbers1,Collections.reverseOrder());
		List<Double> sortedNumbers=Arrays.asList(numbers1);
		for(int i=0;i<sortedNumbers.size();i++){
		log4j.info("Actutal :"+numbers.get(i)+" -> Sorted :"+sortedNumbers.get(i));
			if(sortedNumbers.get(i)!=numbers.get(i)){
				return false;
			}
		}
		return true;
}

public boolean isDesnding(def webdriver, def log4j,def listBox,def type)throws Exception{
		WebElement select=null;
		String[] test1=null;
		if(type.equalsIgnoreCase("id")){
			select = webdriver.findElement(By.id(listBox));
		}else if(type.equalsIgnoreCase("xpath")){
			select = webdriver.findElement(By.xpath(listBox));
		}else if(type.equalsIgnoreCase("name")){
			select = webdriver.findElement(By.name(listBox));
		}
		List<WebElement> options = select.findElements(By.tagName("option"));
		List<Double> numbers=new ArrayList<Double>();
		for(int count=0;count<options.size();count++){
			String value=options.get(count).getText();
			log4j.debug("Listbox option value: "+value);
			test1=value.split("\\(");	
			numbers.add(Double.parseDouble(test1[1].replace("%", "").replace(")", "").trim()));
		}
		Double[] numbers1=numbers.toArray(new Double[numbers.size()]);
		Arrays.sort(numbers1,Collections.reverseOrder());
		List<Double> sortedNumbers=Arrays.asList(numbers1);
		for(int i=0;i<sortedNumbers.size();i++){
		log4j.info("Actutal :"+numbers.get(i)+" -> Sorted :"+sortedNumbers.get(i));
			if(sortedNumbers.get(i)!=numbers.get(i)){
				return false;	
		}
		}
		return true;
}


public boolean isAvailable(def webdriver, def log4j,def listBox,def type,def text,def delimeter)throws Exception{
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

public int getXpathCount(def webdriver, def log4j,def xpath){
	log4j.info("The xpath of the required attribute is: "+xpath);
	List<WebElement> element=webdriver.findElements(By.xpath(xpath));
	return element.size();
	
}
// THIS METHOD WILL CHECK THE SEARCH STRING CONTAINS IN THE MAIN STRING OR NOT
public String stringConcatenate(def webdriver, def log4j, String str1,String str2,String str3)
{
                log4j.info("Parameters are:"+str1+", "+str2+", "+str3);
				String requiredStr1=str1.replace("-","");
				String mStr1=requiredStr1.replace(".","");
				String requriedStr2="";
				if(str2.equalsIgnoreCase("Standard Deviation")){
					requriedStr2="std_dev";
				}
				
				String[] requriedStr3=str3.split("-");
				return mStr1.trim()+"_"+requriedStr3[0].trim()+"_"+requriedStr2.trim();
}
// THIS METHOD WILL CHECK THE TWO STRINGS ARE EQUALS ARE NOT
public boolean isStringsEquals(def webdriver, def log4j, String str1,String str2)
{
                log4j.info("Parameters are:"+str1+", "+str2);
				if(str2.equals(str1)){
					return true;
				}				
				else{
				return false;
				}
}
public void navigate(def webdriver, def log4j)
{
                log4j.info("Parameters are:"+str1+", "+str2);
				webdriver.navigate().to("http://globalconnect.gfoundries.com/newyork/PIYE/tecnet/Pages/default.aspx")
}

                          