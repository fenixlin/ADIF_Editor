package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class ConfigLoader {
	
	//默认里面的Key都是Lowercase
	private static HashMap<String, String> QSOType = new HashMap<String, String>();//All upper-case
	private static HashMap<String, ArrayList<String>> enumList = new HashMap<String, ArrayList<String>>();//Detailed enumeration this program support
	
	private static final String ENUM_FILE = "Enumerations.dat";
	private static final String QSOFIELD_FILE = "QSOFieldType.dat";

	public static void loadData()
	{
		File file = null;
		Scanner scanner = null;
		try
		{
			file = new File(ENUM_FILE);
			if (!file.exists()) throw(new Error());
			
			scanner = new Scanner(file);
			scanner.useDelimiter("[\\n\\r]+");
			while (scanner.hasNext())
			{
				int i = scanner.nextInt();
				String name = scanner.next().toUpperCase();
				ArrayList<String> list = new ArrayList<String>();
				while (i-->0)
				{
					String enumValue = scanner.next();
					list.add(enumValue);
				}
				enumList.put(name, list);
			}
			
			file = new File(QSOFIELD_FILE);
			if (!file.exists()) throw(new Error());
			scanner.close();

			scanner = new Scanner(file);
			while (scanner.hasNext())
			{
				String fieldName = scanner.next().toUpperCase();
				String fileType = scanner.next();
				QSOType.put(fieldName, fileType);
			}
		}
		catch (Exception e)
		{
			throw(new Error("Data file open failure!"));
		}
		finally
		{
			scanner.close();
		}
		//从文件读取数据
	}
	
	public ArrayList<String> getEnumList(String key)
	{
		if (enumList.containsKey(key.toUpperCase()))
		{
			return enumList.get(key.toUpperCase());
		}
		else
		{
			return null;
		}
	}

	public String getQSOType(String key)
	{
		String upperKey = key.toUpperCase();
		if (QSOType.containsKey(upperKey)) return QSOType.get(upperKey);
		else return null;
	}
}
