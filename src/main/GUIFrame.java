package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

import java.awt.event.*;
//��ƽ̨�����swing��ͳ��TAT!!

public class GUIFrame {

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
	
	private GUITable table;
	
	public GUIFrame()
	{
		//������ʼ����
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
		//����open file��merge file�����˳��Ϸ���ʽ���ļ�
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) return true;
			else if (f.getName().endsWith(".adi") || f.getName().endsWith(".adx")) return true;
			else return false;
		}

		@Override
		public String getDescription() {
			return "*����ADIF�ļ�(*.adi;*.adx)";
		}
		
	}
	
	private void openFile()
	{
		//open file�Ĳ˵���
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
		//File analyzer����record���ݴ˴���table
		
		//ת��data
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
		
		//���ú�table
		table = new GUITable(new DefaultTableModel(data,head));
						
		//ˢ����Ļ - ��ľ�и��õķ���- -��
		frame.setVisible(false);
		frame.add(new JScrollPane(table));
		frame.setVisible(true);
	}
	
	
}
