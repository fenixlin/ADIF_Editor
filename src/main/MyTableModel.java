package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel{

	private static final long serialVersionUID = 1L;
	
	private LinkedHashSet<String> titles = null;
	private HashMap<String, String> types = null;
	private ArrayList<HashMap<String,String>> records = null;
	private ArrayList<Boolean> isCheckBox = null;
	
	public MyTableModel(Records r)
	{
		super();
		titles = r.getTitles();
		types = r.getTypes();
		records = r.getRecords();		
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		Vector<String> head = new Vector<String>();
		isCheckBox = new ArrayList<Boolean>();
		
		for (String s : titles)
		{
			head.add(s);
			if (types.get(s).equals("B")) isCheckBox.add(new Boolean(true));
			else isCheckBox.add(new Boolean(false));
		}
		for (int i=0; i<records.size(); i++)
		{
			HashMap<String,String> hm = records.get(i);
			Vector<Object> row = new Vector<Object>();
			int j = 0;
			for(String s:titles)
			{	
				String value = hm.get(s);
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
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
	    if (isCheckBox.get(columnIndex))
	        return Boolean.class;
	    return super.getColumnClass(columnIndex);
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
}
