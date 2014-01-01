package main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class MainTableModel extends DefaultTableModel{

	private static final long serialVersionUID = 1L;
	
	//NOTICE: title place is not up-to-date when user has dragged columns.
	private LinkedHashSet<String> titles = new LinkedHashSet<String>();
	private HashMap<String, String> types = new HashMap<String,String>();
	private ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
	private HashMap<String, UDF> udfs = new HashMap<String, UDF>();
	private HashMap<String, String> apps = new HashMap<String, String>();
	private ArrayList<Boolean> isCheckBox = new ArrayList<Boolean>();
	
	boolean hasDate = false;
	boolean hasTime = false;
	
	public void importData(Records r)
	{
		if (r == null) return;
		
		LinkedHashSet<String> newTitles = r.getTitles();
		HashMap<String, String> newTypes = r.getTypes();
		ArrayList<HashMap<String,String>> newRecords = r.getRecords();	
		HashMap<String, UDF> newUDFs = r.getUDFs();
		HashMap<String, String> newAPPs = r.getAPPs();

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		Vector<String> head = new Vector<String>();
		
		for (String s : newTitles)
		{
			titles.add(s);
		}
		Iterator<Entry<String, String>> iter = newTypes.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<String, String> entry = iter.next();
			types.put(entry.getKey(), entry.getValue());
		}
		iter = newAPPs.entrySet().iterator();
		while (iter.hasNext())
		{
			Entry<String, String> entry = iter.next();
			apps.put(entry.getKey(), entry.getValue());
		}
		Iterator<Entry<String, UDF>> iter2 = newUDFs.entrySet().iterator();
		while (iter2.hasNext())
		{
			Entry<String, UDF> entry = iter2.next();
			udfs.put(entry.getKey(), entry.getValue());
		}
		for (HashMap<String,String> record : newRecords)
		{
			records.add(record);
		}
		for (String s : titles)
		{
			head.add(s);
			if (types.get(s).equals("B")) isCheckBox.add(new Boolean(true));
			else isCheckBox.add(new Boolean(false));
		}
		
		hasDate = false;		
		hasTime = false;		
		if (titles.contains("QSO_DATE")) hasDate = true;
		if (titles.contains("TIME_ON")) hasTime = true;
		
		if (hasDate || hasTime)
			Collections.sort(records, new Comparator<HashMap<String,String>>(){

				@Override
				public int compare(HashMap<String, String> o1,
						HashMap<String, String> o2) {
					
					int result = 0;
					if (hasDate)
					{
						String s1 = o1.get("QSO_DATE");
						String s2 = o2.get("QSO_DATE");
						if (s1==null) s1="";
						if (s2==null) s2="";
						result = s1.compareTo(s2);						
					}
					if (!hasDate || (result==0 && hasTime))
					{
						String s1 = o1.get("TIME_ON");
						String s2 = o2.get("TIME_ON");
						if (s1==null) s1="";
						if (s2==null) s2="";
						result = s1.compareTo(s2);
					}
					
					return -result;
				}
			});		
		
		for (int i=0; i<records.size(); i++)
		{
			HashMap<String,String> record = records.get(i);
			Vector<Object> row = new Vector<Object>();
			int j = 0;
			for(String s: titles)
			{	
				String value = record.get(s);
				if (isCheckBox.get(j))
				{
					if (value!=null && value.equals("Y")) row.add(new Boolean(true));
					else row.add(new Boolean(false));
				}
				else
				{
					row.add(value);
				}
				j++;
			}
			data.add(row);
		}
		
		super.setDataVector(data, head);
	}
	
	public Records exportData(LinkedHashSet<String> printTitles)
	{
		return new Records(printTitles, types, records, udfs, apps);
	}
	
	public void editData(int row, int col, String newData)
	{
		String colName = this.getColumnName(col);
		HashMap<String, String> record = records.get(row);
		if (isCheckBox.get(col))
		{
			if (newData.equals("true")) record.put(colName, "Y");
			else record.put(colName, "N");
		}
		else record.put(colName, newData);
	}
	
	public String getRowValue(int row, String title)
	{
		HashMap<String, String> record = records.get(row);
		return record.get(title);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
	    if (isCheckBox.get(columnIndex))
	        return Boolean.class;
	    return super.getColumnClass(columnIndex);
	}
	
	@Override
	public void addColumn(Object columnName)
	{
		super.addColumn(columnName);
		String title = columnName.toString(); 
		titles.add(title);
		
		ConfigLoader configLoader = new ConfigLoader();
		String type = configLoader.getQSOType(title);
		if (type == null) types.put(title, "S");
		else types.put(title, type);
				
		if (type!=null && type.equals("B")) isCheckBox.add(new Boolean(true));
		else isCheckBox.add(new Boolean(false));
	}
	
	public void addRow()
	{
		records.add(new HashMap<String, String>());
		
		Vector<Object> v = new Vector<Object>();
		for(int i=0; i<titles.size(); i++)
		{	
			if (isCheckBox.get(i)) v.add(new Boolean(false));
			else v.add(new String(""));
		}
		super.addRow(v);
	}
	
	public void removeColumn(String s)
	{
		titles.remove(s);
	}
}
