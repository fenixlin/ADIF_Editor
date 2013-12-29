package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.ss.usermodel.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FileAnalyzer 
{
	public Records analyze(File file)
	{
		//根据调用格式用不同函数分析
		if (file.getName().endsWith(".adi")) return adiAnalyze(file);
		else if (file.getName().endsWith(".adx")) return adxAnalyze(file);
		else if (file.getName().endsWith(".xlsx")) return xlsxAnalyze(file);
		else return null;
	}
	
	//是不是只有adi才要用config呢？
	
	private Records adiAnalyze(File file)
	{
		//分析adi文件成record
		
		Scanner scanner = null;
		Records r = null;
		try
		{
			scanner = new Scanner(file);
			/*boolean isContent;*/
			
			LinkedHashSet<String> titleList = new LinkedHashSet<String>();
			HashMap<String,String> types = new HashMap<String,String>();
			ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
			HashMap<String, UDF> udfs = new HashMap<String, UDF>();
			HashMap<String, String> apps = new HashMap<String, String>();
			//use LinkedHashSet to store titles.
			//use ArrayList to temporarily store data and make it a row when eor is read
			
			if (!scanner.next().startsWith("<"))
			{
				//throw the header out
				String spec;
				String body;
				scanner.useDelimiter("<");
				scanner.next();
				do
				{
					scanner.useDelimiter("[<>\\r\\n]+"); //In case there are no content between tags
					spec = scanner.next();					
					scanner.useDelimiter("[<\\r\\n]+");
					body = scanner.next();
					if (body.startsWith(">")) body=body.substring(1);
					if (spec.startsWith("USERDEF"))
					{
						Scanner titleScanner = new Scanner(spec);
						titleScanner.useDelimiter(":");
						String head = titleScanner.next();
						int fieldID = Integer.parseInt(head.substring(7, head.length()));
						//length is not used here
						titleScanner.nextInt();						
						//Data Types and Data Type Indicators are case insensitive.
						String type = titleScanner.next().toUpperCase();
						titleScanner.close();
						
						Scanner bodyScanner = new Scanner(body);
						bodyScanner.useDelimiter("[{},]+");
						//ADIF Field Names are case-insensitive.
						String fieldName = bodyScanner.next().toUpperCase();						
						ArrayList<String> enums = new ArrayList<String>();
						String range = null;
						if (bodyScanner.hasNext())
						{
							String s = bodyScanner.next();
							if (s.matches("\\d+:\\d+"))
							{
								range = s;								
							}
							else
							{
								do
								{
									enums.add(s);
									if (bodyScanner.hasNext()) s = bodyScanner.next();
									else break;									
								}while (true);								
							}
						}
						bodyScanner.close();
						
						udfs.put(fieldName, new UDF(fieldID, type, enums, range));						
					}
				}while (!spec.equalsIgnoreCase("eoh"));
			}
			else
			{
				scanner.reset();
			}
			
			scanner.useDelimiter("[<>\\r\\n]+");
			
			HashMap<String,String> record = new HashMap<String,String>();
			
			int length = -1;
			String type = null;

			while (scanner.hasNext())
			{				
				String rawTitle = scanner.next();
				Scanner titleScanner = new Scanner(rawTitle);
				titleScanner.useDelimiter(":");
				
				//ADIF Field Names are case-insensitive.
				String title = titleScanner.next().toUpperCase();
				
				length = -1;
				if (titleScanner.hasNextInt()) length = titleScanner.nextInt();
				type = null;
				//Data Types and Data Type Indicators are case insensitive.
				if (titleScanner.hasNext()) type = titleScanner.next().toUpperCase();
				titleScanner.close();
				
				if (title.equals("EOR"))
				{
					records.add(record);
					record = new HashMap<String,String>();
				}
				else
				{
					if (title.startsWith("APP_"))
					{
						String programID = title.substring(4, title.indexOf('_', 4));
						title = title.substring(title.indexOf('_', 4)+1, title.length());
						apps.put(title, programID);
					}
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
								else types.put(title, type);
							}
							else
							{
								types.put(title, type);
							}
						}
					}
					//In case there are nothing between tags, so reset delimiter (the '>' of first tag will be read)
					scanner.useDelimiter("[<\\r\\n]+");
					String value = scanner.next();
					if (value.startsWith(">")) value = value.substring(1);
										
					// CUT but not CHECK??????????????????????????????
					if (length>0) value=value.substring(0, length);
					
					/*
					DataChecker checker = new DataChecker();
					if (!checker.typeCheck(value, type) || !checker.lengthCheck(value, length)) throw(new Exception());
					*/
					
					record.put(title, value);
				}
				scanner.useDelimiter("[<>\\r\\n]+");
			}			
			r = new Records(titleList, types, records, udfs, apps);
		}
		catch (Exception e)
		{
			//Is is possible to cause file not found?
			e.printStackTrace();
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
		Records r = null;
		try
		{
			//They have protected constructors. This is the only way to employ SAXParser
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser saxParser = saxParserFactory.newSAXParser();
			MySaxParser mySaxParser = new MySaxParser();
			
			saxParser.parse(file, mySaxParser);
			r = new Records(mySaxParser.getTitles(), mySaxParser.getTypes(), mySaxParser.getRecords(), mySaxParser.getUDFs(), mySaxParser.getAPPs());
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
		private LinkedHashSet<String> titles = new LinkedHashSet<String>();
		private HashMap<String,String> types = new HashMap<String,String>();
		private ArrayList<HashMap<String, String>> records = new ArrayList<HashMap<String,String>>();
		private HashMap<String, UDF> udfs = new HashMap<String, UDF>();
		private HashMap<String, String> apps = new HashMap<String, String>();
		
		private HashMap<String, String> record;
		private String currentTitle;
		private UDF currentUDF;
		private boolean isUDF = false;
		private boolean isHeader = false;
		private boolean isRecords = false;
		private boolean isRecord = false;
		
		public MySaxParser() {super();}
		
		public LinkedHashSet<String> getTitles() {return titles;}
		public HashMap<String,String> getTypes() {return types;}	
		public ArrayList<HashMap<String,String>> getRecords() {return records;}
		public HashMap<String, UDF> getUDFs() {return udfs;}
		public HashMap<String, String> getAPPs() {return apps;}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (isHeader)
			{
				if (qName.equals("USERDEF"))
				{
					isUDF = true;
					int fieldID = Integer.parseInt(attributes.getValue("FIELDID"));
					String type = attributes.getValue("TYPE");
					String enumlist = attributes.getValue("ENUM");
					ArrayList<String> enums = new ArrayList<String>();
					if (enumlist!=null)
					{
						Scanner enumScanner = new Scanner(enumlist);
						enumScanner.useDelimiter("[{},]+");
						while (enumScanner.hasNext())
						{
							enums.add(enumScanner.next());
						}
						enumScanner.close();
					}
					String range = attributes.getValue("RANGE");
					if (range!=null) range = range.substring(1, range.length()-1);					
					currentUDF = new UDF(fieldID, type, enums, range);					
				}
			}
			else if (isRecords)
			{
				if (isRecord)
				{
					String title;
					//XML is case-sensitive
					if (qName.equals("USERDEF") || qName.equals("APP"))
					{
						//ADIF Field Names are case-insensitive. USERDEF and APP must extract their own name.
						title = attributes.getValue("FIELDNAME").toUpperCase();
						if (qName.equals("APP"))
						{
							apps.put(title, attributes.getValue("PROGRAMID"));
						}
					}					
					else title = qName;
					
					if (!titles.contains(title))
					{
						titles.add(title);
						String type = attributes.getValue("TYPE");
						if (type!=null)	types.put(title, type);						
						else types.put(title, "S");						
					}
					currentTitle = title;
				}
				if (qName.equals("RECORD"))
				{
					isRecord = true;
					record = new HashMap<String, String>();
				}
			}
			else if (qName.equals("HEADER")) isHeader = true;
			else if (qName.equals("RECORDS")) isRecords = true;
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			if (qName.equals("HEADER")) isHeader = false;
			else if (qName.equals("RECORDS")) isRecords = false;
			else if (qName.equals("RECORD"))
			{
				isRecord = false;
				records.add(record);
			}
			else if (qName.equals("USERDEF")) isUDF = false;
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			String s = new String(ch, start, length).trim();
			if (s.length() == 0)
			{
				return; // ignore white space
			}
			if (isRecord && currentTitle!=null) record.put(currentTitle, s);
			else if (isUDF)
			{
				udfs.put(s, currentUDF);
				if (currentUDF.getType()!=null)
				{
					types.put(s, currentUDF.getType());
				}
			}
		}		
	}
	
	private Records xlsxAnalyze(File file)
	{
		Records r = null;
		Workbook workbook;
		try {
			workbook = WorkbookFactory.create(file);
			Sheet sheet = workbook.getSheetAt(0);

			LinkedHashSet<String> titleList = new LinkedHashSet<String>();
			HashMap<String,String> types = new HashMap<String,String>(); 
			ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();

			Row titleRow = sheet.getRow(0);
			int lastColumn = titleRow.getLastCellNum();
			
			for (Cell cell : titleRow)
			{
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String title = cell.getStringCellValue();
				if (!titleList.contains(title))
				{
					titleList.add(title);
					ConfigLoader configLoader = new ConfigLoader();
					//Data Types and Data Type Indicators are case insensitive.
					String type = configLoader.getQSOType(title).toUpperCase();
					if (type == null) types.put(title, "S");
					else types.put(title, type);
				}
			}
			for (int i=1; i<=sheet.getLastRowNum(); i++)
			{
				Row row = sheet.getRow(i);
				HashMap<String, String> record = new HashMap<String, String>();
				for (int j=0; j<lastColumn; j++)
				{
					Cell cell = row.getCell(j, Row.RETURN_BLANK_AS_NULL);
					
					if (cell!=null)
					{
						cell.setCellType(Cell.CELL_TYPE_STRING);
						String title = titleRow.getCell(j).getStringCellValue();
						record.put(title, cell.getStringCellValue());
					}
				}
				records.add(record);
			}
			
			r = new Records(titleList, types, records, new HashMap<String, UDF>(), new HashMap<String, String>());			
		}
		catch (Exception e)
		{
			//还木有处理！
			e.printStackTrace();
		}
		
		return r;
	}
}
