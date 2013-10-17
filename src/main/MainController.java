package main;

import javax.swing.JOptionPane;

public class MainController {
	
	public static void main(String[] args) {
		// set some configurations before it (i.e. loading enumerations, loading arrange configurations)
		// Check the link below...filtering, drop down list, checkbox are all on it!
		// http://docs.oracle.com/javase/tutorial/uiswing/components/table.html#sorting
		//��һ����GUIдһ������ȥ�����һ���ǲ���enum�ǵĻ���һ��dropdownlist
		//�ϴο����и��˺ܻ�����Ǹ�����������벻�е������Ǻ��޵��
		//�ܲ���extendsһ��JTableȻ�������Table��д����һ���ļ�����
		
		try
		{
			DataLoader.loadData();
		}
		catch (Exception e)
		{
			//������ʾ
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		new GUIFrame();
		
		//configuration�ı���
	}

}
