package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.*;

import main.ConfigLoader;

public class GUITable extends JTable{

	private static final long serialVersionUID = 1L;
	private int bottomLine = 0;
	private int popupColumn;
	
	private class HiddenColumn
	{
		int num;
		TableColumn col;
		public HiddenColumn(int x, TableColumn y) { num=x; col=y; }
	}
	private LinkedList<HiddenColumn> hiddenColumn = new LinkedList<HiddenColumn>();
	
	public GUITable(MyTableModel t){
		super(t);
		
		this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.setCellSelectionEnabled(true);
		this.setRowSelectionAllowed(true); //选择行模式
		this.setColumnSelectionAllowed(false);
		this.setDefaultEditor(Object.class, new TextCellEditor()); //改变编辑单元格的模式
		this.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
		//this.changeSelection(0, 0, false, false);
		
		addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if (rowAtPoint(e.getPoint())>=0)
				{
					setColumnSelectionAllowed(false);
					setRowSelectionAllowed(true);
				}
			}
		});
		
		//自动增加行
		//tableModel.addRow();
		//bottomLine=getRowCount()-1;
		
		//将列设置为点击列表头选择
		final JTableHeader header = getTableHeader();
		header.setReorderingAllowed(true);
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				setColumnSelectionAllowed(true);
				setRowSelectionAllowed(false);
				clearSelection();
				int col = columnAtPoint(e.getPoint());
				setColumnSelectionInterval(col, col);
			}			
		});
		
		final JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem hideItem = new JMenuItem("Hide"); 
		JMenuItem removeItem = new JMenuItem("Remove");
		
		hideItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	GUITable.this.hideColumn(popupColumn);
            }
        });
		removeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	GUITable.this.removeColumn(popupColumn);
            }
        });
		
		popupMenu.add(hideItem);
		popupMenu.add(removeItem);
		//header.setComponentPopupMenu(popupMenu);
		header.addMouseListener( new MouseAdapter() {			
			@Override
		    public void mouseReleased(MouseEvent e) {
		        if (e.isPopupTrigger())
		        {
		            popupColumn = header.columnAtPoint( e.getPoint() );
		            popupMenu.show(e.getComponent(), e.getX(), e.getY());
		        }
		    }
		});	
		
		//设置DropDownList
		setDropList();
				
		this.addPropertyChangeListener(new TableCellListener(this, new AbstractAction()	{		    
				private static final long serialVersionUID = 1L;
	
				public void actionPerformed(ActionEvent e)
			    {
			        TableCellListener tcl = (TableCellListener)e.getSource();
			        MyTableModel tm = (MyTableModel)GUITable.this.getModel();			        
			        tm.editData(tcl.getRow(), tcl.getColumn(), tcl.getNewValue().toString());
			    }
			})
		);
	}

	public void addRow()
	{
		MyTableModel tm = (MyTableModel)this.getModel();
		tm.addRow();
	}
	
	public void addColumn(String x)
	{
		//TableColumn tc = new TableColumn(this.getColumnCount());
		//tc.setHeaderValue(x);
		MyTableModel tm = (MyTableModel)this.getModel();
		tm.addColumn(x);
	}
	
	public void hideColumn(int col)
	{		
		TableColumn tc = this.getColumnModel().getColumn(col);
		hiddenColumn.addFirst(new HiddenColumn(col, tc));
		this.removeColumn(tc);
		//this.getColumnModel().getColumn(col).setMinWidth(0);
    	//this.getColumnModel().getColumn(col).setMaxWidth(0);
	}
	
	public void removeColumn(int col)
	{
		TableColumn tc = this.getColumnModel().getColumn(col);
		this.removeColumn(tc);
		MyTableModel tm = (MyTableModel)this.getModel();
		tm.removeColumn(tc.getHeaderValue().toString());
	}
	
	/*
	public int displayedColumnNumber(int col)
	{
		int answer = col;
		for (HiddenColumn x: hiddenColumn)
		{
			if (x.num<col) answer++;
		}
		return answer;
	}
*/
	
	public void showAllHiddenColumn(int maxWidth)
	{
		for (HiddenColumn x: hiddenColumn)
		{			
			this.addColumn(x.col);
			this.moveColumn(this.getColumnCount() - 1, x.num);
		}
		hiddenColumn.clear();
	}
	
	public void importData(Records r)
	{
		MyTableModel tableModel = (MyTableModel)this.getModel();
		tableModel.importData(r);
		setDropList();
		
		//redo the hiding
		Collections.reverse(hiddenColumn);
		for (HiddenColumn x: hiddenColumn)
		{			
			this.removeColumn(this.getColumnModel().getColumn(x.num));
		}
		Collections.reverse(hiddenColumn);
		
		//自动增加行
		//tableModel.addRow();
		//bottomLine=getRowCount()-1;
	}
	
	public Records exportData()
	{
		for (HiddenColumn x: hiddenColumn)
		{			
			this.addColumn(x.col);
			this.moveColumn(this.getColumnCount() - 1, x.num);
		}
		LinkedHashSet<String> printTitles = new LinkedHashSet<String>();
		TableColumnModel tcm = this.getColumnModel();
		for (int i=0; i<this.getColumnCount(); i++)
		{
			printTitles.add(tcm.getColumn(i).getHeaderValue().toString());
		}		
		for (HiddenColumn x: hiddenColumn)
		{			
			this.removeColumn(x.col);
		}		
		
		MyTableModel tableModel = (MyTableModel)this.getModel();
		return tableModel.exportData(printTitles);
	}
	
	private class TextCellEditor extends DefaultCellEditor
	{
		//在选中的时候重新改为选择行……后面可能还会用到吧，感觉和Boolean等等也有关
		private static final long serialVersionUID = 1L;

		public TextCellEditor() {super(new JTextField());}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
		{
			//自动加行XD O_o那怎么删除行
			MyTableModel tableModel = (MyTableModel)GUITable.this.getModel();
			//if (row == bottomLine)
			//{
				//bottomLine++;
				//tableModel.addRow();
				//GUITable.this.scrollRectToVisible(GUITable.this.getCellRect(bottomLine, 0, true));
			//}
			JTextField editor = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
			return editor;
		}
	}
	
	private class ComboCellEditor extends DefaultCellEditor
	{
		private static final long serialVersionUID = 1L;

		public ComboCellEditor(JComboBox<String> comboBox) {
			super(comboBox);
		}
		
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
		{
			//自动加行XD O_o那怎么删除行
			MyTableModel tableModel = (MyTableModel)GUITable.this.getModel();
			//if (row == bottomLine)
			//{
				//bottomLine++;
				//tableModel.addRow();
				//GUITable.this.scrollRectToVisible(GUITable.this.getCellRect(bottomLine, 0, true));
			//}
			
			@SuppressWarnings("unchecked")
			JComboBox<String> editor = (JComboBox<String>) super.getTableCellEditorComponent(table, value, isSelected, row, column);
			
			TableColumn tc = GUITable.this.getColumnModel().getColumn(column);	
			String header = tc.getHeaderValue().toString();
			//dynamically add items
			if (header.equalsIgnoreCase("SUBMODE"))
			{
				String modeValue = tableModel.getRowValue(row, "MODE");
				if (modeValue!=null)
				{
					DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)editor.getModel();
					model.removeAllElements();
					ConfigLoader cl = new ConfigLoader();
					ArrayList<String> list = cl.getSubmodeList(modeValue);
					if (list!=null)
						for (String x: list)
						{
							model.addElement(x);
						}
				}
			}
			else if (header.equalsIgnoreCase("STATE"))
			{
				String modeValue = tableModel.getRowValue(row, "DXCC");
				if (modeValue!=null)
				{
					DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)editor.getModel();
					model.removeAllElements();
					ConfigLoader cl = new ConfigLoader();
					ArrayList<String> list = cl.getStateList(modeValue);
					if (list!=null)
						for (String x: list)
						{
							model.addElement(x);
						}
				}
			}
			else if (header.equalsIgnoreCase("MY_STATE"))
			{
				String modeValue = tableModel.getRowValue(row, "MY_DXCC");
				if (modeValue!=null)
				{
					DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)editor.getModel();
					model.removeAllElements();
					ConfigLoader cl = new ConfigLoader();
					ArrayList<String> list = cl.getStateList(modeValue);
					if (list!=null)
						for (String x: list)
						{
							model.addElement(x);
						}
				}
			}
			
			//Case insensitive selecting			
			if (value != null)
			{
				String valueString = value.toString().toUpperCase();
				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)editor.getModel();
				boolean found = false;
				for (int i=1; i<model.getSize(); i++)
				{
					if (valueString.equals(model.getElementAt(i).toUpperCase()))
					{
						editor.setSelectedIndex(i);
						found = true;
						break;
					}
				}
				if (!found)
				for (int i=1; i<model.getSize(); i++)
				{
					if (valueString.startsWith(model.getElementAt(i).toUpperCase()))
					{
						editor.setSelectedIndex(i);
						break;
					}
				}
			}
			
			//set editable
			editor.setEditable(false);					
			if (header.equals("AWARD_SUBMITTED") || header.equals("AWARD_GRANTED")) editor.setEditable(true);
			
			return editor;
		}		
	}

	public boolean search(String target)
	{		
		for (int row = 0; row <= this.getRowCount() - 1; row++) {			 
            for (int col = 0; col <= this.getColumnCount() - 1; col++) {
            	Object value = this.getValueAt(row, col);
            	if (value!=null)
            	{
            		String temp = value.toString();
            		if (temp.contains(target)) {
            			this.scrollRectToVisible(this.getCellRect(row, 0, true));
            			this.setRowSelectionInterval(row, row);
            			this.changeSelection(row, col, false, false);
            			//this.getColumnModel().getColumn(col).setCellRenderer(new HighlightRenderer());
            			return true;
            		}
                }
            }
		}
		return false;
	}
	
	private class CustomTableCellRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;	
	    
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if (hasFocus)//(table.isCellSelected(row, column))
			    setBorder(BorderFactory.createMatteBorder(2, 1, 2, 1, Color.BLACK));
			else setBorder(BorderFactory.createEmptyBorder());
			/*
			else if (table.isRowSelected(row))
			    c.setBackground(Color.pink);
			else if (!table.isColumnSelected(column)) // seems the table will be refreshed
			    c.setBackground(Color.white);
			*/
			return c;   
		}	    
	}
	
	/*
	 * 有空可以把选中的格子的显示方法强调一下如下
	 * 
	private class HighlightRenderer extends DefaultTableCellRenderer {
		 
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 
            // everything as usual
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
 
            // added behavior
            if(row == table.getSelectedRow()) {
 
                // this will customize that kind of border that will be use to highlight a row
                setBorder(BorderFactory.createMatteBorder(2, 1, 2, 1, Color.BLACK));
            }
 
            return this;
        }
    }
    */
	
	private void setDropList()
	{
		TableColumnModel colModel = getColumnModel();
		ConfigLoader sl = new ConfigLoader();
		for (int i = 0; i<getColumnCount(); i++)
		{
			TableColumn col = colModel.getColumn(i);
			String key = (String)col.getHeaderValue();
			ArrayList<String> values = sl.getEnumList(key);
			if (values!=null)
			{
				/*
				JComboBox<String> comboBox = new JComboBox<String>();
				comboBox.addItem("");//Allow there to be nothing
				for (String v: values)
				{
					comboBox.addItem(v);
				}
				*/
				String[] tmp = new String[values.size()];
				DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(values.toArray(tmp));				
				JComboBox<String> comboBox = new JComboBox<String>(model);
				col.setCellEditor(new ComboCellEditor(comboBox));
			}
		}
	}
	
	

	/*
	 *  This class listens for changes made to the data in the table via the
	 *  TableCellEditor. When editing is started, the value of the cell is saved
	 *  When editing is stopped the new value is saved. When the oold and new
	 *  values are different, then the provided Action is invoked.
	 *
	 *  The source of the Action is a TableCellListener instance.
	 *  
	 *  Source : http://tips4java.wordpress.com/2009/06/07/table-cell-listener/
	 */
	private class TableCellListener implements PropertyChangeListener, Runnable
	{
		private JTable table;
		private Action action;

		private int row;
		private int column;
		private Object oldValue;
		private Object newValue;

		public TableCellListener(JTable table, Action action)
		{
			this.table = table;
			this.action = action;
		}

		private TableCellListener(JTable table, int row, int column, Object oldValue, Object newValue)
		{
			this.table = table;
			this.row = row;
			this.column = column;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public int getColumn() {return column;}
		public Object getNewValue() {return newValue;}
		public Object getOldValue() {return oldValue;}
		public int getRow() {return row;}
		public JTable getTable() {return table;}

		@Override
		public void propertyChange(PropertyChangeEvent e)
		{
			if ("tableCellEditor".equals(e.getPropertyName()))
			{
				if (table.isEditing())
					processEditingStarted();
				else
					processEditingStopped();
			}
		}

		private void processEditingStarted()
		{
			//  The invokeLater is necessary because the editing row and editing
			//  column of the table have not been set when the "tableCellEditor"
			//  PropertyChangeEvent is fired.
			//  This results in the "run" method being invoked

			SwingUtilities.invokeLater( this );
		}
		
		@Override
		public void run()
		{			
			row = table.convertRowIndexToModel( table.getEditingRow() );
			column = table.convertColumnIndexToModel( table.getEditingColumn() );
			oldValue = table.getModel().getValueAt(row, column);
			newValue = null;
		}

		private void processEditingStopped()
		{
			newValue = table.getModel().getValueAt(row, column);

			if (newValue!=null && !newValue.equals(oldValue))
			{
				//  Make a copy of the data in case another cell starts editing
				//  while processing this change

				TableCellListener tcl = new TableCellListener(
					getTable(), getRow(), getColumn(), getOldValue(), getNewValue());

				ActionEvent event = new ActionEvent(
					tcl,
					ActionEvent.ACTION_PERFORMED,
					"");
				action.actionPerformed(event);
			}
		}
	}
}
