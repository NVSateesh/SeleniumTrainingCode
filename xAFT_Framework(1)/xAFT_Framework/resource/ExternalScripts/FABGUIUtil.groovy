import java.util.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.Format;
import java.text.SimpleDateFormat;

// THIS METHOD WILL CHECK THE SEARCH STRING CONTAINS IN THE MAIN STRING OR NOT
public static boolean stringCompare(def Selenium, def log4j, String mainString,String searchString)
{
	boolean flag=mainString.contains(searchString);
	return flag;
}

// THIS METHOD WILL RETRUNG THE NUMBER OF ROWS AFTER DEDUCTING SOME OF THE ROW COUNT
public static String decreaseRowCount(def Selenium, def log4j, String mainString,String noofrows)
{
	int rowcount,rowscountdecrease,finalrowcount;
	log4j.info(mainString+"sdfsdf sdf ds fd fsd fdf ddfs");
	rowcount=Integer.ParseInt(mainString);
	rowscountdecrease=Integer.ParseInt(noofrows);
	finalrowcount=rowcount-rowscountdecrease;
	return finalrowcount+"";
}

// THIS METHOD WILL RETRUNG THE NUMBER OF ROWS AFTER DEDUCTING SOME OF THE ROW COUNT
public static String increaseRowCount(def Selenium, def log4j, String mainString,String noofrows)
{
	int rowcount=Integer.ParseInt(mainString);
	int rowscountincrease=Integer.ParseInt(noofrows);
	int finalrowcount=rowcount+rowscountincrease;
	return finalrowcount+"";
}

//This method split main string and storing the substring into temp variable
public static String splitString(def Selenium, def log4j, String mainString,String delimeter)
{
                String[] temp=mainString.split(delimeter);
				def subString1=temp[0]+"";
				log4j.info("FirstsubString value is:"+subString1);
                return subString1;
}


//This method split main string and storing the substring into temp variable
public static String replaceString(def Selenium, def log4j, String mainString,String delimeter,String repstr)
{
                String temp=mainString.replace(delimeter,repstr);
				log4j.info("Replace String value is:"+temp);
                return temp;
}

//This method split main string and storing the substring into temp variable
public static String getSubString(def Selenium, def log4j, String mainString,String delimeter)
{
                String temp=mainString.substring(0, mainString.lastIndexOf(delimeter));
				log4j.info("Replace String value is:"+temp);
                return temp;
}


//This method for getting the spec id from the given String
public static String getSpecID(def Selenium, def log4j, String mainString)
{
                String[] sub = mainString.split(" ");
				String text = sub[1];		
				log4j.info("SubString value is:"+text);
                return (text.replace("<", "").replace(">", ""));
}

//This  method for searching the given string
public static boolean StringSearch(def Selenium, def log4j,String search)
{
	String sublottypes="QF "+"QD "+"QE "+"QX "+"MW ";
	boolean result=false;
	if(sublottypes.contains(search)) {
		result = true;
	}
	log4j.info(result);
	return result;
}

//This method is used to search the string in specific line of the history content
	public static boolean verifyHistory(def Selenium, def log4j,String cellvalue,String targetline,String departments)
	{
		ArrayList<String> approvedDepts=new ArrayList<String>();
			String[] sentence=cellvalue.split("\n");	
			for(String sen:sentence){
				if(sen.contains(targetline)){
					String aDeptsLine=sen.substring(sen.indexOf(":")+1,sen.length()).trim();
					if(targetline.contains("AutoApproved")){
						String[] aDepts=aDeptsLine.split(", ");
						for(String d:aDepts)
							approvedDepts.add(d.trim());
					}else{
						String aDepts=aDeptsLine.split(", ")[0];
						approvedDepts.add(aDepts);
					}
				}
				}

			String[] depts=departments.split("\n");
			
			for(String dept:depts)
			{
				if(!approvedDepts.contains(dept.trim())){
					log4j.info(dept+" Department is not exist in History");
					return false;
				}
			}
				return true;
	 }

//This method is used to compare the previous date with current date
public static boolean  isPreviousDate(def Selenium, def log4j,String previousdate,String formate)
	{
		try{
			DateFormat dateFormat = new SimpleDateFormat(formate);
			Date date = new Date();
			String currentdate=dateFormat.format(date);
			System.out.println(currentdate);

			Date prevdate = dateFormat.parse(previousdate);

			if(prevdate.before(date)){
				return true;
			}
		}
		catch(ParseException ex){
			ex.printStackTrace();
		}
		return false;
	}
