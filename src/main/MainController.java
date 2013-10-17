package main;

import javax.swing.JOptionPane;

public class MainController {
	
	public static void main(String[] args) {
		// set some configurations before it (i.e. loading enumerations, loading arrange configurations)
		// Check the link below...filtering, drop down list, checkbox are all on it!
		// http://docs.oracle.com/javase/tutorial/uiswing/components/table.html#sorting
		//下一步：GUI写一个函数去检查这一行是不是enum是的话加一个dropdownlist
		//上次看到有个人很会玩嘛！那个样例软件导入不行导出还是很赞的嘛！
		//能不能extends一下JTable然后把整个Table都写到另一个文件啊？
		
		try
		{
			DataLoader.loadData();
		}
		catch (Exception e)
		{
			//弹窗提示
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		new GUIFrame();
		
		//configuration的保存
	}

}
