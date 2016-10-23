import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Get absolute path
def getFilePath(def Selenium, def log4j, String filePath)
{
	File f=new File(filePath);
	return f.getCanonicalPath();
}



public static Boolean verifyRelatedObjInfo(def Selenium, def log4j,String actualObj, String expectedObj)throws Exception{
	String[] aObj=actualObj.split("\n");
	String[] eObj=expectedObj.split(";");
	List<String> actlObjects = new ArrayList<String>(Arrays.asList(aObj)); 
	if(aObj.length==eObj.length){
	for(int i=0;i<aObj.length;i++){
	if(!actlObjects.contains(eObj[i])){
	return false;
	}
	}
	}else{
	throw new Exception("actual("+aObj.length+") Objects count was not matched with expected("+eObj.length+") objects count ");
	}
	 
	return true;
}


		



