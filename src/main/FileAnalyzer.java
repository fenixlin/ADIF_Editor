package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class Records
{
	//��¼���ļ������record��Ϣ
	private LinkedHashSet<String> titles = new LinkedHashSet<String>();
	private ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
	
	public LinkedHashSet<String> getTitles() {return titles;}
	public ArrayList<HashMap<String,String>> getRecords() {return records;}
					
	public void setTitles(LinkedHashSet<String> x) {titles=x;}		
	public void setRecords(ArrayList<HashMap<String,String>> x) {records=x;}		
}

public class FileAnalyzer 
{
	
	public Records analyze(File file)
	{
		//���ݵ��ø�ʽ�ò�ͬ��������
		if (file.getName().endsWith(".adi")) return adiAnalyze(file);
		else return adxAnalyze(file);
	}
	
	private Records adiAnalyze(File file)
	{
		//����adi�ļ���record
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
			ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
			//use LinkedHashSet to store titles.
			//use ArrayList to temporarily store data and make it a row when eor is read
			HashMap<String,String> record = new HashMap<String,String>();
			while (scanner.hasNext())
			{				
				String rawTitle = scanner.next();
				Scanner titleScanner = new Scanner(rawTitle);
				titleScanner.useDelimiter(":");
				String title = titleScanner.next();
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
					}
					scanner.useDelimiter("[<\\r\\n]+");
					String value = scanner.next();
					if (value.startsWith(">")) value = value.substring(1);//In case there are nothing between tags
					record.put(title, value);
				}
				scanner.useDelimiter("[<>\\r\\n]+");
			}
			r.setTitles(titleList);
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
		//����adx�ļ���Record (��SAX����xml) 
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
	
	public class MySaxParser extends DefaultHandler
	{
		//����xml�ļ���xml����������Ҫ��������ô������
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
}
