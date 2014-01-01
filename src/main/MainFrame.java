package main;

import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import net.coderazzi.filters.gui.*;
import net.coderazzi.filters.gui.TableFilterHeader.*;

import java.awt.Color;
import java.awt.event.*;

public class MainFrame extends JFrame{

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
	private JMenuItem addRowMenuItem;
	private JMenuItem addColumnMenuItem;
	private JMenuItem removeColumnMenuItem;
	private JMenuItem hideColumnMenuItem;
	private JMenuItem showAllHiddenColumnMenuItem;
	private JMenuItem searchMenuItem;
	private JMenuItem filterMenuItem;
	
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	
	private JFileChooser jFileChooser;
	
	private MainTable table = null;
	
	private TableFilterHeader filterHeader = null;
	
	private Records r;
	
	public static void main(String[] args) {
		try {
			ConfigLoader.loadData();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Fatal error: fail to load initialization data.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		new MainFrame();
	}
	
	public MainFrame()
	{
		//创建初始界面
		super();
		this.setSize(800,600);
		this.setTitle("ADIF Editor v1.0 by lazyowl");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		table = new MainTable(new MainTableModel());	
		this.add(new JScrollPane(table));
		
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		newMenuItem = new JMenuItem("New");
		importMenuItem = new JMenuItem("Import & Merge");
		exportMenuItem = new JMenuItem("Export");
		exitMenuItem = new JMenuItem("Quit");
		
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		exportMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		
		newMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				table.setModel(new MainTableModel());
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
		addRowMenuItem = new JMenuItem("Add row");
		addRowMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				table.addRow();			
			}
		});
		addColumnMenuItem = new JMenuItem("Add column");
		addColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(MainFrame.this, "Input new field name");
				if (target!=null) table.addColumn(target);				
			}
		});
		removeColumnMenuItem = new JMenuItem("Remove column");
		removeColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(MainFrame.this, "Input the column number to remove (starting from 1)");				
				if (target!=null) try
				{
					int col = Integer.parseInt(target)-1;
					table.removeColumn(col);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(MainFrame.this, "Please input legal integer number.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		hideColumnMenuItem = new JMenuItem("Hide column");
		hideColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(MainFrame.this, "Input the column number to hide (starting from 1)");
				if (target!=null) try
				{
					int col = Integer.parseInt(target)-1;
					table.hideColumn(col);
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(MainFrame.this, "Please input legal integer number.", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		showAllHiddenColumnMenuItem = new JMenuItem("Show all hidden column");
		showAllHiddenColumnMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				table.showAllHiddenColumn(MainFrame.this.getWidth());
			}
		});
		
		searchMenuItem = new JMenuItem("Search");
		searchMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(MainFrame.this, "Please input the string to search", "Search", JOptionPane.PLAIN_MESSAGE);
				if (target!=null)
				{
					boolean ok = table.search(target);
					if (!ok) JOptionPane.showMessageDialog(MainFrame.this, "Target not found.");					
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
		addColumnMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		addRowMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		
		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				JOptionPane.showMessageDialog(MainFrame.this, 
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
		
		editMenu.add(addRowMenuItem);
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
		jFileChooser.addChoosableFileFilter(new ADIF3FileFilter2());
		jFileChooser.addChoosableFileFilter(new ADIF2FileFilter());
		jFileChooser.addChoosableFileFilter(new XlsxFileFilter());
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (jFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			thread = new Thread(){
				public void run()
				{
					FileAnalyzer fa = new FileAnalyzer();
					File file = jFileChooser.getSelectedFile();
					String path = file.getAbsolutePath();
					Object selectedFileFilter = jFileChooser.getFileFilter();
					if (selectedFileFilter instanceof ADIF3FileFilter && !path.endsWith(".adi"))
						file = new File(path+".adi");
					else if (selectedFileFilter instanceof ADIF2FileFilter && !path.endsWith(".adi"))
						file = new File(path+".adi");
					else if (selectedFileFilter instanceof ADIF3FileFilter2 && !path.endsWith(".adx"))
						file = new File(path+".adx");
					else if (selectedFileFilter instanceof XlsxFileFilter && !path.endsWith(".xlsx"))
						file = new File(path+".xlsx");
					try {
						r = fa.analyze(file);
					} catch (Exception e) {
						JOptionPane.showMessageDialog(MainFrame.this, "Import failed: Fatal error detected in your file.", "Error", JOptionPane.ERROR_MESSAGE);
					}					
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
						//e.printStackTrace();			
					}
					finally {
						progressBar.dispose();
					}
					//设置好table
					table.importData(r);
					
					//刷新屏幕
					MainFrame.this.revalidate();
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
		jFileChooser.addChoosableFileFilter(new ADIF3FileFilter2());
		jFileChooser.addChoosableFileFilter(new ADIF2FileFilter());
		jFileChooser.addChoosableFileFilter(new XlsxFileFilter());
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (jFileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
		{
			thread = new Thread(){
				public void run()
				{
					r = table.exportData();
					FileExporter fe = new FileExporter();
					File file = jFileChooser.getSelectedFile();
					String path = file.getAbsolutePath();
					String version = "3.0.4";
					Object selectedFileFilter = jFileChooser.getFileFilter();
					if (selectedFileFilter instanceof ADIF3FileFilter && !path.endsWith(".adi"))
						file = new File(path+".adi");
					else if (selectedFileFilter instanceof ADIF2FileFilter)
					{
						version = "2.2.7";
						if (!path.endsWith(".adi")) file = new File(path+".adi");
					}
					else if (selectedFileFilter instanceof ADIF3FileFilter2 && !path.endsWith(".adx"))
						file = new File(path+".adx");
					else if (selectedFileFilter instanceof XlsxFileFilter && !path.endsWith(".xlsx"))
						file = new File(path+".xlsx");
					try
					{
						fe.export(version, r, file);
						JOptionPane.showMessageDialog(MainFrame.this, "Successfully exported.");
					}
					catch (Exception e)
					{
						//e.printStackTrace();
						JOptionPane.showMessageDialog(MainFrame.this, "File not found.", "Error", JOptionPane.ERROR_MESSAGE);
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
						//e.printStackTrace();			
					}
					finally {
						progressBar.dispose();
					}
	            }
	        }.start();
		}
	}
	
	private class ADIF3FileFilter extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any ADIF 3.0.4 file(*.adi)";
		}
	}
	
	private class ADIF3FileFilter2 extends FileFilter
	{
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any ADIF 3.0.4 file(*.adx)";
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
	
}
