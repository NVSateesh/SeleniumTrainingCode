import java.util.*;
import java.io.*;

// THIS METHOD WILL RETURN THE CURRENT DAY
public static String getCurrentDay(def Selenium, def log4j)
{
	return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"";
}

// THIS METHOD WILL CHECK THE SEARCH STRING CONTAINS IN THE MAIN STRING OR NOT
public static boolean stringCompare(def Selenium, def log4j, String mainString,String searchString)
{
	boolean flag=mainString.contains(searchString);
	return flag;
}


// THIS METHOD WILL CHECK THE SEARCH STRING CONTAINS IN THE MAIN STRING OR NOT
public static String getGeneratedCPName(def Selenium, def log4j, String str1,String str2)
{
	log4j.info("Concat String is:"+str1+" - "+str2);	
	return str1+" - "+str2;
}

// THIS METHOD WILL CHECK THE SEARCH STRING CONTAINS IN THE MAIN STRING OR NOT
public static String stringConcatenate(def Selenium, def log4j, String str1,String str2)
{
	log4j.info("Concat String is:"+str1+str2);
	return str1+str2;
}

// THIS METHOD WILL RETURN THE FILE SIZE
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

// THIS METHOD WILL CHECK FOR THE EXISTENCE OF THE FILE AND RETURN TRUE/FALSE
public static boolean exists(def Selenium, def log4j, String filePath){
	Thread.sleep(30000);
	File file = new File(filePath);
	log4j.info(file.getName());
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

public static String decreaseRowCount(def Selenium, def log4j, String mainString,String noofrows)
{
	log4j.info(mainString+"sdfsdf sdf ds fd fsd fdf ddfs");
	int rowcount=Integer.ParseInt(mainString);
	int rowscountdecrease=Integer.ParseInt(noofrows);
	int finalrowcount=rowcount-rowscountdecrease;
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
public static String noOfSubStrings(def Selenium, def log4j, String mainString,String delimeter)
{
                String[] temp=mainString.split(delimeter);
                log4j.info("Length of temp array "+temp.length);
				return temp.length+"";
				
}


//This method split main string and storing the substring into temp variable
public static String getStringLength(def Selenium, def log4j, String mainString)
{
                String temp=mainString.length();
                log4j.info("Length of String  "+temp);
				return temp+"";
				
}

//Get the file path
public static String getFilePath(def Selenium, def log4j, String filePath){
	File f=new File(filePath);
	return f.getCanonicalPath();
}
