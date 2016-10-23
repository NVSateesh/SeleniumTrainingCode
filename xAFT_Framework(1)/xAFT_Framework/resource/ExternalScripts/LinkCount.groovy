	import java.io.*;
	import org.openqa.selenium.*;
	import java.util.*;
	
	public class LinkCount
{
	public int linkCount(def webdriver, def log4j)throws Exception
	{
		return webdriver.findElements(By.xpath("//td[contains(text(),"Revision") or contains(text(),'Rev')]/following-sibling::td/div/ul/li/a")).size();
	}	
}