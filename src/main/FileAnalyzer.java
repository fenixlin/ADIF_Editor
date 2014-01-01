package main;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.ss.usermodel.*;
import org.mozilla.universalchardet.UniversalDetector;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FileAnalyzer 
{
	boolean dataOK;
	
	public Records analyze(File file) throws Exception
	{
		//根据调用格式用不同函数分析
		if (file.getName().endsWith(".adi")) return adiAnalyze(file);
		else if (file.getName().endsWith(".adx")) return adxAnalyze(file);
		else if (file.getName().endsWith(".xlsx")) return xlsxAnalyze(file);
		else return null;
	}
	
	private Records adiAnalyze(File file) throws Exception
	{
		//分析adi文件成record
		
		Scanner scanner = null;
		Records r = null;
		DataChecker checker = new DataChecker();
		dataOK = true;

		String charset = detectCharset(file);
		scanner = new Scanner(file, charset);
		/*boolean isContent;*/
		LinkedHashSet<String> titleList = new LinkedHashSet<String>();
		HashMap<String,String> types = new HashMap<String,String>();
		ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
		HashMap<String, UDF> udfs = new HashMap<String, UDF>();
		HashMap<String, String> apps = new HashMap<String, String>();
		//use LinkedHashSet to store titles.
		//use ArrayList to temporarily store data and make it a row when eor is read
		//If the file is not empty
		if (scanner.hasNext())
		{
			if (!scanner.hasNext("<.+"))
			{
				//Header part
				String spec;
				String body;
				scanner.useDelimiter("<");
				scanner.next();
				do
				{
					scanner.useDelimiter(">"); //In case there are no content between tags
					spec = scanner.next();
					spec = spec.substring(spec.lastIndexOf('<')+1);
					int length = -1;
					
					Scanner titleScanner = new Scanner(spec);
					titleScanner.useDelimiter(":");
					titleScanner.next();
					//+1 for '>'
					if (titleScanner.hasNextInt()) 
					{
						length = titleScanner.nextInt()+1;
						titleScanner.close();
					}
					else if (spec.equalsIgnoreCase("eoh"))
					{
						titleScanner.close();
						break;
					}
					else
					{
						titleScanner.close();
						throw new Exception();
					}
					
					StringBuffer buf = new StringBuffer();
					scanner.useDelimiter("<");
					buf.append(scanner.next());
					while (buf.toString().length() < length)
					{
						//读fixed Length
						buf.append(scanner.findInLine("[<]+"));
						if (buf.toString().length() >= length) break;
						buf.append(scanner.next());
					}
					body = buf.toString();
					if (body.length()>=length) body=body.substring(1,length);
					else body = body.substring(1);
					
					if (spec.startsWith("USERDEF"))
					{
						Scanner udfTitleScanner = new Scanner(spec);
						udfTitleScanner.useDelimiter(":");
						String head = udfTitleScanner.next();
						int fieldID = Integer.parseInt(head.substring(7, head.length()));
						//length is not used here
						udfTitleScanner.nextInt();	
						//Data Types and Data Type Indicators are case insensitive.
						String type = udfTitleScanner.next().toUpperCase();
						udfTitleScanner.close();
						
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
			
			scanner.useDelimiter("[>\\r\\n]+");
			while (scanner.hasNext() && (!scanner.hasNext(".*<.+")))
			{
				scanner.next();
			}
			scanner.useDelimiter(">");
			
			HashMap<String,String> record = new HashMap<String,String>();

			int length = -1;
			String type = null;
			
			while (scanner.hasNext())
			{
				String spec = scanner.next();
				//when file ends (what if problem occur?)
				if (spec.lastIndexOf('<')<0) break;
				spec = spec.substring(spec.lastIndexOf('<')+1);
				
				Scanner titleScanner = new Scanner(spec);
				titleScanner.useDelimiter(":");					
				//ADIF Field Names are case-insensitive.
				String title = titleScanner.next().toUpperCase();					
				if (titleScanner.hasNextInt())
				{
					length = titleScanner.nextInt()+1;
					type = null;
					//Data Types and Data Type Indicators are case insensitive.
					if (titleScanner.hasNext()) type = titleScanner.next().toUpperCase();
					titleScanner.close();
					
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
					
					StringBuffer buf = new StringBuffer();
					scanner.useDelimiter("<");
					buf.append(scanner.next());
					while (buf.toString().length() < length)
					{
						//读fixed Length
						buf.append(scanner.findInLine("[<]+"));
						if (buf.toString().length() >= length) break;
						buf.append(scanner.next());
					}
					String body = buf.toString();
					if (body.length()>=length) body=body.substring(1,length);
					else body = body.substring(1);
										
					if (checker.dataCheck(body, type))
						record.put(title, body);
					else
					{
						System.out.println("wrong:"+body+" --- "+type);
						dataOK = false;
					}
				}
				else if (title.equals("EOR"))
				{
					records.add(record);
					record = new HashMap<String,String>();
				}
				else
				{
					titleScanner.close();
					throw new Exception();
				}
									
				scanner.useDelimiter(">");
			}
					
			r = new Records(titleList, types, records, udfs, apps);
		}

		scanner.close();
		
		if (!dataOK) JOptionPane.showMessageDialog(null, "Several data in wrong format have not been imported.", "Error", JOptionPane.ERROR_MESSAGE);

		return r;
	}
	
	private Records adxAnalyze(File file) throws Exception
	{
		//分析adx文件成Record (用SAX读入xml) 
		Records r = null;
		dataOK = true;

		//They have protected constructors. This is the only way to employ SAXParser
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		SAXParser saxParser = saxParserFactory.newSAXParser();
		MySaxParser mySaxParser = new MySaxParser();
		
		saxParser.parse(file, mySaxParser);
		r = new Records(mySaxParser.getTitles(), mySaxParser.getTypes(), mySaxParser.getRecords(), mySaxParser.getUDFs(), mySaxParser.getAPPs());

		if (!dataOK) JOptionPane.showMessageDialog(null, "Several data in wrong format have not been imported.", "Error", JOptionPane.ERROR_MESSAGE);
		
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
		
		DataChecker checker = new DataChecker();
		
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
			if (isRecord && currentTitle!=null)
			{				
				if (checker.dataCheck(s, types.get(currentTitle)))
					record.put(currentTitle, s);
				else
					dataOK = false;
			}
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
	
	private Records xlsxAnalyze(File file) throws Exception
	{
		dataOK = true;
		DataChecker checker = new DataChecker();
		Records r = null;
		Workbook workbook;

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
					String value = cell.getStringCellValue();
					if (checker.dataCheck(value, types.get(title)))
						record.put(title, value);
					else
						dataOK = false;
				}
			}
			records.add(record);
		}
		
		r = new Records(titleList, types, records, new HashMap<String, UDF>(), new HashMap<String, String>());			
		
		if (!dataOK) JOptionPane.showMessageDialog(null, "Several data in wrong format have not been imported.", "Error", JOptionPane.ERROR_MESSAGE);
		
		return r;
	}

	private String detectCharset(File file) throws Exception
	{
		byte[] buf = new byte[4096];

		FileInputStream fis = new FileInputStream(file);
		UniversalDetector detector = new UniversalDetector(null);
		int nread;
	    while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
	      detector.handleData(buf, 0, nread);
	    }
	    detector.dataEnd();
	    String encoding = detector.getDetectedCharset();
	    //If no charset is detected
	    if (encoding==null) encoding = "UTF-8";
	    fis.close();
	    return encoding;		    
	}
}
