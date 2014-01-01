package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

	public void export(String version, Records r, File file) throws Exception
	{
		if (file.getName().endsWith(".adi")) adiExport(version, r, file);
		else if (file.getName().endsWith(".adx")) adxExport(r, file);
		else if (file.getName().endsWith(".xlsx")) xlsxExport(r, file);
	}
	
	private void adiExport(String version, Records r, File file) throws Exception
	{
		BufferedWriter fout = new BufferedWriter(new FileWriter(file));
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd' at 'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		fout.write("Generated on "+df.format(c.getTime())+"\n\n");
		fout.write("<ADIF_VER:5>"+version+"\n");
		fout.write("<PROGRAMID:11>ADIF_Editor\n");
		HashMap<String, UDF> udfs = r.getUDFs();
		Iterator<Entry<String, UDF>> udfIter = udfs.entrySet().iterator();
		int i=0;
		while (udfIter.hasNext())
		{
			i++;
			Entry<String, UDF> entry = udfIter.next();
			UDF udf = entry.getValue();
			
			StringBuffer buffer = new StringBuffer();
			buffer.append(entry.getKey());
			if (udf.getRange()!=null)
			{
				buffer.append(",{"+udf.getRange()+"}");
			}
			else if (udf.getEnums().size()>0)
			{
				buffer.append(",{");
				ArrayList<String> enums = udf.getEnums();
				Iterator<String> enumsIter = enums.iterator();
				while (enumsIter.hasNext())
				{
					buffer.append(enumsIter.next());
					if (enumsIter.hasNext()) buffer.append(",");
				}
				buffer.append("}");
			}
			
			fout.write("<USERDEF"+i+":"+buffer.length()+":"+udf.getType()+">"+buffer);
			fout.write("\n");
		}
		//user defined fields not done yet
		fout.write("\n<EOH>\n\n");
					
		ArrayList<HashMap<String,String>> records = r.getRecords();
		LinkedHashSet<String> titles = r.getTitles();
		HashMap<String, String> apps = r.getAPPs();
		for (HashMap<String,String> record : records)
		{
			if (record.size()==0) continue;
			for (String title: titles)
			{
				String dataSpec = title;
				String value = record.get(title);
				if (value==null) continue;
				value=value.trim();
				if (apps.containsKey(dataSpec)) dataSpec = "APP_"+apps.get(dataSpec)+"_"+dataSpec;
				fout.write("<"+dataSpec+":"+value.length()+">"+value+"\n");
			}
			fout.write("<EOR>\n\n");
		}
		
		fout.close();
	}	
	
	private void adxExport(Records r, File file) throws Exception
	{
		BufferedWriter fout = new BufferedWriter(new FileWriter(file));
			
		fout.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		fout.write("<ADX>\n\n");
		String tab = "    ";
		
		fout.write(tab+"<HEADER>\n\n");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd' at 'HH:mm:ss");
		Calendar c = Calendar.getInstance();
		fout.write(tab+tab+"<!--Generated on "+df.format(c.getTime())+"-->\n");
		fout.write(tab+tab+"<ADIF_VER>3.0.4</ADIF_VER>\n");
		fout.write(tab+tab+"<PROGRAMID>ADIF_Editor</PROGRAMID>\n");
		//user defined fields not done yet
		HashMap<String, UDF> udfs = r.getUDFs();
		Iterator<Entry<String, UDF>> udfIter = udfs.entrySet().iterator();
		int i=0;
		while (udfIter.hasNext())
		{
			i++;
			Entry<String, UDF> entry = udfIter.next();
			UDF udf = entry.getValue();
			fout.write(tab+tab+"<USERDEF FIELDID=\""+i+"\" TYPE=\""+udf.getType()+"\"");
			if (udf.getRange()!=null)
			{
				fout.write(" RANGE=\"{"+udf.getRange()+"}\"");
			}
			else if (udf.getEnums().size()>0)
			{
				fout.write(" ENUM=\"{");
				ArrayList<String> enums = udf.getEnums();
				Iterator<String> enumsIter = enums.iterator();
				while (enumsIter.hasNext())
				{
					fout.write(enumsIter.next());
					if (enumsIter.hasNext()) fout.write(",");
				}
				fout.write("}\"");
			}
			fout.write(">"+entry.getKey().toUpperCase()+"</USERDEF>\n");				
		}
		fout.write("\n"+tab+"</HEADER>\n");
				
		fout.write(tab+"<RECORDS>\n\n");
		ArrayList<HashMap<String,String>> records = r.getRecords();
		LinkedHashSet<String> titles = r.getTitles();
		HashMap<String,String> types = r.getTypes();
		HashMap<String,String> apps = r.getAPPs();
		for (HashMap<String,String> record : records)
		{
			if (record.size()==0) continue;
			fout.write(tab+tab+"<RECORD>\n\n");			
			for (String title : titles)
			{
				String value = record.get(title);
				if (value==null) continue;
				value=value.trim();
				String elemname = null;
				String fieldname = null;
				String programid = null;
				String type = null;
				if (apps.containsKey(title))
				{
					programid = apps.get(title);
					fieldname = title;
					type = types.get(title);
					elemname = "APP";
				}
				else if (udfs.containsKey(title))
				{						
					fieldname = title;
					elemname = "USERDEF";
				}
				else 
				{
					elemname = title.toUpperCase();
				}
				fout.write(tab+tab+tab+"<"+elemname);
				if (programid!=null) fout.write(" PROGRAMID=\""+programid+"\"");
				if (fieldname!=null) fout.write(" FIELDNAME=\""+fieldname+"\"");
				if (type!=null) fout.write(" TYPE=\""+type+"\"");
				fout.write(">"+value+"</"+elemname+">\n");
			}
			fout.write("\n"+tab+tab+"</RECORD>\n");
		}
		fout.write("\n"+tab+"</RECORDS>\n");
		fout.write("\n</ADX>\n");		
		
		fout.close();
	}
	
	private void xlsxExport(Records r, File file) throws Exception
	{
		Workbook workbook = new XSSFWorkbook();
		FileOutputStream  fout = new FileOutputStream(file);
		Sheet sheet = workbook.createSheet("ADIF Data");
		LinkedHashSet<String> titles = r.getTitles();
		ArrayList<HashMap<String,String>> records = r.getRecords();
		
		for (int i=0; i<=records.size(); i++) sheet.createRow(i);
		
		int i=0;
		Row row = sheet.getRow(0);
		for (String title: titles)
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
		
		fout.close();
	}
}
