package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import com.aspose.cells.*;

public class FileExporter {

	public void export(Records r, File file)
	{
		if (file.getName().endsWith(".adi")) adiExport(r, file);
		else if (file.getName().endsWith(".adx")) adxExport(r, file);
		else if (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xls")) xlsxExport(r, file);		
	}
	
	private void adiExport(Records r, File file)
	{
		
	}
	
	private void adxExport(Records r, File file)
	{
		
	}
	
	private void xlsxExport(Records r, File file)
	{		
		try {
			Workbook workbook = new Workbook();
			Cells cells = workbook.getWorksheets().get(0).getCells();
			LinkedHashSet<String> titleList = new LinkedHashSet<String>();
			ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
			
			int i=0;
			for (String title: titleList)
			{
				cells.get(0, i).setValue(title);
				int j=1;
				for (HashMap<String, String> hm : records)
				{
					cells.get(j, i).setValue(hm.get(title));
					j++;
				}
				i++;
			}
			
			workbook.save(file.getAbsolutePath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
