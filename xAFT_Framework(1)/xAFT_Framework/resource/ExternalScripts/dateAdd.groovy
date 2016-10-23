

import java.util.*;
import java.text.*;


//Method to add minutes 	
public static String addMinutes(def QTP, def log4j, String date_time, String mins)
{
			Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            cal.setTime(sdf.parse(date_time));
            cal.set(cal.MINUTE,cal.get(Calendar.MINUTE)+Integer.parseInt(mins));
            log4j.info((cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
			return ((cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
}


//Method to add seconds
public static String addSeconds(def QTP, def log4j, String date_time, String secs)
{
			Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            cal.setTime(sdf.parse(date_time));
            cal.set(cal.SECOND,cal.get(Calendar.SECOND)+Integer.parseInt(secs));
            log4j.info((cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
			return ((cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
}

//Method to add months
public static String addMonths(def QTP, def log4j, String date_time, String months)
{
			Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            cal.setTime(sdf.parse(date_time));
			cal.set(cal.MONTH,cal.get(Calendar.MONTH)+Integer.parseInt(months));
            log4j.info((cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
			return ((cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));

}

//Method to add days
public static String addDays(def QTP, def log4j, String date_time, String days)
{
			Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            cal.setTime(sdf.parse(date_time));
            cal.set(cal.DAY_OF_MONTH,cal.get(Calendar.DAY_OF_MONTH)+Integer.parseInt(days));
            log4j.info((cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
			return ((cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
}

//Method to change the date format
public static String xsiteDateFormat(def QTP, def log4j, String date_time)
{
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy H:mm:ss");
			Calendar cal = Calendar.getInstance();
			Date today = df.parse(date_time);
			cal.setTime(today);
			String day=cal.get(Calendar.DAY_OF_MONTH)+"";
			String month=(cal.get(Calendar.MONTH)+1)+"";
			String hour=cal.get(Calendar.HOUR)+"";
			String min=cal.get(Calendar.MINUTE)+"";
			String sec=cal.get(Calendar.SECOND)+"";
			if(month.length()==1)
			{
				month=0+month;
			}
			if(day.length()==1)
			{
				day=0+day;
			}
			if(hour.length()==1)
			{
				hour=0+hour;
			}
			if(month.length()==1)
			{
				month=0+month;
			}
			if(sec.length()==1)
			{
				sec=0+sec;
			}
			String requiredDate=cal.get(Calendar.YEAR)+"-"+month+"-"+day+" "+hour+":"+min+":"+sec;
			return requiredDate;
}



