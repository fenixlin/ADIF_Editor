package main;

public class DataChecker {

	public boolean dataCheck(String data, String type, int length)
	{
		if (data.length()!=length) return false;
		switch(type)
		{
			case "B": return booleanCheck(data); 
			case "N": return numberCheck(data);
			case "S": return stringCheck(data);
			case "I": return true;
			case "D": return dateCheck(data);
			case "T": return timeCheck(data);
			default: return true;
		}
	}
	
	private boolean booleanCheck(String data)
	{
		if (data.length()>1) return false;
		return (data.equals("Y") || data.equals("N"));
	}
	
	private boolean digitCheck(String data)
	{
		char[] ch = data.toCharArray();
		for (int i=0; i<ch.length; i++)
		{
			if (ch[i]>'9' || ch[i]<'0') return false;
		}
		return true;
	}
	
	private boolean stringCheck(String data)
	{
		char[] ch = data.toCharArray();
		for (int i=0; i<ch.length; i++)
		{
			if (ch[i]<'\u0020' || ch[i]>'\u007E') return false;
		}		
		return true;
	}
	
	private boolean numberCheck(String data)
	{
		String tmp;
		if (data.startsWith("-")) tmp=data.substring(1);
		else tmp=data;
		int point = tmp.indexOf('.');
		if (point<0) return(digitCheck(data));
		else return(digitCheck(data.substring(0,point)) && digitCheck(data.substring(point+1)));
	}
	
	private boolean dateCheck(String data)
	{
		int[] daysInMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
		if (data.length() != 8) return false;
		if (!digitCheck(data)) return false;
		int year = Integer.parseInt(data.substring(0, 4));
		int month = Integer.parseInt(data.substring(4, 6));
		int day = Integer.parseInt(data.substring(6, 8));
		if (year<1930 || month>12 || month<1) return false;
		//leap year
		if (month == 2 && ((year%4==0 && year%100!=0) || year%400==0) ) return (day>=1 && day<=29);
		else return (day>=1 && day<=daysInMonth[month]);
	}
	
	private boolean timeCheck(String data)
	{
		if (data.length() != 6) return false;
		if (!digitCheck(data)) return false;
		int hour = Integer.parseInt(data.substring(0, 2));
		int minute = Integer.parseInt(data.substring(2, 4));
		int second = Integer.parseInt(data.substring(4, 6));
		return (hour>=0 && hour <=23 && minute>=0 && minute<=60 && second>=0 && second<=60);
	}
}