//This method returns the depart name by splitting the given approvalName (read from task tab) 
public static String getDeptName(def Selenium, def log4j,String approvalName){
	String dName="null";
	try{
		log4j.info("Approval name from task Tab: "+approvalName);
		Pattern p = Pattern.compile("\\[(.*?)\\]");
		Matcher m = p.matcher(approvalName);
		m.find();
		dName=m.group(1);
		log4j.info("Department name after splitting Approval name (which was read from task Tab): "+dName);
		}	
		catch(Exception ex){
			ex.printStackTrace();
		}
		return dName;
			
}
//This method verifies all the department names are exist in the test data sheet 
public static boolean verifyModuleApprovals(def Selenium, def log4j,String allDepts,String cellData)
	{
		boolean flag=true;
		try{
				log4j.info("Splitting and adding  allDepts to ArrayList:"+allDepts);
				List<String> depts=new ArrayList<String>();
				String[] dep=allDepts.split("\n");
				for(String d:dep){
					depts.add(d);
				}
				
				String[] strings=cellData.split(",");
				
				for(String s:strings){
					if(depts.contains(s)){
							depts.remove(s);
						}else{
							log4j.warn(s+" department not mentioned in the test data");
							flag=false;
						}
					
				}
				if(depts.size()>0){
					log4j.info("All depts infromation not available in the history. Those are::");
					for(String d:depts){
					log4j.info(d);
					}
					return false;
				}
		}catch(Exception ex){
			ex.printStackTrace();
			flag=false;
		}
		
			return flag;
	}
	//This method perform click operation on specified Key
	public static boolean clickOnKey(def Selenium, def log4j, String keyName) throws AWTException
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
			
		}else{
			log4j.info("Parsed "+keyName+" key not exist ");
			flag=false;
		}		
		return flag;
	}
	//This method Copy the given string into clipboard and paste it in specified location
	public static void copyAndPasteData(def Selenium, def log4j,String text) throws AWTException{
		Robot robot;
		robot=new Robot();
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSelection = new StringSelection(text);
		log4j.info("Copied "+text+" text into Clipboard");
		clipboard.setContents(stringSelection, null);
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
		robot.keyPress(KeyEvent.VK_CONTROL);
		robot.keyPress(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_V);
		robot.keyRelease(KeyEvent.VK_CONTROL);
		//robot.keyPress(KeyEvent.VK_ENTER);
		//robot.keyRelease(KeyEvent.VK_ENTER);
	
	}
	//This  method for getting the middle of string
public static String getMiddleOfString(def Selenium, def log4j, String mainString)
{
                String temp=mainString.substring(mainString.lastIndexOf("["), mainString.lastIndexOf("]"));
				log4j.info("Replace String value is:"+temp);
                return temp;
}
//This method for getting the previous date
public static String getPreviousDate(def Selenium, def log4j)
{
       Calendar cal = Calendar.getInstance();
	   cal.add(Calendar.DATE, -1);
	   log4j.info("Previous date is"+new SimpleDateFormat("MMM dd, yyyy").format(cal.getTime()));
	   return new SimpleDateFormat("MMM dd, yyyy").format(cal.getTime());
}
	
//This method is used to search the string in specific line of the history content
public static boolean verifyHistoryData(def Selenium, def log4j,String cellvalue,String targetline,String departments)
{
	List<String> approvedDepts=new ArrayList<String>();
	String[] sentence=cellvalue.split("\n");	
	boolean flag=true;
	for(String sen:sentence){
		if(sen.contains(targetline)){
			String aDeptsLine=sen.substring(sen.indexOf(":")+1,sen.length());
			log4j.info("Splitting the text using : character..." +aDeptsLine);
			String[] aDepts=aDeptsLine.split(", ");
			approvedDepts=Arrays.asList(aDepts);
		}
	}
	String[] depts=departments.split("\n");
	for(String dept:depts)
	{
		log4j.info("Department:" + dept);
		if(approvedDepts.contains(dept)){
			//approvedDepts.remove(dept);
			log4j.info(dept + " department exist in the History ");
		}else{
			flag=false;
			log4j.info(dept + " department not exist in the History ");
		}
	}
	return flag;
}

//This method returns the cell_Data size/Cell data count
	public static int getCellDataCount(def Selenium, def log4j,String cellData)
	{				
		if(cellData.trim()=="")
			return 0;
		else
			return cellData.split("\n").length;
	}
	
	//This method split the given text based on condition and returns the value based on index
	public static String getValByIndexFromGivenData(def Selenium, def log4j,String text, String index)
	{				
		
		try{
			int i=Integer.parseInt(index);
			String[] str= text.split("\n");
			log4j.info("index ["+i+"] value="+str[i]);
			return str[i];
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	//This method returns the matched text line from given data by comparing each and every line with searchText 
	public static String getMatchedTextLine(def Selenium, def log4j,String text,String searchText)
	{
		log4j.info("Actual text :"+ text);
		String[] str=text.split("\n");
		for(String s:str){
			if(s.contains(searchText))
				if(s.contains("from"))
					return s.split("] from")[0];
				else
					return s;
		}
		return " ";
	}
	//This method is to check whether the String is empty or not.	

public static boolean isEmpty(def Selenium, def log4j,String Value)
{
	return Value.isEmpty();
}
	