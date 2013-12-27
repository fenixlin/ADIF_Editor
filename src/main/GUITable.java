package main;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.*;

public class GUITable extends JTable{

	private static final long serialVersionUID = 1L;
	private MyTableModel tableModel = null;	
	private int bottomLine = 0;
	
	public GUITable(MyTableModel t){
		super(t);
		tableModel = t;
		
		this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		this.setCellSelectionEnabled(true);
		this.setRowSelectionAllowed(true); //选择行模式
		this.setColumnSelectionAllowed(false);
		this.setDefaultEditor(Object.class, new MyCellEditor()); //改变编辑单元格的模式
		CustomTableCellRenderer renderer = new CustomTableCellRenderer();
		this.setDefaultRenderer(Object.class, renderer);
		this.changeSelection(0, 0, false, false);
		
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
		tableModel.addRow();
		bottomLine=getRowCount()-1;
		
		//将列设置为点击列表头选择
		final JTableHeader header = getTableHeader();
		header.setReorderingAllowed(true);
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setColumnSelectionAllowed(true);
				setRowSelectionAllowed(false);
				clearSelection();
				int col = columnAtPoint(e.getPoint());
				setColumnSelectionInterval(col, col);
			}
		});
		
		//设置DropDownList
		setDropList();
	}
	
	public void importData(Records r)
	{
		
	}
	
	private class MyCellEditor extends DefaultCellEditor
	{
		//在选中的时候重新改为选择行……后面可能还会用到吧，感觉和Boolean等等也有关
		private static final long serialVersionUID = 1L;

		public MyCellEditor() {super(new JTextField());}
		
		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) 
		{
			//自动加行XD O_o那怎么删除行
			if (row == bottomLine)
			{
				bottomLine++;
				tableModel.addRow();
			}
			JTextField editor = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
			return editor;
		}
	}

	public boolean search(String value)
	{		
		for (int row = 0; row <= this.getRowCount() - 1; row++) {			 
            for (int col = 0; col <= this.getColumnCount() - 1; col++) {
            	String temp = this.getValueAt(row, col).toString();
                if (temp.contains(value)) {
                	this.scrollRectToVisible(this.getCellRect(row, 0, true));
                	this.setRowSelectionInterval(row, row);
                	this.changeSelection(row, col, false, false);
                	//this.getColumnModel().getColumn(col).setCellRenderer(new HighlightRenderer());
                	return true;
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
		StorageLoader sl = new StorageLoader();
		for (int i = 0; i<getColumnCount(); i++)
		{
			TableColumn col = colModel.getColumn(i);
			String key = (String)col.getHeaderValue();
			ArrayList<String> values = sl.getEnumList(key);
			if (values!=null)
			{
				JComboBox<String> comboBox = new JComboBox<String>();
				comboBox.addItem("");//Allow there to be nothing
				for (String v: values)
				{
					comboBox.addItem(v);
				}
				col.setCellEditor(new DefaultCellEditor(comboBox));
			}
		}
	}
}
