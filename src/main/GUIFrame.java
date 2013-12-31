package main;

import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import net.coderazzi.filters.gui.*;
import net.coderazzi.filters.gui.TableFilterHeader.*;

import java.awt.Color;
import java.awt.event.*;
//跨平台起见，swing丑就丑吧TAT!!

public class GUIFrame extends JFrame{

	private static final long serialVersionUID = 1L;

	private Thread thread;
	private ProgressBar progressBar;
	
	private JMenuBar menuBar;
	
	private JMenu fileMenu;
	private JMenuItem newMenuItem;
	private JMenuItem importMenuItem;	
	private JMenuItem exportMenuItem;
	private JMenuItem exitMenuItem;
	
	private JMenu editMenu;
	private JMenuItem addColumnMenuItem;
	private JMenuItem removeColumnMenuItem;
	private JMenuItem hideColumnMenuItem;
	private JMenuItem showAllHiddenColumnMenuItem;
	private JMenuItem searchMenuItem;
	private JMenuItem filterMenuItem;
	
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	
	private JFileChooser jFileChooser;
	
	private GUITable table = null;
	
	private TableFilterHeader filterHeader = null;
	
	private Records r;
	
	public static void main(String[] args) {
		ConfigLoader.loadData();
		new GUIFrame();
	}
	
