package main;

import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import net.coderazzi.filters.gui.*;
import net.coderazzi.filters.gui.TableFilterHeader.*;

import java.awt.Color;
import java.awt.event.*;
//跨平台起见，swing丑就丑吧TAT!!

public class GUIFrame {

	private JFrame frame;
	
	private JMenuBar menuBar;
	
	private JMenu fileMenu;
	private JMenuItem newMenuItem;
	private JMenuItem importMenuItem;
	private JMenu exportMenu;
	private JMenuItem exportAdxMenuItem;
	private JMenuItem exportAdiMenuItem;
	private JMenuItem exportAdi2MenuItem;
	private JMenuItem exportXlsxMenuItem;
	private JMenuItem exitMenuItem;
	
	private JMenu editMenu;
	private JMenuItem addColumnMenuItem;
	private JMenuItem removeColumnMenuItem;
	private JMenuItem searchMenuItem;
	private JMenuItem filterMenuItem;
	
	private JMenu viewMenu;
	private JMenuItem headerMenuItem;
	
	private JMenu helpMenu;
	private JMenuItem aboutMenuItem;
	
	private JFileChooser jFileChooser;
	
	private GUITable table = null;
	
	private TableFilterHeader filterHeader = null;
	
	public GUIFrame()
	{
		//创建初始界面
		frame = new JFrame();
		frame.setSize(800,600);
		frame.setTitle("ADIF Editor v1.0 by lazyowl");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		newMenuItem = new JMenuItem("New");
		importMenuItem = new JMenuItem("Import / Merge");
		exportMenu = new JMenu("Export");
		exportAdxMenuItem = new JMenuItem("Export *.adx in ADIF v 3.0.4");
		exportAdiMenuItem = new JMenuItem("Export *.adi in ADIF v 3.0.4");
		exportAdi2MenuItem = new JMenuItem("Export *.adi in ADIF v 2.2.7");
		exportXlsxMenuItem = new JMenuItem("Export *.xlsx");
		exitMenuItem = new JMenuItem("Exit");
		
		editMenu = new JMenu("Edit");
		addColumnMenuItem = new JMenuItem("Add column");
		removeColumnMenuItem = new JMenuItem("Remove column");
		searchMenuItem = new JMenuItem("Search");
		searchMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
		searchMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				String target = JOptionPane.showInputDialog(table, "请输入要搜索的值", "搜索", JOptionPane.PLAIN_MESSAGE);
				if (target!=null)
				{
					boolean ok = table.search(target);
					if (!ok) JOptionPane.showMessageDialog(table, "没有找到相应的值");					
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
		
		
		viewMenu = new JMenu("View");
		headerMenuItem = new JMenuItem("View Header");
		
		helpMenu = new JMenu("Help");
		aboutMenuItem = new JMenuItem("About");		
		
		newMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		importMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		
		importMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				importFile();
			}
		});
		
		exitMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt)
			{
				System.exit(0);
			}			
		});
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenu);
		menuBar.add(helpMenu);
		
		fileMenu.add(newMenuItem);
		fileMenu.add(importMenuItem);
		exportMenu.add(exportAdi2MenuItem);
		exportMenu.add(exportAdiMenuItem);
		exportMenu.add(exportAdxMenuItem);
		exportMenu.add(exportXlsxMenuItem);
		fileMenu.add(exportMenu);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);
		
		editMenu.add(addColumnMenuItem);
		editMenu.add(removeColumnMenuItem);
		editMenu.addSeparator();
		editMenu.add(searchMenuItem);
		editMenu.add(filterMenuItem);
		
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
			else if (f.getName().endsWith(".adi") || f.getName().endsWith(".adx") || f.getName().endsWith(".xls") || f.getName().endsWith(".xlsx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "Any ADIF file(*.adi;*.adx) or Excel file(*.xls;*.xlsx)";
		}
	}
	
	private void importFile()
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
			
			//设置好table
			if (table!=null)
			{
				
			}
			else
			{
				table = new GUITable(new MyTableModel(r));	
				frame.add(new JScrollPane(table));
			}			
			
			//刷新屏幕
			frame.revalidate();
			table.requestFocusInWindow(); //有了focus搜索才能显示
		}
	}
}
