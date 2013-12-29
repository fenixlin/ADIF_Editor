package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

public class Records {
	//记录从文件读入的record信息
		private LinkedHashSet<String> titles;
		private HashMap<String,String> types;//values are all upper-case, default type is S
		private ArrayList<HashMap<String,String>> records;
		private HashMap<String, UDF> udfs;
		private HashMap<String, String> apps;
		
		public Records() {}
		
		public Records(LinkedHashSet<String> x, HashMap<String,String> y, ArrayList<HashMap<String,String>> z, HashMap<String, UDF> p, HashMap<String, String> q)
		{
			titles = x;
			types = y;
			records = z;
			udfs = p;
			apps = q;
		}
		
		public LinkedHashSet<String> getTitles() {return titles;}
		public HashMap<String,String> getTypes() {return types;}
		public ArrayList<HashMap<String,String>> getRecords() {return records;}
		public HashMap<String, UDF> getUDFs() {return udfs;}
		public HashMap<String, String> getAPPs() {return apps;}
						
		public void setTitles(LinkedHashSet<String> x) {titles=x;}
		public void setTypes(HashMap<String,String> x) {types=x;}
		public void setRecords(ArrayList<HashMap<String,String>> x) {records=x;}
		public void setUDFs(HashMap<String, UDF> x) {udfs=x;}
		public void setAPPs(HashMap<String, String> x) {apps=x;}
}

class UDF {
	private int fieldID;
	private String type;
	private ArrayList<String> enums;
	private String range;
	
	public UDF(int a, String b, ArrayList<String> c, String d)
	{
		fieldID = a;
		type = b;
		enums = c;
		range = d;
	}
	
	public int getFieldID() {return fieldID;}
	public String getType() {return type;}
	public ArrayList<String> getEnums() {return enums;}
	public String getRange() {return range;}
}

