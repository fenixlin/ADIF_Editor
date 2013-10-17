package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Vector;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

import java.awt.Component;
import java.awt.event.*;
//跨平台起见，swing丑就丑吧TAT!!

public class GUIController {

	private JFrame frame;
	
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem openMenuItem;
	private JMenuItem importMenuItem;
	private JMenu exportMenu;
	private JMenuItem exportAdxMenuItem;
	private JMenuItem exportAdiMenuItem;
	private JMenuItem exitMenuItem;
	private JMenu viewMenu;
	private JMenuItem headerMenuItem;
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	
	private JFileChooser jFileChooser;
	
	private JTable table;
	
	private int bottomLine;
	
	public GUIController()
	{
		//创建初始界面
		frame = new JFrame();
		frame.setSize(800,600);
		
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		openMenuItem = new JMenuItem("Open");
		importMenuItem = new JMenuItem("Import");
		exportMenu = new JMenu("Export");
		exportAdxMenuItem = new JMenuItem("Export *.adx in ADIF v 3.0.4");
		exportAdiMenuItem = new JMenuItem("Export *.adi in ADIF v 3.0.4");
		exitMenuItem = new JMenuItem("Exit");
		viewMenu = new JMenu("View");
		headerMenuItem = new JMenuItem("View Header");
		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About");		
		
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		
		
		openMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				openFile();
			}
		});
		
		exitMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				System.exit(0);
			}			
		});
		
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(helpMenu);
		fileMenu.add(openMenuItem);
		fileMenu.add(importMenuItem);
		exportMenu.add(exportAdiMenuItem);
		exportMenu.add(exportAdxMenuItem);
		fileMenu.add(exportMenu);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		
		
		viewMenu.add(headerMenuItem);
		
		helpMenu.add(aboutMenuItem);
		
		//Do something for preview
		
		frame.setVisible(true);
	}
	
	private class OpenFileFilter extends FileFilter
	{
		//帮助open file和merge file来过滤出合法格式的文件
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi") || f.getName().endsWith(".adx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "*所有ADIF文件(*.adi;*.adx)";
		}
		
	}
	
	private void openFile()
	{
		//open file的菜单项
		jFileChooser = new JFileChooser(new File("."));
		OpenFileFilter fileFilter = new OpenFileFilter();
		jFileChooser.removeChoosableFileFilter(jFileChooser.getFileFilter());
		jFileChooser.setFileFilter(fileFilter);
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (jFileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
		{
			FileAnalyzer fa = new FileAnalyzer();
			Records r = fa.analyze(jFileChooser.getSelectedFile());
			drawTable(r);
		}
	}
	
	private void drawTable(Records r)
	{
		//File analyzer给个record，据此创建table
		
		//转化data
		LinkedHashSet<String> titles = r.getTitles();
		ArrayList<HashMap<String,String>> records = r.getRecords();
		
		String[][] data = new String[records.size()][titles.size()];
		String[] head = new String[titles.size()];
		
		int k=0;
		for (String s : titles)
		{
			head[k]=s;
			k++;
		}
		for (int i=0;i<records.size();i++)
		{
			int j=0;
			HashMap<String,String> hm = records.get(i);
			for(String s:titles)
			{
				data[i][j]=hm.get(s);
				j++;
			}
		}
		
		//设置好table
		table = new JTable(new DefaultTableModel(data,head));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setRowSelectionAllowed(true); //选择行模式
		table.setColumnSelectionAllowed(false);
		table.setDefaultEditor(Object.class, new MyCellEditor()); //改变编辑单元格的模式
		table.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if (table.rowAtPoint(e.getPoint())>=0)
				{
					table.setColumnSelectionAllowed(false);
					table.setRowSelectionAllowed(true);
				}
			}
		});
		
		//自动增加行
		addRow();
		bottomLine=table.getRowCount()-1;
		
		//将列设置为点击列表头选择
		final JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(true);
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				table.setColumnSelectionAllowed(true);
				table.setRowSelectionAllowed(false);
				table.clearSelection();
				int col = table.columnAtPoint(e.getPoint());
				table.setColumnSelectionInterval(col, col);
			}
		});
		
		
		//刷新屏幕 - 有木有更好的方法- -？
		frame.setVisible(false);
		frame.add(new JScrollPane(table));
		frame.setVisible(true);
	}
	
	private void addRow()
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		Vector<String> v = new Vector<String>();
		v.add("");
		model.addRow(v);
	}
	
	private class MyCellEditor extends DefaultCellEditor
	{
		//在选中的时候重新改为选择行……后面可能还会用到吧，感觉和Boolean等等也有关
		private static final long serialVersionUID = 1L;

		public MyCellEditor() {super(new JTextField());}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
		{
			//如果能够在cell里面加一个mouselistener或者还会更好呢！（这样的话还是要加一个）
			//table.setColumnSelectionAllowed(false);
			//table.setRowSelectionAllowed(true);
			
			//自动加行XD O_o那怎么删除行
			if (row == bottomLine)
			{
				bottomLine++;
				addRow();
			}
			JTextField editor = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
			return editor;
		}
	}
}
