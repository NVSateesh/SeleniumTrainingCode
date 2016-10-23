import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

// Find whether a string contains a particular substring
def strFind(def selenium, def log4j,def source,def searchItem)
{
	log4j.info(source)
		if(source.toLowerCase().contains(searchItem.toLowerCase())){
			return true
		}
		else{
			return false
		}
}


def getFilePath(def Selenium, def log4j, String filePath)
{
	File f=new File(filePath);
	return f.getCanonicalPath();
}
		



