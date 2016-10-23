import java.text.*
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.Alert;
import com.ags.aft.webdriver.common.AFTSeleniumBase;

//THIS GROOVY FILE CONTAINS FILE UTIL METHODS.

public class RVAUtil{

	// THIS METHOD WILL CHECK FOR THE EXISTENCE OF THE FILE AND RETURN TRUE/FALSE
	public static boolean exists(def Selenium, def log4j, String filePath){
		File file = new File(filePath);
		boolean fileExists;
		//CHECKING THE EXISTENCE OF FILE
		fileExists = file.exists();
		if(!fileExists){
			log4j.info("The Specified file "+ filePath +"you are searching doesn't exist");
		}
		else{
			log4j.info("The Specified file "+ filePath +" you are searching does exist");
		}
		return fileExists;
	}
	
	// To find a string in a given string
	public static boolean strFind(def selenium, def log4j,def source,def searchItem){
		//log4j.info("Find an Item in a String")
		if(source.toLowerCase().contains(searchItem.toLowerCase()))	
			return true;
		else	
			return false;
	}
	
	// To verify the content of a given string
	public static boolean verifyContent(def selenium, def log4j,def actString,def searchString){
		
		log4j.info("Actual String: "+actString+" Search String: "+searchString);
		//Splitting the search String with ','
		String[] strArray = searchString.split(",");
		for(int i=0; i<strArray.size(); i++){
			if(!strFind(selenium, log4j, actString,strArray[i])){
				log4j.info(strArray[i]+" is present in the Actual String: "+actString); 
				return false;
			}
		}		
		return true;
	}
	
	//This method will return CurrentTime
	public static String getCurrentTime(def selenium, def log4j,String s){
		TimeZone.setDefault(TimeZone.getDefault());
		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		Calendar c=Calendar.getInstance();
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(c.getTime());
	}
	
	//This method will retun required date format
	public static Calendar getCalendar(String date) throws Exception{
		Calendar cal=Calendar.getInstance();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		cal.setTime(sdf.parse(date));
		return cal;
		
	}
	
	// This method will return the current test result row number
	public static String getRowNumber(def webdriver,def log,String s){
	// TODO Auto-generated method stub
		int counttime=0;
		log.info("Method getRowNumber start");
		Calendar cal1=getCalendar(s.trim());
		List<WebElement> elements=null;
		while(counttime<4){
			elements=webdriver.findElements(By.xpath("//div[3]/div/div[4]/div/div[3]/div/div[2]/div/div/table/tbody/tr"));
			if(counttime.size()>0){
				counttime=5;
			}else{
				Thread.sleep(3000);
			}
		}
		counttime=0;
		if(elements!=null){
		while(counttime<3){
			log.info("Element Size: "+elements.size());
			//for(int i=1;i<=elements.size();i++){
				WebElement element=webdriver.findElement(By.xpath("//div[3]/div/div[4]/div/div[3]/div/div[2]/div/div/table/tbody/tr[1]/td[2]/div"));
				Calendar cal2=null;			
				try{
					cal2=getCalendar(element.getText().trim());
					if(cal1.before(cal2) || cal1.equals(cal2)){					
						counttime=4;
						return 1+"";
					}
				}catch(Exception e){
			//		i--;
				}			
			//}
			counttime++;
			Thread.sleep(3000);
		}
		}
	}
	
	//This method will retun difference between two date/times
	public static String getDiff(def selenium,def log,String sdate, String edate){		
		Date sdt, edt;
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		sdt = (Date) sdf.parse(sdate);
		edt = (Date) sdf.parse(edate);
		
		long diff = edt.getTime() - sdt.getTime(); 
		long diffSecs = diff/1000; 
		long diffMins = diffSecs/60; 
		long diffHrs = diffMins/60; 
		long hours=0,min=0,secs=0;
		if(diff>0){
			hours=diff/(60*60*1000);
			long bal=diff-(hours*60*60*1000);
			
			if(bal>0){
				min=bal/(60*1000);
			
				long minbal=bal-(min*60*1000);
				if(minbal>0){
					secs=minbal/1000;
				}
			}
		}
		log.info("Diff in minutes: "+hours+"h:"+min+"m:"+secs+"s");
		return hours+"h:"+min+"m:"+secs+"s";
		
		
		/*log.info("Execution time diff: "+diffMillis);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:MM");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(diffMillis);
		return calendar.get(calendar.HOUR)+":"+calendar.get(calendar.MINUTE);
		
		Date diff = dateFormat.format(calendar.getTime())
		log.info("Final Difference to print: "+diff); 
		return diff.toString();		*/
	}
	
	//This method will accept confirmation dialogue
	public static String getConfirmation(def selenium, def log4j, String action) throws Exception{
			Alert alert = null;
			String alertStatus = null;
		try {
			alert = AFTSeleniumBase.getInstance().getDriver().switchTo()
					.alert();
			log4j.info("Clicking on alert dialogue");
			if (action.toLowerCase().contains("ok")) {
				alert.accept();
				alertStatus = "OK";
				log4j.info("Clicked on Alert OK button");
			} else if (action.toLowerCase().contains("cancel")) {
				alert.dismiss();
				alertStatus = "Cancel";
				log4j.info("Clicked on Alert CANCEL button");
			} else {
				alertStatus = "Cancel";
				log4j.info("Invalid input action specified for Alert!");
			}
		} catch (Exception e) {
			log4j.info("Exception::::::::::::::::::::::::::::::::::::::::::");
		}

		return alertStatus;	
	}
}
