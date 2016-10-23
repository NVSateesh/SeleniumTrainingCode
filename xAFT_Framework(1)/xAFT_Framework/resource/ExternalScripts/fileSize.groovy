import java.io.*;
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