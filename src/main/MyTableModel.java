package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel{

	private static final long serialVersionUID = 1L;
	
	private LinkedHashSet<String> titles = new LinkedHashSet<String>();
	private HashMap<String, String> types = new HashMap<String,String>();
	private ArrayList<HashMap<String,String>> records = new ArrayList<HashMap<String,String>>();
	private HashMap<String, UDF> udfs = new HashMap<String, UDF>();
	private HashMap<String, String> apps = new HashMap<String, String>();
	private ArrayList<Boolean> isCheckBox = new ArrayList<Boolean>();		
	
	public void importData(Records r)
	{		
		// TODO 一定要记得加上下面这行
		// if (r == null) return;
		
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
		isCheckBox.add(new Boolean(false));
	}
	
	public void addRow()
	{
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
