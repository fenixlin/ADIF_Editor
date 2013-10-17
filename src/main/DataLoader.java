package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class DataLoader {
	
	public static ArrayList<String> QSOField = new ArrayList<String>();
	public static ArrayList<String> QSOType = new ArrayList<String>();
	public static ArrayList<String> enumName = new ArrayList<String>();//The enumeration names this program support
	public static ArrayList<ArrayList<String>> enumList = new ArrayList<ArrayList<String>>();//Detailed enumeration
	
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
			while (scanner.hasNext())
			{
				int i = scanner.nextInt();
				String name = scanner.next();
				enumName.add(name);
				ArrayList<String> list = new ArrayList<String>();
				while (i-->0)
				{
					String enumValue = scanner.next();
					list.add(enumValue);
					enumList.add(list);
				}
			}
			
			file = new File(QSOFIELD_FILE);
			if (!file.exists()) throw(new Error());
			scanner.close();

			scanner = new Scanner(file);
			while (scanner.hasNext())
			{
				String fieldName = scanner.next();
				String fileType = scanner.next();
				QSOField.add(fieldName);
				QSOType.add(fileType);
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
}
