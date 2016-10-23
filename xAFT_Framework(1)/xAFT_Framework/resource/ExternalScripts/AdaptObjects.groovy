import org.sikuli.script.Key;
import org.sikuli.script.KeyModifier;
import org.sikuli.script.Pattern;
import org.sikuli.script.Screen;
import org.sikuli.script.Settings;

public static void sikuliClick(def selenium, def log4j, String aftRootPath, String strImageName)
{
  Settings.BundlePath = aftRootPath + "\\objectRepository\\sikuliImages";
  Screen objScreen = null;

  Pattern objPattern = null;
  objScreen = new Screen();
  
  objPattern = new Pattern(strImageName+".png").similar((float)0.71).targetOffset(0,0);
  if(objScreen.exists(objPattern)!=null)
  {
   objScreen.click(objPattern);
   Thread.sleep(3000);
   log4j.info("Clicking the Image"+strImageName);
  }
  
   objScreen = null;
   objPattern = null;
   
 }

