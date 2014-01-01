package main;

public class DataChecker {

	public boolean dataCheck(String data, String type)
	{
		if (type!=null)
		{
			try
			{
				switch(type)
				{
					case "B": return booleanCheck(data); 
					case "N": return numberCheck(data);
					case "S": return true;
					case "I": return true;
					case "D": return dateCheck(data);
					case "T": return timeCheck(data);
					case "L": return locationCheck(data);
					default: return true;
				}
			}
			catch (Exception e)
			{
				return false;
			}
		}
		else return true;
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
		int[] daysInMonth = {0,31,28,31,30,31,30,31,31,30,31,30,31};
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
		if (!digitCheck(data)) return false;
		if (data.length() == 6)
		{		
			int hour = Integer.parseInt(data.substring(0, 2));
			int minute = Integer.parseInt(data.substring(2, 4));
			int second = Integer.parseInt(data.substring(4, 6));
			return (hour>=0 && hour <=23 && minute>=0 && minute<=60 && second>=0 && second<=60);
		}
		else if (data.length() == 4)
		{
			int hour = Integer.parseInt(data.substring(0, 2));
			int minute = Integer.parseInt(data.substring(2, 4));
			return (hour>=0 && hour <=23 && minute>=0 && minute<=60);
		}
		else return false;
	}
	
	private boolean locationCheck(String data)
	{
		if (data.length()!=11) return false;
		char direction = data.charAt(0);
		if (direction!='E' && direction!='W' && direction!='N' && direction!='S') return false;
		int deg = Integer.parseInt(data.substring(1, 4));
		if (deg<0 || deg>180) return false;
		double minute = Double.parseDouble(data.substring(5,11));
		if (minute<0 || minute>=60) return false;
		return true;
	}
}