	public GUIFrame()
	{
		//创建初始界面
		super();
		this.setSize(800,600);
		this.setTitle("ADIF Editor v1.0 by lazyowl");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		table = new GUITable(new MyTableModel());	
		this.add(new JScrollPane(table));
		
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		newMenuItem = new JMenuItem("New");
		importMenuItem = new JMenuItem("Import & Merge");
		exportMenuItem = new JMenuItem("Export");
		exitMenuItem = new JMenuItem("Exit");
		
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		
		newMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				table.setModel(new MyTableModel());
			}
		});		
		importMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				importFile();
			}
		});
		exportMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				exportFile();				
			}
		});		
		exitMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				System.exit(0);
			}
		});
		
		editMenu = new JMenu("Edit");
		addColumnMenuItem = new JMenuItem("Add column");
		addColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(GUIFrame.this, "Input new field name");
				if (target!=null) table.addColumn(target);				
			}
		});
		removeColumnMenuItem = new JMenuItem("Remove column");
		removeColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(GUIFrame.this, "请输入要隐藏的列号(列号从1开始)");				
				if (target!=null) try
				{
					int col = Integer.parseInt(target)-1;
					table.removeColumn(col);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(GUIFrame.this, "Please input legal integer number.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		hideColumnMenuItem = new JMenuItem("Hide column");
		hideColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(GUIFrame.this, "请输入要隐藏的列号(列号从1开始)");
				if (target!=null) try
				{
					int col = Integer.parseInt(target)-1;
					table.hideColumn(col);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(GUIFrame.this, "Please input legal integer number.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		showAllHiddenColumnMenuItem = new JMenuItem("Show all hidden column");
		showAllHiddenColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				table.showAllHiddenColumn(GUIFrame.this.getWidth());
			}
		});
		
		searchMenuItem = new JMenuItem("Search");
		searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		searchMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(GUIFrame.this, "Please input the string to search", "Search", JOptionPane.PLAIN_MESSAGE);
				if (target!=null)
				{
					boolean ok = table.search(target);
					if (!ok) JOptionPane.showMessageDialog(GUIFrame.this, "Target not found.");					
				}
			}
		});
		filterMenuItem = new JMenuItem("Show / Hide filter");
		filterMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				if (filterHeader!=null && filterHeader.isVisible())
				{
					filterHeader.setVisible(false);
				}
				else
				{
					filterHeader = new TableFilterHeader(table, AutoChoices.ENABLED);
					filterHeader.setPosition(Position.TOP);
					filterHeader.setBackground(Color.darkGray);
					filterHeader.setForeground(Color.white);
					filterHeader.setVisible(true);
				}
			}
		});
		
		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				JOptionPane.showMessageDialog(GUIFrame.this, 
						"ADIF Editor v1.0 is a simple editor build for ADIF format data.\n"
						+ "If you have any questions, please contact fenixl@163.com\n"
						+ "                                                              Author: Lazyowl", 
						"About", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);
		
		fileMenu.add(newMenuItem);
		fileMenu.add(importMenuItem);
		fileMenu.add(exportMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		
		editMenu.add(addColumnMenuItem);
		editMenu.add(removeColumnMenuItem);
		editMenu.add(hideColumnMenuItem);
		editMenu.add(showAllHiddenColumnMenuItem);
		editMenu.addSeparator();
		editMenu.add(searchMenuItem);
		editMenu.add(filterMenuItem);		
		
		helpMenu.add(aboutMenuItem);
		
		//Do something for preview		
		this.setVisible(true);
	}	
	
	private void importFile()
	{
		//open file的菜单项
		jFileChooser = new JFileChooser(new File("."));
		jFileChooser.removeChoosableFileFilter(jFileChooser.getFileFilter());
		jFileChooser.addChoosableFileFilter(new ADIF3FileFilter());
		jFileChooser.addChoosableFileFilter(new ADIF2FileFilter());
		jFileChooser.addChoosableFileFilter(new XlsxFileFilter());
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			thread = new Thread(){
				public void run()
				{
					FileAnalyzer fa = new FileAnalyzer();
					r = fa.analyze(jFileChooser.getSelectedFile());
				}
			};
			
			progressBar = new ProgressBar(this, "Importing file, please wait......");
			progressBar.setVisible(true);
			new Thread()
	        {
	            public void run()
	            {
					try {
						thread.run();
						thread.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();			
					}
					finally {
						progressBar.dispose();
					}
					//设置好table
					table.importData(r);
					
					//刷新屏幕
					GUIFrame.this.revalidate();
					table.requestFocusInWindow(); //有了focus搜索才能显示
	            }
	        }.start();
		}
	}
	
	private void exportFile()
	{
		jFileChooser = new JFileChooser(new File("."));
		jFileChooser.removeChoosableFileFilter(jFileChooser.getFileFilter());
		jFileChooser.addChoosableFileFilter(new ADIF3FileFilter());
		jFileChooser.addChoosableFileFilter(new ADIF2FileFilter());
		jFileChooser.addChoosableFileFilter(new XlsxFileFilter());
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (jFileChooser.showSaveDialog(GUIFrame.this) == JFileChooser.APPROVE_OPTION)
		{
			thread = new Thread(){
				public void run()
				{
					r = table.exportData();
					FileExporter fe = new FileExporter();
					try
					{
						fe.export(r, jFileChooser.getSelectedFile());
					}
					catch (Exception e)
					{
						e.printStackTrace();
						JOptionPane.showMessageDialog(GUIFrame.this, "File not found.", "Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			};
			
			progressBar = new ProgressBar(this, "Exporting file, please wait......");
			progressBar.setVisible(true);
			new Thread()
	        {
	            public void run()
	            {
					try {
						thread.run();
						thread.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();			
					}
					finally {
						progressBar.dispose();
					}
					
					JOptionPane.showMessageDialog(GUIFrame.this, "Successfully exported.");	
	            }
	        }.start();
		}
	}
	
	private class ADIF3FileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi") || f.getName().endsWith(".adx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any ADIF 3.0.4 file(*.adi;*.adx)";
		}
	}
	
	private class ADIF2FileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any ADIF 2.2.7 file(*.adi)";
		}
	}
	
	private class XlsxFileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".xlsx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any Excel 2007(or later) file(*.xlsx)";
		}
	}
	
	/*
	private class AllFileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi") || f.getName().endsWith(".adx") || f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any legal file(*.adi;*.adx;*.xls;*.xlsx)";
		}
	}
	*/
}
