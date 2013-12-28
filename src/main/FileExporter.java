package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class FileExporter {

	public void export(Records r, File file)
	{
		if (file.getName().endsWith(".adi")) adiExport(r, file);
		else if (file.getName().endsWith(".adx")) adxExport(r, file);
		else if (file.getName().endsWith(".xlsx")) xlsxExport(r, file);		
	}
	
	private void adiExport(Records r, File file)
	{
		BufferedWriter fout = null;
		try {
			fout = new BufferedWriter(new FileWriter(file));
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd' at 'HH:mm:ss");
			Calendar c = Calendar.getInstance();
			fout.write("Generated on "+df.format(c.getTime())+"\n\n");
			fout.write("<adif_ver:5>3.0.4\n");
			fout.write("<programid:11>ADIF_Editor\n");
			//user defined fields not done yet
			fout.write("\n<EOH>\n\n");
						
			ArrayList<HashMap<String,String>> records = r.getRecords();
			for (HashMap<String,String> record : records)
			{
				if (record.size()==0) continue;
				Iterator<Entry<String, String>> iter = record.entrySet().iterator();
				while (iter.hasNext())
				{
					Entry<String, String> data = iter.next();
					fout.write("<"+data.getKey()+":"+data.getValue().length()+">"+data.getValue()+"\n");
				}
				fout.write("<eor>\n\n");
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				fout.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private void adxExport(Records r, File file)
	{
		BufferedWriter fout = null;
		try {
			fout = new BufferedWriter(new FileWriter(file));
			
			fout.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			fout.write("<ADX>\n\n");
			//user defined fields not done yet
			String tab = "    ";
			
			fout.write(tab+"<HEADER>\n\n");
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd' at 'HH:mm:ss");
			Calendar c = Calendar.getInstance();
			fout.write(tab+tab+"<!--Generated on "+df.format(c.getTime())+"-->\n");
			fout.write(tab+tab+"<ADIF_VER>3.0.4</ADIF_VER>\n");
			fout.write(tab+tab+"<PROGRAMID>ADIF_Editor</PROGRAMID>\n");
			//user defined fields not done yet
			fout.write("\n"+tab+"</HEADER>\n");
					
			fout.write(tab+"<RECORDS>\n\n");
			ArrayList<HashMap<String,String>> records = r.getRecords();
			for (HashMap<String,String> record : records)
			{
				if (record.size()==0) continue;
				fout.write(tab+tab+"<RECORD>\n\n");
				Iterator<Entry<String, String>> iter = record.entrySet().iterator();
				while (iter.hasNext())
				{
					Entry<String, String> data = iter.next();
					String title = data.getKey();
					String elemname = null;
					String fieldname = null;
					String programid = null;
					if (title.substring(0, 3).equalsIgnoreCase("app"))
					{
						programid = title.substring(4, title.indexOf('_', 4));
						fieldname = title.substring(title.indexOf('_', 4)+1, title.length());
						elemname = "APP";
					}
					//user defined fields not done yet
					else 
					{
						elemname = title.toUpperCase();
					}
					fout.write(tab+tab+tab+"<"+elemname);
					if (programid!=null) fout.write(" PROGRAMID=\""+programid+"\"");
					if (fieldname!=null) fout.write(" FIELDNAME=\""+fieldname+"\"");
					fout.write(">"+data.getValue()+"</"+elemname+">\n");
				}
				fout.write("\n"+tab+tab+"</RECORD>\n");
			}
			fout.write("\n"+tab+"</RECORDS>\n");
			fout.write("\n</ADX>\n");
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				fout.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void xlsxExport(Records r, File file)
	{
		FileOutputStream fout = null;
		try {
			Workbook workbook = new XSSFWorkbook();
			fout = new FileOutputStream(file);
			Sheet sheet = workbook.createSheet("ADIF Data");
			LinkedHashSet<String> titleList = r.getTitles();
			ArrayList<HashMap<String,String>> records = r.getRecords();
			
			for (int i=0; i<=records.size(); i++) sheet.createRow(i);
			
			int i=0;
			Row row = sheet.getRow(0);
			for (String title: titleList)
			{
				Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cell.setCellValue(title);
				for (int j=0; j<records.size(); j++)
				{
					HashMap<String,String> hm = records.get(j);					
					Cell dataCell = sheet.getRow(j+1).getCell(i, Row.CREATE_NULL_AS_BLANK);
					dataCell.setCellType(Cell.CELL_TYPE_STRING);
					dataCell.setCellValue(hm.get(title));
				}
				i++;				
			}
			
			workbook.write(fout);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			try {
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
