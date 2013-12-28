package main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.aspose.cells.*;

class Records
{
	//记录从文件读入的record信息
	private LinkedHashSet<String> titles;
	private HashMap<String,String> types;//values are all upper-case, default type is S
	private ArrayList<HashMap<String,String>> records;
	
	public Records() {}
	
	public Records(LinkedHashSet<String> x, HashMap<String,String> y, ArrayList<HashMap<String,String>> z)
	{
		titles = x;
		types = y;
		records = z;
	}
	
	public LinkedHashSet<String> getTitles() {return titles;}
	public HashMap<String,String> getTypes() {return types;}
	public ArrayList<HashMap<String,String>> getRecords() {return records;}
					
	public void setTitles(LinkedHashSet<String> x) {titles=x;}
	public void setTypes(HashMap<String,String> x) {types=x;}
	public void setRecords(ArrayList<HashMap<String,String>> x) {records=x;}		
}

public class FileAnalyzer 
{
	public Records analyze(File file)
	{
		//根据调用格式用不同函数分析
		if (file.getName().endsWith(".adi")) return adiAnalyze(file);
		else if (file.getName().endsWith(".adx")) return adxAnalyze(file);
		else if (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xls")) return xlsxAnalyze(file);
		else return null;
	}
	
	//是不是只有adi才要用config呢？
	
	private Records adiAnalyze(File file)
	{
		//分析adi文件成record
		Scanner scanner = null;
		Records r = new Records();
		try
		{
			scanner = new Scanner(file);
			/*boolean isContent;*/
			if (!scanner.next().startsWith("<"))
			{	
				//throw the header out
				String s;
				do
				{
					scanner.useDelimiter("[<\\r\\n]+"); //In case there are nothing between tags
					s = scanner.next();
					scanner.useDelimiter("[<>\\r\\n]+");
					s = scanner.next();
				}while (!s.equalsIgnoreCase("eoh"));
			}
			else
			{
				scanner.reset();
			}
			
			scanner.useDelimiter("[<>\\r\\n]+");
			LinkedHashSet<String> titleList = new LinkedHashSet<String>();
			HashMap<String,String> types = new HashMap<String,String>(); 
			ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
			//use LinkedHashSet to store titles.
			//use ArrayList to temporarily store data and make it a row when eor is read
			HashMap<String,String> record = new HashMap<String,String>();
			
			int length = -1;
			String type = null;
			
			while (scanner.hasNext())
			{				
				String rawTitle = scanner.next();
				Scanner titleScanner = new Scanner(rawTitle);
				titleScanner.useDelimiter(":");
				String title = titleScanner.next();
				
				length = -1;
				if (titleScanner.hasNextInt()) length = titleScanner.nextInt();
				type = null;
				if (titleScanner.hasNext()) type = titleScanner.next();
				titleScanner.close();
				
				if (title.equalsIgnoreCase("eor"))
				{
					records.add(record);
					record = new HashMap<String,String>();
				}
				else
				{
					if (!titleList.contains(title))
					{
						titleList.add(title);
						if (!types.containsKey(title))
						{
							if (type == null)
							{
								ConfigLoader configLoader = new ConfigLoader();
								type = configLoader.getQSOType(title);
								if (type == null) types.put(title, "S");
								else types.put(title, type.toUpperCase());
							}
							else
							{
								types.put(title, type.toUpperCase());
							}
						}
					}
					//In case there are nothing between tags, so reset delimiter (the '>' of first tag will be read)
					scanner.useDelimiter("[<\\r\\n]+");
					String value = scanner.next();
					if (value.startsWith(">")) value = value.substring(1);					
					
					/*
					DataChecker checker = new DataChecker();
					if (!checker.typeCheck(value, type) || !checker.lengthCheck(value, length)) throw(new Exception());
					*/
					
					record.put(title, value);
				}
				scanner.useDelimiter("[<>\\r\\n]+");
			}
			r.setTitles(titleList);
			r.setTypes(types);
			r.setRecords(records);
		}
		catch (Exception e)
		{
			//Is is possible to cause file not found?
		}
		finally
		{
			scanner.close();
		}
		return r;
	}
	
	private Records adxAnalyze(File file)
	{
		//分析adx文件成Record (用SAX读入xml) 
		Records r = new Records();
		try
		{
			//They have protected constructors. This is the only way to employ SAXParser
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			MySaxParser mySaxParser = new MySaxParser();
			
			saxParser.parse(file, mySaxParser);
			r.setTitles(mySaxParser.getTitleList());
			r.setRecords(mySaxParser.getRecordList());
		}
		catch (Exception e)
		{
			//print XML errors
			e.printStackTrace();
		}
		return r;
	}
	
	private class MySaxParser extends DefaultHandler
	{
		//分析xml文件的xml读入器，主要定义了怎么存数据
		private LinkedHashSet<String> titleList = new LinkedHashSet<String>();
		private ArrayList<HashMap<String, String>> recordList = new ArrayList<HashMap<String,String>>();
		private HashMap<String, String> hashMap;
		private String currentTitle;
		private boolean isHeader = false;
		private boolean isRecords = false;
		private boolean isRecord = false;
		
		public MySaxParser() {super();}
		
		public LinkedHashSet<String> getTitleList() {return titleList;}
		public ArrayList<HashMap<String,String>> getRecordList() {return recordList;}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (isHeader)
			{
				//do something with header
			}
			if (isRecords)
			{
				if (isRecord)
				{
					String title;
					//XML is case-sensitive
					if (!qName.equals("USERDEF") && !qName.equals("APP")) title = qName;
					else title = attributes.getValue("FIELDNAME");//USERDEF and APP must extract their own name.
					if (!titleList.contains(title))
					{						
						titleList.add(title);
					}
					currentTitle = title;
				}
				if (qName.equals("RECORD"))
				{
					isRecord = true;
					hashMap = new HashMap<String, String>();
				}
			}
			if (qName.equals("HEADER")) isHeader = true;
			if (qName.equals("RECORDS")) isRecords = true;
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if (qName.equals("HEADER")) isHeader = false;
			if (qName.equals("RECORDS")) isRecords = false;
			if (qName.equals("RECORD"))
			{
				isRecord = false;
				recordList.add(hashMap);
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			String s = new String(ch, start, length).trim();
			if (s.length() == 0)
			{
				return; // ignore white space
			}
			if (isRecord && currentTitle!=null)hashMap.put(currentTitle, s);
		}
		
		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
		{
			if (start==1);
		}
	}
	
	private Records xlsxAnalyze(File file)
	{
		FileInputStream fstream = null;
		Records r = new Records();
		try {
			fstream = new FileInputStream(file);
			Workbook workbook = new Workbook(fstream);
			Worksheet worksheet = workbook.getWorksheets().get(0);
			Cells cells = worksheet.getCells();
			int maxCol = 0;
			while (true)
			{
				Cell cell = cells.get(0, maxCol);
				String value = cell.getDisplayStringValue().trim();
				if (value==null || value.isEmpty()) break;				
				maxCol++;		
			}
			int maxRow = 0;
			while (true)
			{
				Cell cell = cells.get(maxRow, 0);
				String value = cell.getDisplayStringValue().trim();
				if (value==null || value.isEmpty()) break;				
				maxRow++;		
			}
			
			LinkedHashSet<String> titleList = new LinkedHashSet<String>();
			HashMap<String,String> types = new HashMap<String,String>(); 
			ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
			
			for (int i=0;i<maxCol;i++)
			{
				Cell cell = cells.get(0, i);
				String title = cell.getDisplayStringValue();
				if (!titleList.contains(title))
				{
					titleList.add(title);
					ConfigLoader configLoader = new ConfigLoader();
					String type = configLoader.getQSOType(title);
					if (type == null) types.put(title, "S");
					else types.put(title, type.toUpperCase());
				}
			}
			
			for (int i=1;i<maxRow;i++)
			{
				HashMap<String, String> record = new HashMap<String, String>();
				for (int j=0;j<maxCol;j++)
				{
					Cell cell = cells.get(i, j);
					Cell titleCell = cells.get(0, j);
					record.put(titleCell.getDisplayStringValue(), cell.getDisplayStringValue());					
				}
				records.add(record);
			}
			
			r.setTitles(titleList);
			r.setTypes(types);
			r.setRecords(records);
		} 
		catch (Exception e) {}
		finally
		{
			try {
				fstream.close();
			} 
			catch (Exception e) {}
		}
		
		return r;
	}
}
