import java.util.*;
import java.io.*;

// THIS METHOD WILL RETURN THE CURRENT DAY
public static String getCurrentDay(def Selenium, def log4j)
{
	return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"";
}

public static String getFutureDay(def Selenium, def log4j)
{
	Calendar c=Calendar.getInstance();
	c.add(Calendar.DATE,1);
	return c.get(Calendar.DAY_OF_MONTH)+"";
}

// THIS METHOD WILL CHECK THE SEARCH STRING CONTAINS IN THE MAIN STRING OR NOT
public static String stringCompare(def Selenium, def log4j, String mainString,String searchString)
{
	boolean flag=mainString.contains(searchString);
	return flag+"";
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
