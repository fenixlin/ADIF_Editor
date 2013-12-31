package main;

import javax.swing.JOptionPane;

public class MainController {
	
	//public static void main(String[] args) {
		///////所有函数描述请遵循这个格式///////
		
		// set some configurations before it (i.e. loading enumerations, loading arrange configurations)
		// Check the link below...filtering, drop down list, checkbox are all on it!
		// http://docs.oracle.com/javase/tutorial/uiswing/components/table.html#sorting
		// 【关键】把重要的思路都记录下来。真的是过了两个星期再看就看不懂了卧槽。
		//下一步：简单的类型和长度检查？
		//下下一步：drop down list 和 checkbox的自动增加行？
		//上次看到有个人很会玩嘛！那个样例软件导入不行导出还是很赞的嘛！
		//能不能extends一下JTable然后把整个Table都写到另一个文件啊？
		
		//问题：重新再开一个文件就会出问题……
		//     搜索-一开始直接搜索，不能标出框框……
		
		//架构： MainController-->GUIFrame-->GUITable
		//     MainController-->StorageLoader
		
		//user-defined field not done yet!!!
		//这里说一下，我们显示的表头的东西应该是FIELDNAME
		//然后user-defined的东西可以在文件头部声明，可选择附带范围或枚举类型(人家只要枚举啦)
		//user-defined还要能够加入什么的吧，还有枚举类型呢
		
		//sort应该是基于QSO_DATE和TIME_ON
		//弄一个TextArea的help?
		
		//要能够读"ADI"
		//13M的文件
		
		//check随便啦
		//CNTY什么的也要弄进去呢TAT
		//不用config文件，移动到什么位置按这个顺序存就好了
		
		//进度条
		
		//根据同一行别的数据动态决定枚举列表的项目？监听鼠标点击来添加枚举列表？
		//不区分大小写的enumeration?

	//	try
	//	{
	//		ConfigLoader.loadData();
	//	}
	//	catch (Exception e)
	//	{
			//弹窗提示
	//		JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	//	}
	//	new GUIFrame();
		
		//configuration的保存
	//}
}
