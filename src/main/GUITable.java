package main;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.*;

public class GUITable extends JTable{

	private static final long serialVersionUID = 1L;
	private MyTableModel tableModel = null;
	
	public GUITable(MyTableModel t){
		super(t);
		tableModel = t;
		
		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setRowSelectionAllowed(true); //选择行模式
		setColumnSelectionAllowed(false);
		setDefaultEditor(Object.class, new MyCellEditor()); //改变编辑单元格的模式
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
	
	private int bottomLine = 0;
	
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
